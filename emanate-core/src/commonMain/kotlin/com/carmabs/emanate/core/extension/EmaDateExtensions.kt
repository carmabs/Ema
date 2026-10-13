package com.carmabs.emanate.core.extension

import com.carmabs.emanate.core.constants.LONG_ZERO
import com.carmabs.emanate.core.constants.STRING_EMPTY
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.format.format
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Common formats, to use with the date extensions of this file, which are based on kotlinx-datetime and available on
 * every platform.
 *
 * The formats are [DateTimeFormat]s of [DateTimeComponents], so the same format works with epoch milliseconds, texts,
 * dates and times. Use these ones or build your own with the DSL of kotlinx-datetime, for example
 * `DateTimeComponents.Format { day(); char('-'); monthNumber(); char('-'); year() }`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaDateFormat {

    /**
     * 07/10/2026 09:30
     */
    val DDMMYYYY_HHMM: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        day()
        char('/')
        monthNumber()
        char('/')
        year()
        char(' ')
        hour()
        char(':')
        minute()
    }

    /**
     * 07/10/2026
     */
    val DDMMYYYY: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        day()
        char('/')
        monthNumber()
        char('/')
        year()
    }

    /**
     * 2026/10/07
     */
    val YYYYMMDD: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        year()
        char('/')
        monthNumber()
        char('/')
        day()
    }

    /**
     * 10/07/2026
     */
    val MMDDYYYY: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        monthNumber()
        char('/')
        day()
        char('/')
        year()
    }

    /**
     * 2026-10-07T09:30:00.000+02:00, or with Z when the offset is zero
     */
    val ISO8601: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        date(LocalDate.Formats.ISO)
        char('T')
        hour()
        char(':')
        minute()
        char(':')
        second()
        char('.')
        secondFraction(3)
        offset(UtcOffset.Formats.ISO)
    }

    /**
     * 09:30
     */
    val HHMM: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        hour()
        char(':')
        minute()
    }

    /**
     * 09:30:05
     */
    val HHMMSS: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        hour()
        char(':')
        minute()
        char(':')
        second()
    }
}

/**
 * Parses the date and returns its epoch milliseconds, or 0 if it cannot be parsed. When the text has no
 * time it is considered the start of the day, and when it has no offset, [timeZone] is used.
 */
fun String.toTimeStamp(
    format: DateTimeFormat<DateTimeComponents>,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Long = try {
    val components = format.parse(this)
    val date = components.toLocalDate()
    val time = runCatching { components.toLocalTime() }.getOrDefault(LocalTime(0, 0))
    val dateTime = LocalDateTime(date, time)
    val offset = runCatching { components.toUtcOffset() }.getOrNull()
    (offset?.let { dateTime.toInstant(it) } ?: dateTime.toInstant(timeZone)).toEpochMilliseconds()
} catch (e: Exception) {
    LONG_ZERO
}

/**
 * Converts the epoch milliseconds into the date and time of [timeZone].
 */
fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)

/**
 * Converts the date and time, read in [timeZone], into epoch milliseconds.
 */
fun LocalDateTime.toEpochMilli(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    toInstant(timeZone).toEpochMilliseconds()

/**
 * Formats the epoch milliseconds as an ISO 8601 date with the offset of [timeZone], for example
 * 2026-10-12T09:30:00.000+02:00.
 */
fun Long.toISO8601(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    toDateFormat(EmaDateFormat.ISO8601, timeZone)

/**
 * Formats the epoch milliseconds with the date, time and offset of [timeZone].
 */
fun Long.toDateFormat(
    format: DateTimeFormat<DateTimeComponents>,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val instant = Instant.fromEpochMilliseconds(this)
    return format.format { setDateTimeOffset(instant, timeZone.offsetAt(instant)) }
}

/**
 * Formats the date, or returns an empty string if the format needs more than the date, like the time.
 */
fun LocalDate.toDateFormat(format: DateTimeFormat<DateTimeComponents>): String = try {
    format.format { setDate(this@toDateFormat) }
} catch (e: IllegalStateException) {
    STRING_EMPTY
}

/**
 * Formats the time, or returns an empty string if the format needs more than the time, like the date.
 */
fun LocalTime.toHourFormat(format: DateTimeFormat<DateTimeComponents>): String = try {
    format.format { setTime(this@toHourFormat) }
} catch (e: IllegalStateException) {
    STRING_EMPTY
}
