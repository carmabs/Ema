package com.carmabs.emanate.core.source.middleware

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.logging.toStringPretty
import com.carmabs.emanate.core.state.EmaState
import kotlin.math.absoluteValue

/**
 * Logs every action with the state before and after its reducers. Add it as the last middleware: the reducers run as
 * soon as it calls next, so the state after the action is the one they return, even if a previous middleware has
 * continued the action later with nextAfter. The actions that a middleware stops don't reach it.
 *
 * @param tag of the logs
 */
fun loggerMiddleware(tag: String, logger: EmaLogger): EmaMiddleware<EmaState, Nothing, EmaAction> =
    LoggerEmaMiddleware(tag, logger)

/**
 * Logs in one line every action that enters the source. Added as the first middleware, together with
 * [loggerMiddleware] as the last one, it shows the actions that a middleware stops or changes.
 */
internal fun receivedActionLoggerMiddleware(
    tag: String,
    logger: EmaLogger
): EmaMiddleware<EmaState, Nothing, EmaAction> = middlewareOf(EmaAction::class) { action ->
    logger.log(tag, "Received action: $action")
    next()
}

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal class LoggerEmaMiddleware(
    private val tag: String,
    private val logger: EmaLogger,
    private val lineLength: Int = 80
) : EmaMiddleware<EmaState, Nothing, EmaAction> {

    override val actionType = EmaAction::class

    override fun MiddlewareScope<EmaState, Nothing>.invoke(action: EmaAction) {
        val padding = "    "
        val info: (String) -> Unit = { line -> logger.log(tag, line) }
        run {
            info("")
            info(printLine('*', "STARTING EMA ACTION LOGGING", textPadding = padding))
            info(printLine('*', "", sideCharacterLimit = 2))
            info(printLine('|', "Dispatched action", textPadding = padding))
            info(printLine('|', "", sideCharacterLimit = 2))
            action.toStringPretty().lines().forEach {
                info(
                    printLine(
                        '|',
                        it,
                        sideCharacterLimit = 2,
                        alignment = Alignment.Start,
                        textPadding = padding
                    )
                )
            }
            info(printLine('|', "State before apply action", textPadding = padding))
            info(printLine('|', "", sideCharacterLimit = 2))

            state.toStringPretty().lines().forEach {
                info(
                    printLine(
                        '|',
                        it,
                        sideCharacterLimit = 2,
                        alignment = Alignment.Start,
                        textPadding = padding
                    )
                )
            }

            next()
            info(printLine('|', "State after apply action", textPadding = padding))
            info(printLine('|', "", sideCharacterLimit = 2))
            state.toStringPretty().lines().forEach {
                info(
                    printLine(
                        '|',
                        it,
                        sideCharacterLimit = 2,
                        alignment = Alignment.Start,
                        textPadding = padding
                    )
                )
            }
            info(printLine('|', ""))
            info(printLine('*', "", sideCharacterLimit = 2))
            info(printLine('*', "FINISHED EMA ACTION LOGGING", textPadding = padding))
            info("")
        }
    }

    private fun printLine(
        character: Char,
        text: String,
        length: Int = lineLength,
        sideCharacterLimit: Int? = null,
        alignment: Alignment = Alignment.Center,
        textPadding: String = ""
    ): String {
        when (alignment) {
            Alignment.Start -> {
                val sideStart = sideCharacterLimit?.let {
                    character.toString().repeat(it)
                } ?: ""
                val endLength =
                    (length - sideStart.length - text.length - textPadding.length * 2).coerceAtLeast(
                        0
                    )
                val sideEnd = (
                    sideCharacterLimit?.let {
                        character.toString().repeat(it) + " ".repeat(
                            (endLength - it).coerceAtLeast(0)
                        )
                    } ?: character.toString()
                        .repeat(endLength)

                    ).reversed()
                return sideStart + textPadding + text + textPadding + sideEnd
            }

            Alignment.Center -> {
                val sideLength = (length - textPadding.length * 2 - text.length) / 2
                val sideString = (
                    sideCharacterLimit?.let {
                        character.toString().repeat(it) + " ".repeat(
                            (sideLength - it).coerceAtLeast(0)
                        )
                    } ?: character.toString().repeat(sideLength)
                    )
                return (sideString + textPadding + text + textPadding + sideString.reversed()).let {
                    val remainingCharacters = (lineLength - it.length)
                    when {
                        remainingCharacters > 0 -> {
                            it + character.toString().repeat(remainingCharacters)
                        }

                        remainingCharacters < 0 -> {
                            it.dropLast(remainingCharacters.absoluteValue)
                        }

                        else -> {
                            it
                        }
                    }
                }
            }

            Alignment.End -> {
                val sideEnd = (
                    sideCharacterLimit?.let {
                        character.toString().repeat(it)
                    } ?: ""
                    )
                val startLength =
                    (length - sideEnd.length - text.length - textPadding.length * 2).coerceAtLeast(
                        0
                    )
                val sideStart = (
                    sideCharacterLimit?.let {
                        character.toString().repeat(it) + " ".repeat(
                            startLength - it
                        )
                    } ?: character.toString().repeat(startLength)
                    )
                return sideStart + textPadding + text + textPadding + sideEnd
            }
        }
    }

    private enum class Alignment {
        Start,
        Center,
        End
    }
}
