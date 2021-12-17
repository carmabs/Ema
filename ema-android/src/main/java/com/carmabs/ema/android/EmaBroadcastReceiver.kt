package com.carmabs.ema.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper
import com.carmabs.ema.core.concurrency.ConcurrencyManager
import com.carmabs.ema.core.concurrency.DefaultConcurrencyManager
import org.kodein.di.DI
import org.kodein.di.DIAware

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
abstract class EmaBroadcastReceiver : BroadcastReceiver(), DIAware {

    override lateinit var di: DI

    @CallSuper
    override fun onReceive(context: Context, intent: Intent) {
        di = (context.applicationContext as DIAware).di
    }
}
