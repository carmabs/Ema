package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.Ema
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class ListState(val users: List<String> = emptyList(), val notified: Boolean = false) : EmaState

private sealed interface SenderAction : EmaAction {
    data class Create(val user: String) : SenderAction
}

private sealed interface ReceiverAction : EmaAction

/**
 * Broadcast actions: their type is the id of the broadcast
 */
private sealed interface UserBroadcast : EmaAction {
    data class Created(val user: String) : UserBroadcast
    data class Deleted(val user: String) : UserBroadcast
}

private sealed interface NoEvent : EmaEvent

@OptIn(ExperimentalCoroutinesApi::class)
class EmaSourceBroadcastTest {

    private val received = mutableListOf<String>()

    private fun TestScope.senderSource() = EmaSource<ListState, SenderAction, NoEvent>(
        ListState(),
        UnconfinedTestDispatcher(testScheduler)
    ) {
        sideEffects {
            add<SenderAction.Create> { action ->
                broadcast(UserBroadcast.Created(action.user))
            }
        }
    }

    private fun TestScope.receiverSource() = EmaSource<ListState, ReceiverAction, NoEvent>(
        ListState(),
        UnconfinedTestDispatcher(testScheduler)
    ) {
        middlewares {
            add<UserBroadcast> { action ->
                received += "middleware $action"
                next()
            }
        }
        reducers {
            add<UserBroadcast> { action ->
                when (action) {
                    is UserBroadcast.Created -> state.copy(users = state.users + action.user)
                    is UserBroadcast.Deleted -> state.copy(users = state.users - action.user)
                }
            }
        }
        sideEffects {
            add<UserBroadcast.Created> { action -> received += "side effect ${action.user}" }
        }
        broadcasts {
            listen<UserBroadcast.Created>()
        }
    }

    @AfterTest
    fun checkBroadcasterIsClean() {
        assertEquals(0, Ema.actionBroadcaster.listenerCount)
        assertEquals(0, Ema.actionBroadcaster.pendingCount)
    }

    @Test
    fun `a broadcast action goes through the middlewares reducers and side effects of the listener`() = runTest {
        val sender = senderSource()
        val receiver = receiverSource()

        sender.dispatch(SenderAction.Create("Ana"))
        runCurrent()

        assertEquals(ListState(users = listOf("Ana")), receiver.state)
        assertEquals(listOf("middleware Created(user=Ana)", "side effect Ana"), received)
        sender.close()
        receiver.close()
    }

    @Test
    fun `only the actions of the listened type are received`() = runTest {
        val receiver = receiverSource()

        Ema.actionBroadcaster.broadcast(UserBroadcast.Created("Ana"))
        Ema.actionBroadcaster.broadcast(UserBroadcast.Deleted("Ana"))
        runCurrent()

        assertEquals(ListState(users = listOf("Ana")), receiver.state)
        assertTrue(received.none { "Deleted" in it })
        receiver.close()
    }

    @Test
    fun `every source listening to the type receives the action`() = runTest {
        val sender = senderSource()
        val firstReceiver = receiverSource()
        val secondReceiver = receiverSource()

        sender.dispatch(SenderAction.Create("Ana"))
        runCurrent()

        assertEquals(listOf("Ana"), firstReceiver.state.users)
        assertEquals(listOf("Ana"), secondReceiver.state.users)
        sender.close()
        firstReceiver.close()
        secondReceiver.close()
    }

    @Test
    fun `the listener is removed when the source is closed`() = runTest {
        val sender = senderSource()
        val receiver = receiverSource()
        assertEquals(1, Ema.actionBroadcaster.listenerCount)

        receiver.close()
        sender.dispatch(SenderAction.Create("Ana"))
        runCurrent()

        assertEquals(0, Ema.actionBroadcaster.listenerCount)
        assertEquals(ListState(), receiver.state)
        assertTrue(received.isEmpty())
        sender.close()
    }

    @Test
    fun `a kept action without listener is delivered to the first source that listens to it`() = runTest {
        Ema.actionBroadcaster.broadcast(UserBroadcast.Created("Ana"), keepUntilReceived = true)

        val firstReceiver = receiverSource()
        val secondReceiver = receiverSource()
        runCurrent()

        assertEquals(listOf("Ana"), firstReceiver.state.users)
        assertEquals(emptyList(), secondReceiver.state.users)
        firstReceiver.close()
        secondReceiver.close()
    }

    @Test
    fun `a kept action with listeners is delivered at the moment and not kept`() = runTest {
        val receiver = receiverSource()

        Ema.actionBroadcaster.broadcast(UserBroadcast.Created("Ana"), keepUntilReceived = true)
        runCurrent()

        assertEquals(listOf("Ana"), receiver.state.users)
        assertEquals(0, Ema.actionBroadcaster.pendingCount)
        receiver.close()
    }

    @Test
    fun `a kept action of a type not listened to is still kept`() = runTest {
        val receiver = receiverSource()

        Ema.actionBroadcaster.broadcast(UserBroadcast.Deleted("Ana"), keepUntilReceived = true)

        assertEquals(1, Ema.actionBroadcaster.pendingCount)
        Ema.actionBroadcaster.clearPendingActions()
        receiver.close()
    }
}
