import com.carmabs.emanate.core.extension.EmaDateFormat
import com.carmabs.emanate.core.extension.toDateFormat
import com.carmabs.emanate.core.extension.toHourFormat
import com.carmabs.emanate.core.extension.toISO8601
import com.carmabs.emanate.core.extension.toTimeStamp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char

class EmaDateExtensionsTest {

    private val oneDay = 86_400_000L

    @Test
    fun `formats epoch milliseconds`() {
        assertEquals("02/01/1970 01:30", (oneDay + 5_400_000L).toDateFormat(EmaDateFormat.DDMMYYYY_HHMM, TimeZone.UTC))
    }

    @Test
    fun `formats ISO 8601 with milliseconds and offset`() {
        assertEquals("1970-01-01T00:00:01.500Z", 1_500L.toISO8601(TimeZone.UTC))
    }

    @Test
    fun `parses a date with time`() {
        assertEquals(oneDay + 5_400_000L, "02/01/1970 01:30".toTimeStamp(EmaDateFormat.DDMMYYYY_HHMM, TimeZone.UTC))
    }

    @Test
    fun `parses a date without time as the start of the day`() {
        assertEquals(oneDay, "02/01/1970".toTimeStamp(EmaDateFormat.DDMMYYYY, TimeZone.UTC))
    }

    @Test
    fun `parses the offset of the text`() {
        assertEquals(0L, "1970-01-01T01:00:00.000+01:00".toTimeStamp(EmaDateFormat.ISO8601))
    }

    @Test
    fun `invalid text is parsed as zero`() {
        assertEquals(0L, "not a date".toTimeStamp(EmaDateFormat.DDMMYYYY))
    }

    @Test
    fun `formats local date and time`() {
        assertEquals("2026/10/07", LocalDate(2026, 10, 7).toDateFormat(EmaDateFormat.YYYYMMDD))
        assertEquals("09:05:03", LocalTime(9, 5, 3).toHourFormat(EmaDateFormat.HHMMSS))
    }

    @Test
    fun `a format that needs more than the date or the time returns an empty string`() {
        assertEquals("", LocalDate(2026, 10, 7).toDateFormat(EmaDateFormat.DDMMYYYY_HHMM))
        assertEquals("", LocalTime(9, 5, 3).toHourFormat(EmaDateFormat.DDMMYYYY))
    }

    @Test
    fun `a custom format built with the DSL`() {
        val format = DateTimeComponents.Format {
            day()
            char('-')
            monthNumber()
        }
        assertEquals("02-01", oneDay.toDateFormat(format, TimeZone.UTC))
    }
}
