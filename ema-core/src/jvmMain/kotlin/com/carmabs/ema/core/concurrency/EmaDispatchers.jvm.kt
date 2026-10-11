package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual fun defaultIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
