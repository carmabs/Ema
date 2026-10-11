package com.carmabs.emanate.core.broadcast

import com.carmabs.emanate.core.extension.emaName
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 6/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
val <T : EmaViewModelContract<*, *>> KClass<T>.backBroadcastId: BackBroadcastId
    get() = BackBroadcastId("broadcast/${this.emaName}")

@JvmInline
value class BackBroadcastId internal constructor(val id: String)
