package org.sdelaysam.util.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bluelinelabs.conductor.Router
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.sdelaysam.util.conductor.ConductorRouterOwner
import org.sdelaysam.util.conductor.ConductorRouterProvider
import java.lang.ref.WeakReference

/**
 * Created on 4/17/20.
 * @author sdelaysam
 */

enum class AppState {
    Background,
    Foreground
}

interface AppStateProvider {
    fun observeStatus(): Observable<AppState>
}

class DefaultApplicationLifecycle: Application.ActivityLifecycleCallbacks,
    AppStateProvider,
    ConductorRouterProvider {

    private var routerOwner = WeakReference<ConductorRouterOwner>(null)

    private val stateSubject = BehaviorRelay.createDefault(AppState.Background)

    private var numResumed = 0

    override val currentRouter: Router?
        get() = routerOwner.get()?.router

    override fun observeStatus(): Observable<AppState> {
        return stateSubject.distinctUntilChanged()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is ConductorRouterOwner) {
            routerOwner = WeakReference(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is ConductorRouterOwner) {
            routerOwner = WeakReference(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (numResumed++ == 0) {
            stateSubject.accept(AppState.Foreground)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        numResumed--
    }

    override fun onActivityStopped(activity: Activity) {
        if (numResumed == 0) {
            stateSubject.accept(AppState.Background)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (routerOwner.get() == activity) {
            routerOwner.clear()
        }
    }
}