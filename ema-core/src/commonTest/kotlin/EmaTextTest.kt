import com.carmabs.ema.core.model.EmaText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EmaTextTest {

    @Test
    fun `id texts with the same content are equal`() {
        assertEquals(EmaText.id(1, "a", 2), EmaText.id(1, "a", 2))
        assertEquals(EmaText.id(1, "a", 2).hashCode(), EmaText.id(1, "a", 2).hashCode())
    }

    @Test
    fun `id texts with different content are different`() {
        assertNotEquals(EmaText.id(1, "a"), EmaText.id(2, "a"))
        assertNotEquals(EmaText.id(1, "a"), EmaText.id(1, "b"))
        assertNotEquals(EmaText.id(1), EmaText.id(1, "a"))
    }

    @Test
    fun `literal texts compare text and arguments`() {
        assertEquals(EmaText.text("hello %s", "Ana"), EmaText.text("hello %s", "Ana"))
        assertNotEquals(EmaText.text("hello %s", "Ana"), EmaText.text("hello %s", "Eva"))
        assertEquals(EmaText.empty(), EmaText.text(""))
        assertTrue(EmaText.empty().isEmpty())
    }

    @Test
    fun `plural and composition compare their content`() {
        assertEquals(EmaText.plural(3, 2, "x"), EmaText.plural(3, 2, "x"))
        assertNotEquals(EmaText.plural(3, 2), EmaText.plural(3, 5))
        assertEquals(
            EmaText.composition(EmaText.id(1), EmaText.text(" ")),
            EmaText.composition(EmaText.id(1), EmaText.text(" "))
        )
    }

    @Test
    fun `texts of different type are never equal`() {
        assertNotEquals<EmaText>(EmaText.id(1), EmaText.plural(1, 1))
        assertNotEquals<EmaText>(EmaText.text("1"), EmaText.id(1))
    }

    @Test
    fun `toString shows the content`() {
        assertEquals("Id(id=1, data=[a, 2])", EmaText.id(1, "a", 2).toString())
        assertEquals("Text(text=hi, data=[])", EmaText.text("hi").toString())
    }
}
