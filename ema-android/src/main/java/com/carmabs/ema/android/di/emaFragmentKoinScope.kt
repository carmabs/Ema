package com.carmabs.ema.android.di

import androidx.fragment.app.Fragment
import org.koin.android.ext.android.getKoin
import org.koin.androidx.scope.LifecycleScopeDelegate
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.scope.getScopeOrNull
import org.koin.core.Koin
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName

/**
 * Created by Carlos Mateo Benito on 12/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun Fragment.emaFragmentKoinScope() = LifecycleScopeDelegate<Fragment>(this,this.getKoin()){ koin: Koin ->
    val scope = koin.createScope(getScopeId(), getScopeName(),this)
    val activityScope = activity?.getScopeOrNull()
    activityScope?.let { scope.linkTo(it) }
    scope
}