import com.carmabs.ema.core.extension.DATE_FORMAT_DDMMYYYY
import com.carmabs.ema.core.extension.DATE_FORMAT_DDMMYYYY_HHMM
import com.carmabs.ema.core.extension.DATE_FORMAT_ISO8601
import com.carmabs.ema.core.extension.DATE_FORMAT_YYYYMMDD
import com.carmabs.ema.core.extension.HOUR_FORMAT_HHMMSS
import com.carmabs.ema.core.extension.toDateFormat
import com.carmabs.ema.core.extension.toHourFormat
import com.carmabs.ema.core.extension.toISO8601
import com.carmabs.ema.core.extension.toTimeStamp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class EmaDateExtensionsTest {

    private val oneDay = 86_400_000L

    @Test
    fun `formats epoch milliseconds`() {
        assertEquals("02/01/1970 01:30", (oneDay + 5_400_000L).toDateFormat(DATE_FORMAT_DDMMYYYY_HHMM, TimeZone.UTC))
    }

    @Test
    fun `formats ISO 8601 with milliseconds and offset`() {
        assertEquals("1970-01-01T00:00:01.500Z", 1_500L.toISO8601(TimeZone.UTC))
    }

    @Test
    fun `parses a date with time`() {
        assertEquals(oneDay + 5_400_000L, "02/01/1970 01:30".toTimeStamp(DATE_FORMAT_DDMMYYYY_HHMM, TimeZone.UTC))
    }

    @Test
    fun `parses a date without time as the start of the day`() {
        assertEquals(oneDay, "02/01/1970".toTimeStamp(DATE_FORMAT_DDMMYYYY, TimeZone.UTC))
    }

    @Test
    fun `parses the offset of the text`() {
        assertEquals(0L, "1970-01-01T01:00:00.000+01:00".toTimeStamp(DATE_FORMAT_ISO8601))
    }

    @Test
    fun `invalid text is parsed as zero`() {
        assertEquals(0L, "not a date".toTimeStamp(DATE_FORMAT_DDMMYYYY))
    }

    @Test
    fun `formats local date and time`() {
        assertEquals("2026/10/07", LocalDate(2026, 10, 7).toDateFormat(DATE_FORMAT_YYYYMMDD))
        assertEquals("09:05:03", LocalTime(9, 5, 3).toHourFormat(HOUR_FORMAT_HHMMSS))
    }
}
