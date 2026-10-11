package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual fun defaultIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
