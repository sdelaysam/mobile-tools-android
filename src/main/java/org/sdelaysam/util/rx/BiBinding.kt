package org.sdelaysam.util.rx

import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding3.widget.textChanges
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.sdelaysam.util.OpenForTesting

/**
 * Created on 7/5/20.
 * @author sdelaysam
 */

@OpenForTesting
class TextBiBinding(relay: BehaviorRelay<String>): BiBinding<String, TextView>(relay) {

    override fun observeView(view: TextView): Observable<String> {
        return view.textChanges()
            .skipInitialValue()
            .map { it.toString() }
    }

    override fun setValue(view: TextView, value: String?) {
        if (value?.contentEquals(view.text) != true) {
            view.text = value
        }
    }
}

abstract class BiBinding<T, V: View>(private val relay: BehaviorRelay<T>) {

    private var valid = true

    private lateinit var view: V

    fun bind(view: V): Disposable {
        this.view = view
        val disposables = CompositeDisposable()
        observeView(view)
            .distinctUntilChanged()
            .subscribe(relay)
            .addTo(disposables)
        relay.distinctUntilChanged()
            .observeOn(RxSchedulers.main)
            .filter { valid }
            .doAfterTerminate { view.removeCallbacks(applyValue) }
            .subscribe {
                valid = false
                view.post(applyValue)
            }
            .addTo(disposables)
        return disposables
    }

    private val applyValue = Runnable {
        setValue(view, relay.value)
        valid = true
    }

    abstract fun observeView(view: V): Observable<T>

    abstract fun setValue(view: V, value: T?)

}