package org.sdelaysam.util.rx

import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

/**
 * Created on 4/13/20.
 * @author sdelaysam
 */

interface UiDisposable {
    val disposable: CompositeDisposable
    fun dispose()
}

interface UiPausable {
    val isPaused: Observable<Boolean>
}

fun Disposable.addTo(disposables: CompositeDisposable) {
    disposables.add(this)
}

fun <T> Observable<T>.bufferWhile(
    paused: Observable<Boolean>,
    bufferSize: Int? = 1
): Observable<T> {
    return lift(
        if (bufferSize == 1) {
            BufferSingleValueWhileIdleOperator(paused)
        } else {
            BufferWhileIdleOperator(paused, bufferSize)
        }
    )
}

fun Observable<Boolean>.not(): Observable<Boolean> {
    return map { !it }
}

fun <T> UiDisposable.bind(observable: Observable<T>, consumer: Consumer<in T>) {
    observable.observeOn(RxSchedulers.main).subscribe(consumer).addTo(disposable)
}

fun <T, V: View> UiDisposable.bind(binding: BiBinding<T, V>, view: V) {
    binding.bind(view).addTo(disposable)
}

fun View.enabled(): Consumer<Boolean> {
    return Consumer { isEnabled = it }
}