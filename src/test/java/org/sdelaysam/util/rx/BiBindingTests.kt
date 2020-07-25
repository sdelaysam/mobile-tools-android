package org.sdelaysam.util.rx

import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.sdelaysam.util.TestActivity

/**
 * Created on 7/8/20.
 * @author sdelaysam
 */

@RunWith(AndroidJUnit4::class)
class BiBindingTests {

    lateinit var scenario: ActivityScenario<TestActivity>

    private val disposables = CompositeDisposable()

    @Before
    fun setup() {
        RxSchedulers.main = Schedulers.trampoline()
        RxSchedulers.network = Schedulers.trampoline()
        RxSchedulers.timer = Schedulers.trampoline()
        scenario = launch(TestActivity::class.java)
    }

    @After
    fun teardown() {
        disposables.clear()
    }

    @Test
    fun test() {
        onTextView {

            val subject = BehaviorRelay.create<String>()
            val observer = TestObserver.create<String>()
            subject.subscribe(observer)

            TextBiBinding(subject).bind(it).addTo(disposables)

            observer.assertNoValues()
            assertTrue(it.text.isNullOrEmpty())

            subject.accept("Text")
            observer.assertValueCount(2) // one is from TextView, another is from direct subscription
            observer.assertValueAt(0, "Text")
            observer.assertValueAt(1, "Text")
            assertEquals("Text", it.text.toString())

            it.text = "Text1"
            observer.assertValueCount(3)
            observer.assertValueAt(2, "Text1")
        }
    }

    private fun onTextView(action: (TextView) -> Unit) {
        scenario.onActivity {
            val parent = it.requireViewById<ViewGroup>(android.R.id.content)
            val view = TextView(parent.context)
            parent.addView(view)
            action(view)
        }
    }

}