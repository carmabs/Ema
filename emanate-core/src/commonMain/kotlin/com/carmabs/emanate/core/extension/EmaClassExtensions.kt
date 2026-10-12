package com.carmabs.emanate.core.extension

import kotlin.reflect.KClass

/**
 * Name that identifies the class. On JVM and native platforms it is the fully qualified name; on JS, where
 * qualified names are not available, the simple name.
 */
internal val KClass<*>.qualifiedNameOrSimpleName: String
    // Without kotlin-reflect, the JVM appends " (Kotlin reflection is not available)" to the name
    get() = toString().removePrefix("class ").removePrefix("interface ").substringBefore(" (")
