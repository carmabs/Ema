package com.carmabs.emanate.core.extension

import com.carmabs.emanate.core.model.EmaText

/*
 * Created by Carlos Mateo Benito on 23/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Converts the string into an [EmaText] with this text.
 */
fun String.toEmaText(): EmaText = EmaText.text(this)

/**
 * Converts the string into an [EmaText] with this text as format and [data] as its arguments.
 */
fun String.toEmaText(vararg data: Any): EmaText = EmaText.text(this, *data)
