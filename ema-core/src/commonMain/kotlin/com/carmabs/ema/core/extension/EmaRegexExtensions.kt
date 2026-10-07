package com.carmabs.ema.core.extension

/**
 * Created by Carlos Mateo Benito on 18/9/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Get float value from string, with comma or dot separator
 */
val emaRegexGetFloatValue = Regex("\\d{1,2}[,.]\\d{1,2}")

/**
 * Get character value from string, removing all digits
 */
val emaRegexGetCharacterValue = Regex("[\\D]")