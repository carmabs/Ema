package com.carmabs.ema.android

import android.content.BroadcastReceiver
import org.koin.core.component.KoinComponent


/**
 * Created by Carlos Mateo Benito on 17/12/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
/**
 * Broadcast with dependency injector included
 */
abstract class EmaBroadcastReceiver : BroadcastReceiver(), KoinComponent
