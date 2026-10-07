import com.carmabs.ema.core.navigator.EmaNavigationNode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Carlos Mateo Benito on 9/7/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class NavigationNodeTest {

    enum class TestPages {
        PAGE_A,
        PAGE_B,
        PAGE_C
    }

    @Test
    fun `single top skip node`() {
        val node = EmaNavigationNode(TestPages.PAGE_A)
            .next(TestPages.PAGE_B)
            .next(TestPages.PAGE_C)
            .next(TestPages.PAGE_A)
            .next(TestPages.PAGE_B)
        val nodeSkipped = node.next(TestPages.PAGE_A, true)
        assertEquals(false, nodeSkipped.hasPreviousNodeValue(TestPages.PAGE_A))
    }
}
class NavigationNodeIdTest {

    @Test
    fun `nodes created at the same time have different ids`() {
        val ids = List(1_000) { EmaNavigationNode(it).id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
