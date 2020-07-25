package org.sdelaysam.util.conductor

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.bluelinelabs.conductor.Controller
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import org.koin.core.context.KoinContextHandler
import org.sdelaysam.util.lifecycle.LifecycleMonitor
import org.sdelaysam.util.lifecycle.ViewModelLifecycleMonitor
import org.sdelaysam.util.navigation.Navigatable
import org.sdelaysam.util.navigation.NavigationPoint
import org.sdelaysam.util.navigation.NavigationRouter
import org.sdelaysam.util.rx.UiDisposable
import org.sdelaysam.util.viewmodel.BaseViewModel
import org.sdelaysam.util.viewmodel.ViewModel

/**
 * Created on 4/6/20.
 * @author sdelaysam
 */

abstract class BaseViewController(
    @LayoutRes private val layoutId: Int,
    args: Bundle? = null
) : Controller(args), LayoutContainer,
    Navigatable, UiDisposable {

    override var containerView: View? = null

    override val disposable = CompositeDisposable()

    private val router: NavigationRouter by inject()

    private val lifecycleMonitors = mutableListOf<LifecycleMonitor>()

    init {
        Log.d("WTF", "${this.javaClass.simpleName} Init")
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(layoutId, container, false)
        containerView = view
        Log.d("WTF", "${this.javaClass.simpleName} Create View")
        onViewCreated(view, savedViewState)
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        router.checkActiveNavigation()
    }

    final override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        Log.d("WTF", "${this.javaClass.simpleName} Destroy View")
        onViewDestroyed(view)
        disposable.clear()
        clearFindViewByIdCache()
        containerView = null
    }

    final override fun onDestroy() {
        super.onDestroy()
        Log.d("WTF", "${this.javaClass.simpleName} Destroy")
        onDestroyed()
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        lifecycleMonitors.forEach { it.onActivityResumed() }
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        lifecycleMonitors.forEach { it.onActivityPaused() }
    }

    protected open fun onViewCreated(view: View, savedViewState: Bundle?) = Unit

    protected open fun onViewDestroyed(view: View) = Unit

    protected open fun onDestroyed() = Unit

    protected fun navigate(to: NavigationPoint, animated: Boolean = true) {
        router.navigate(to, animated)
    }

    protected fun navigate(to: List<NavigationPoint>, animated: Boolean = true) {
        router.navigate(to, animated)
    }

    protected inline fun <reified T : ViewModel> Controller.getViewModel() = lazy(LazyThreadSafetyMode.NONE) {
        val viewModel: T = KoinContextHandler.get().get<T>(null, null)
        if (viewModel is BaseViewModel) {
            val monitor =
                ViewModelLifecycleMonitor(viewModel)
            addLifecycleMonitor(monitor)
            val isResumed = activity?.window?.decorView?.isShown ?: false
            if (isResumed && isAttached) {
                viewModel.onResume()
            }
        }
        viewModel
    }

    fun addLifecycleMonitor(lifecycleMonitor: LifecycleMonitor) {
        lifecycleMonitors += lifecycleMonitor
    }

    fun removeLifecycleMonitor(lifecycleMonitor: LifecycleMonitor) {
        lifecycleMonitors -= lifecycleMonitor
    }

    override fun dispose() {
        disposable.clear()
    }
}

private inline fun <reified T : Any> inject() = lazy(LazyThreadSafetyMode.NONE) {
    KoinContextHandler.get().get<T>(null, null)
}
