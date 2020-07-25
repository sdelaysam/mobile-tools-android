package org.sdelaysam.util.rx

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

/**
 * Created on 6/8/20.
 * @author sdelaysam
 */

class ActivityIndicator : Observable<Boolean>() {

    private val subject: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

    private val loading = subject
        .onErrorReturnItem(0)
        .map { it > 0 }
        .distinctUntilChanged()

    private fun increment() {
        subject.onNext(subject.value!! + 1)
    }

    private fun decrement() {
        subject.onNext(maxOf(subject.value!! - 1, 0))
    }

    override fun subscribeActual(observer: Observer<in Boolean>?) {
        if (observer == null) {
            return
        }
        loading.subscribe(observer)
    }

    internal fun <T> trackObservable(source: Observable<T>): Observable<T> {
        return source
            .doOnSubscribe { increment() }
            .doFinally { decrement() }
    }

    internal fun <T> trackSingle(source: Single<T>): Single<T> {
        return source
            .doOnSubscribe { increment() }
            .doFinally { decrement() }
    }
}

fun <T> Observable<T>.trackActivity(activityIndicator: ActivityIndicator): Observable<T> = activityIndicator.trackObservable(this)

fun <T> Single<T>.trackActivity(activityIndicator: ActivityIndicator): Single<T> = activityIndicator.trackSingle(this)

