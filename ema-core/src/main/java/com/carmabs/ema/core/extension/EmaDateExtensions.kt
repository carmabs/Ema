package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.LONG_ZERO
import com.carmabs.ema.core.constants.STRING_EMPTY
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */

const val DATE_FORMAT_DDMMYYYY_HHMM = "dd/MM/yyyy HH:mm"
const val DATE_FORMAT_DDMMYYYY = "dd/MM/yyyy"
const val DATE_FORMAT_YYYYMMDD = "yyyy/MM/dd"
const val DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy"
const val DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
const val DATE_FORMAT_HHMM = "HH:mm"

/**
 * Convert a string with provided format to timestamp
 * @param dateFormat Format of the string
 */
fun String.toTimeStamp(dateFormat: String): Long = try {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    val date = formatter.parse(this)
    date.time
} catch (e: Exception) {
    LONG_ZERO
}

/**
 * Convert a long timestamp to date format
 */
fun Long.toDate() = Date(this)

/**
 * Converts a long timestamp to instant
 */
fun Long.toInstant(zoneId:ZoneId = ZoneId.systemDefault()): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(zoneId)

/**
 * Get timestamp from ZonedDateTime
 */
fun ZonedDateTime.getEpochMilli(): Long = toInstant().toEpochMilli()


/**
 * Convert a long timestamp to a string with provided format
 * @param dateFormat Format of the string
 * @param zoneId Zone to format the hour difference
 */
fun Long.toDateFormat(dateFormat: String, zoneId: ZoneId = ZoneId.systemDefault()): String = try {
        val formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
        toInstant(zoneId).format(formatter)
    } catch (e: java.lang.Exception) {
        STRING_EMPTY
    }