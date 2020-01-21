package com.carmabs.ema.android.extra

import android.content.Intent

/**
 * Created by Carlos Mateo Benito on 2019-12-26.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaActivityResult(
        val requestCode: Int,
        val resultCode: Int,
        val data: Intent?
)