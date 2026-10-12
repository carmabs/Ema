package com.carmabs.emanate.core.extension

/*
 * Created by Carlos Mateo Benito on 18/9/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Finds a decimal number of one or two digits on each side, with comma or dot as separator. For example 3,5 or 12.25
 */
val emaRegexGetFloatValue = Regex("\\d{1,2}[,.]\\d{1,2}")

/**
 * Finds any character that is not a digit
 */
val emaRegexGetCharacterValue = Regex("[\\D]")
