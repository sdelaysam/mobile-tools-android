package org.sdelaysam.util.lifecycle

import android.view.View
import com.bluelinelabs.conductor.Controller
import org.sdelaysam.util.viewmodel.BaseViewModel

/**
 * Created on 7/3/20.
 * @author sdelaysam
 */

interface LifecycleMonitor: Controller.LifecycleListener {
    fun onActivityResumed()
    fun onActivityPaused()
}

class ViewModelLifecycleMonitor(private val viewModel: BaseViewModel):
    LifecycleMonitor {

    private var isResumed = false

    private var isAttached = false

    override fun preAttach(controller: Controller, view: View) {
        isAttached = true
        if (isResumed) {
            viewModel.resume()
        }
    }

    override fun postDetach(controller: Controller, view: View) {
        isAttached = false
        viewModel.pause()
    }

    override fun postDestroy(controller: Controller) {
        viewModel.destroy()
    }

    override fun onActivityResumed() {
        isResumed = true
        if (isAttached) {
            viewModel.resume()
        }
    }

    override fun onActivityPaused() {
        isResumed = false
        viewModel.pause()
    }
}