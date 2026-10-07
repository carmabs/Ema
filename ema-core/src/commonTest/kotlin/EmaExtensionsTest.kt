import com.carmabs.ema.core.extension.checkNull
import com.carmabs.ema.core.extension.checkNullOrEmpty
import com.carmabs.ema.core.extension.constraintValue
import com.carmabs.ema.core.extension.emaRegexGetCharacterValue
import com.carmabs.ema.core.extension.emaRegexGetFloatValue
import com.carmabs.ema.core.extension.forEachFirstLast
import com.carmabs.ema.core.extension.hasDecimals
import com.carmabs.ema.core.extension.ifFalse
import com.carmabs.ema.core.extension.ifFalseLet
import com.carmabs.ema.core.extension.ifTrue
import com.carmabs.ema.core.extension.ifTrueLet
import com.carmabs.ema.core.extension.iteratePositionFromCenter
import com.carmabs.ema.core.extension.iteratePositionFromEnd
import com.carmabs.ema.core.extension.mapEachFirstLast
import com.carmabs.ema.core.extension.mapFirstLast
import com.carmabs.ema.core.extension.maxOrNull
import com.carmabs.ema.core.extension.minOrNull
import com.carmabs.ema.core.extension.replaceLast
import com.carmabs.ema.core.extension.toEmaResult
import com.carmabs.ema.core.extension.toEmaText
import com.carmabs.ema.core.extension.toScope
import com.carmabs.ema.core.extension.update
import com.carmabs.ema.core.manager.PermissionState
import com.carmabs.ema.core.manager.areAllPermissionsGranted
import com.carmabs.ema.core.manager.areAllPermissionsStateGranted
import com.carmabs.ema.core.manager.isPermissionGranted
import com.carmabs.ema.core.manager.isPermissionStateGranted
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.core.value.EmaUriRes
import com.carmabs.ema.core.value.EmaUriType
import com.carmabs.ema.core.value.toUriRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers

class EmaNumberExtensionsTest {

    @Test
    fun `checkNull returns the value or the default`() {
        assertEquals(3, (3 as Int?).checkNull())
        assertEquals(0, (null as Int?).checkNull())
        assertEquals(7, (null as Int?).checkNull(7))
        assertEquals(1.5f, (1.5f as Float?).checkNull())
        assertEquals(0f, (null as Float?).checkNull())
        assertEquals(2.toShort(), (2.toShort() as Short?).checkNull())
        assertEquals(0.toShort(), (null as Short?).checkNull())
        assertEquals(9L, (9L as Long?).checkNull())
        assertEquals(0L, (null as Long?).checkNull())
        assertEquals(2.5, (2.5 as Double?).checkNull())
        assertEquals(0.0, (null as Double?).checkNull())
        assertTrue((true as Boolean?).checkNull())
        assertFalse((null as Boolean?).checkNull())
    }

    @Test
    fun `minOrNull and maxOrNull keep the value inside the limit`() {
        assertEquals(5, 5.minOrNull(5))
        assertNull(4.minOrNull(5))
        assertEquals(5, 5.maxOrNull(5))
        assertNull(6.maxOrNull(5))

        assertEquals(1.5f, 1.5f.minOrNull(1f))
        assertNull(0.5f.minOrNull(1f))
        assertEquals(1.5f, 1.5f.maxOrNull(2f))
        assertNull(2.5f.maxOrNull(2f))

        assertEquals(3.toShort(), 3.toShort().minOrNull(2.toShort()))
        assertNull(1.toShort().minOrNull(2.toShort()))
        assertEquals(3.toShort(), 3.toShort().maxOrNull(4.toShort()))
        assertNull(5.toShort().maxOrNull(4.toShort()))

        assertEquals(10L, 10L.minOrNull(1L))
        assertNull(0L.minOrNull(1L))
        assertEquals(10L, 10L.maxOrNull(10L))
        assertNull(11L.maxOrNull(10L))

        assertEquals(2.0, 2.0.minOrNull(1.0))
        assertNull(0.5.minOrNull(1.0))
        assertEquals(2.0, 2.0.maxOrNull(3.0))
        assertNull(3.5.maxOrNull(3.0))
    }

    @Test
    fun `hasDecimals and constraintValue`() {
        assertTrue(1.5f.hasDecimals())
        assertFalse(2f.hasDecimals())
        assertEquals(5f, 9f.constraintValue(0f, 5f))
        assertEquals(0f, (-1f).constraintValue(0f, 5f))
        assertEquals(3f, 3f.constraintValue(0f, 5f))
        assertEquals(5, 9.constraintValue(0, 5))
        assertEquals(0, (-1).constraintValue(0, 5))
    }

    @Test
    fun `iteratePositionFromCenter visits every position once starting at the center`() {
        val positions = List(7) { iteratePositionFromCenter(it, 7) }
        assertEquals(listOf(3, 2, 4, 1, 5, 0, 6), positions)
        assertEquals((0 until 7).toSet(), positions.toSet())

        val leftFirst = List(5) { iteratePositionFromCenter(it, 5, startRight = false) }
        assertEquals(listOf(2, 3, 1, 4, 0), leftFirst)
    }

    @Test
    fun `iteratePositionFromCenter returns the center out of range`() {
        assertEquals(3, iteratePositionFromCenter(-1, 7))
        assertEquals(3, iteratePositionFromCenter(7, 7))
    }

