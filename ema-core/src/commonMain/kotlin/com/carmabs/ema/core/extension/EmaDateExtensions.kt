@file:OptIn(FormatStringsInDatetimeFormats::class)

package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.LONG_ZERO
import com.carmabs.ema.core.constants.STRING_EMPTY
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.format
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Date extensions based on kotlinx-datetime, available on every platform.
 *
 * Formats use the Unicode pattern syntax (the same as java.time). Locale dependent directives, like month
 * or day names, are not supported.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

const val DATE_FORMAT_DDMMYYYY_HHMM = "dd/MM/yyyy HH:mm"
const val DATE_FORMAT_DDMMYYYY = "dd/MM/yyyy"
const val DATE_FORMAT_YYYYMMDD = "yyyy/MM/dd"
const val DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy"
const val DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
const val HOUR_FORMAT_HHMM = "HH:mm"
const val HOUR_FORMAT_HHMMSS = "HH:mm:ss"

/**
 * Parses the date and returns its epoch milliseconds, or 0 if it cannot be parsed. When the text has no
 * time it is considered the start of the day, and when it has no offset, [timeZone] is used.
 */
fun String.toTimeStamp(dateFormat: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = try {
    val components = DateTimeComponents.Format { byUnicodePattern(dateFormat) }.parse(this)
    val date = components.toLocalDate()
    val time = runCatching { components.toLocalTime() }.getOrDefault(LocalTime(0, 0))
    val dateTime = LocalDateTime(date, time)
    val offset = runCatching { components.toUtcOffset() }.getOrNull()
    (offset?.let { dateTime.toInstant(it) } ?: dateTime.toInstant(timeZone)).toEpochMilliseconds()
} catch (e: Exception) {
    LONG_ZERO
}

fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)

fun LocalDateTime.toEpochMilli(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    toInstant(timeZone).toEpochMilliseconds()

fun Long.toISO8601(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    toDateFormat(DATE_FORMAT_ISO8601, timeZone)

/**
 * Formats the epoch milliseconds, or returns an empty string if the format is not valid.
 */
fun Long.toDateFormat(dateFormat: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): String = try {
    val instant = Instant.fromEpochMilliseconds(this)
    DateTimeComponents.Format { byUnicodePattern(dateFormat) }.format {
        setDateTimeOffset(instant, timeZone.offsetAt(instant))
    }
} catch (e: Exception) {
    STRING_EMPTY
}

fun LocalDate.toDateFormat(dateFormat: String): String = try {
    LocalDate.Format { byUnicodePattern(dateFormat) }.format(this)
} catch (e: Exception) {
    STRING_EMPTY
}

fun LocalTime.toHourFormat(hourFormat: String): String = try {
    LocalTime.Format { byUnicodePattern(hourFormat) }.format(this)
} catch (e: Exception) {
    STRING_EMPTY
}
