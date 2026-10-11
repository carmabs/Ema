package com.carmabs.emanate.core.source

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Marks the scopes of an [EmaSource]. Inside a scope, the members of an outer scope can't be called implicitly, so
 * each block only reaches what belongs to it: a reducer can't add middlewares, a middleware can't add reducers and a
 * side effect can't launch other side effects. The view model isn't marked, so its properties (for example its use
 * cases) can be used in every scope.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class EmaDsl
