package com.carmabs.emanate.serialization

import com.carmabs.emanate.core.savestate.EmaSavedCodec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * Saves a value of type [T] in JSON with kotlinx.serialization. A value that can't be decoded, for example one saved
 * by a previous version of the app with other fields, isn't restored.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaJsonCodec<T>(private val serializer: KSerializer<T>, private val format: Json = Json) : EmaSavedCodec<T> {

    override fun encode(value: T): String = format.encodeToString(serializer, value)

    override fun decode(value: String): T? = try {
        format.decodeFromString(serializer, value)
    } catch (exception: IllegalArgumentException) {
        // A SerializationException, or an invalid value for the type
        null
    }
}

/**
 * Saves a value of type [T], a class annotated with Serializable or a basic type, in JSON:
 * `add("form", json(), save = { LoginForm(userName, rememberMe) }, restore = { ... })`. Inside add, the type is
 * inferred from the value returned by save; elsewhere write it: `json<LoginForm>()`.
 *
 * @param format encodes and decodes the value. By default the one of kotlinx.serialization.
 */
inline fun <reified T> json(format: Json = Json): EmaSavedCodec<T> = EmaJsonCodec(serializer<T>(), format)

/**
 * Saves a value of type [T] in JSON with [serializer], for the types whose serializer can't be found from the type,
 * like the ones with a custom serializer.
 *
 * @param format encodes and decodes the value. By default the one of kotlinx.serialization.
 */
fun <T> json(serializer: KSerializer<T>, format: Json = Json): EmaSavedCodec<T> = EmaJsonCodec(serializer, format)
