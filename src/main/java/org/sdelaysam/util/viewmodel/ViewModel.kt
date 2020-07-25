package org.sdelaysam.util.viewmodel

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.sdelaysam.util.rx.UiPausable

/**
 * Created on 4/13/20.
 * @author sdelaysam
 */

interface ViewModel

abstract class BaseViewModel: UiPausable,
    ViewModel {

    protected val disposables = CompositeDisposable()

    override val isPaused = BehaviorRelay.createDefault(true)

    open fun onResume() {}

    open fun onPause() {}

    open fun onDestroy() {}

    internal fun resume() {
        isPaused.accept(false)
        onResume()
    }

    internal fun pause() {
        isPaused.accept(true)
        onPause()
    }

    internal fun destroy() {
        onDestroy()
        disposables.dispose()
    }

    protected fun Disposable.untilDestroy() = disposables.add(this)
}