    @Test
    fun `iteratePositionFromEnd alternates both ends`() {
        assertEquals(listOf(4, 0, 3, 1, 2), List(5) { iteratePositionFromEnd(it, 5) })
        assertEquals(listOf(0, 4, 1, 3, 2), List(5) { iteratePositionFromEnd(it, 5, startRight = false) })
        assertEquals(2, iteratePositionFromEnd(-1, 5))
        assertEquals(2, iteratePositionFromEnd(5, 5))
    }
}

class EmaBooleanAndStringExtensionsTest {

    @Test
    fun `ifTrue and ifFalse run the action only for their value`() {
        var calls = 0
        assertTrue(true.ifTrue { calls++ })
        assertFalse(false.ifTrue { calls++ })
        assertFalse(false.ifFalse { calls++ })
        assertTrue(true.ifFalse { calls++ })
        assertEquals(2, calls)
    }

    @Test
    fun `ifTrueLet and ifFalseLet return the result or null`() {
        assertEquals("yes", true.ifTrueLet { "yes" })
        assertNull(false.ifTrueLet { "yes" })
        assertEquals("no", false.ifFalseLet { "no" })
        assertNull(true.ifFalseLet { "no" })
    }

    @Test
    fun `string null checks`() {
        assertEquals("a", ("a" as String?).checkNull())
        assertEquals("", (null as String?).checkNull())
        assertEquals("x", (null as String?).checkNull("x"))
        assertEquals("x", ("" as String?).checkNullOrEmpty("x"))
        assertEquals("x", (null as String?).checkNullOrEmpty("x"))
        assertEquals("a", ("a" as String?).checkNullOrEmpty("x"))
    }

    @Test
    fun `replaceLast replaces the last delimiter`() {
        assertEquals("1.000,50", "1.000.50".replaceLast('.', ","))
    }

    @Test
    fun `toEmaText keeps the arguments`() {
        assertEquals(EmaText.text("Hi %s"), "Hi %s".toEmaText())
        assertEquals(EmaText.text("Hi %s", "Ana"), "Hi %s".toEmaText("Ana"))
    }

    @Test
    fun `regex helpers`() {
        assertEquals("12,5", emaRegexGetFloatValue.find("Price 12,5 €")?.value)
        assertEquals("a", emaRegexGetCharacterValue.find("1a2")?.value)
    }

    @Test
    fun `uri resources`() {
        assertEquals(EmaUriRes("icon", EmaUriType.Drawable), "icon".toUriRes(EmaUriType.Drawable))
    }
}

class EmaListExtensionsTest {

    private val list = listOf(1, 2, 3, 4, 5)

    @Test
    fun `update by criteria replaces the first match`() {
        assertEquals(listOf(1, 20, 3, 4, 5), list.update({ it == 2 }) { this * 10 })
        assertEquals(list, list.update({ it == 9 }) { this * 10 })
    }

    @Test
    fun `update by index replaces the item`() {
        assertEquals(listOf(1, 2, 30, 4, 5), list.update(2) { this * 10 })
        assertEquals(list, list.update(-1) { this * 10 })
    }

    @Test
    fun `forEachFirstLast iterates alternating both ends with the original index`() {
        val visited = mutableListOf<Pair<Int, Int>>()
        list.forEachFirstLast { index, value -> visited.add(index to value) }
        assertEquals(listOf(0 to 1, 4 to 5, 1 to 2, 3 to 4, 2 to 3), visited)
    }

    @Test
    fun `mapEachFirstLast maps alternating both ends`() {
        assertEquals(listOf("0:1", "4:5", "1:2", "3:4", "2:3"), list.mapEachFirstLast { i, v -> "$i:$v" })
    }

    @Test
    fun `mapFirstLast maps alternating both ends`() {
        assertEquals(listOf(10, 50, 20, 40, 30), list.mapFirstLast { it * 10 })
        assertEquals(emptyList(), emptyList<Int>().mapFirstLast { it })
    }
}

class EmaPermissionExtensionsTest {

    @Test
    fun `boolean permission maps`() {
        val permissions = mapOf("a" to true, "b" to false)
        assertTrue(permissions.isPermissionGranted("a"))
        assertFalse(permissions.isPermissionGranted("b"))
        assertFalse(permissions.isPermissionGranted("c"))
        assertFalse(permissions.areAllPermissionsGranted())
        assertTrue(mapOf("a" to true).areAllPermissionsGranted())
        assertFalse(emptyMap<String, Boolean>().areAllPermissionsGranted())
    }

    @Test
    fun `state permission maps`() {
        val permissions = mapOf("a" to PermissionState.GRANTED, "b" to PermissionState.NOT_GRANTED)
        assertTrue(permissions.isPermissionStateGranted("a"))
        assertFalse(permissions.isPermissionStateGranted("b"))
        assertFalse(permissions.areAllPermissionsStateGranted())
        assertTrue(mapOf("a" to PermissionState.GRANTED).areAllPermissionsStateGranted())
        assertFalse(emptyMap<String, PermissionState>().areAllPermissionsStateGranted())
    }
}

class EmaResultExtensionsTest {

    @Test
    fun `kotlin result is converted to EmaResult`() {
        assertEquals(1, Result.success(1).toEmaResult { "error" }.getOrNull())
        assertEquals(
            "boom",
            Result.failure<Int>(IllegalStateException("boom")).toEmaResult { it.message }.getFailureOrNull()
        )
    }

    @Test
    fun `coroutine context to scope`() {
        assertEquals(
            Dispatchers.Default,
            Dispatchers.Default.toScope().coroutineContext[kotlin.coroutines.ContinuationInterceptor]
        )
    }
}
