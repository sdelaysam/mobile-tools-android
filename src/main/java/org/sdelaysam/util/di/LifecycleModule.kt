package org.sdelaysam.util.di

import android.app.Application
import org.koin.dsl.module
import org.sdelaysam.util.lifecycle.AppStateProvider
import org.sdelaysam.util.conductor.ConductorRouterProvider
import org.sdelaysam.util.lifecycle.DefaultApplicationLifecycle

/**
 * Created on 4/17/20.
 * @author sdelaysam
 */

var lifecycleModule = module {

    val lifecycle = DefaultApplicationLifecycle()

    single<Application.ActivityLifecycleCallbacks> { lifecycle }
    single<AppStateProvider> { lifecycle }
    single<ConductorRouterProvider> { lifecycle }
}