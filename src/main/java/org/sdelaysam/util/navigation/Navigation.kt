package org.sdelaysam.util.navigation

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.sdelaysam.util.conductor.ConductorRouterProvider
import org.sdelaysam.util.rx.RxSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created on 4/13/20.
 * @author sdelaysam
 */

interface NavigationPoint

interface Navigatable {
    val origin: NavigationPoint
}

interface NavigationController: Navigatable {
    fun navigateTo(point: NavigationPoint, animated: Boolean): Boolean
}

interface Navigation {
    fun navigate(to: NavigationPoint, animated: Boolean)
    fun navigate(to: List<NavigationPoint>, animated: Boolean)
}

interface NavigationRouter: Navigation {
    fun checkActiveNavigation()
}

interface NavigationPathProvider {
    val currentPath: List<Navigatable?>?
}

class DefaultNavigationPathProvider(
    private val routerProvider: ConductorRouterProvider
): NavigationPathProvider {

    override val currentPath: List<Navigatable?>?
        get() {
            val router = routerProvider.currentRouter ?: return null
            val path = mutableListOf<Navigatable?>()
            var controller = router.topController
            while (controller != null) {
                path.add(controller as? Navigatable)
                controller = controller.topChildController
            }
            return path
        }
}

class DefaultNavigationRouter(
    private val pathProvider: NavigationPathProvider
): NavigationRouter {

    private val checkNavigationSubject = PublishRelay.create<Unit>()

    private val activeNavigation = LinkedList<NavigationPoint>()

    private var activeNavigationAnimated = false

    private val disposable: Disposable

    init {
        disposable = checkNavigationSubject
            .observeOn(RxSchedulers.main) // to prevent recursion
            .doOnNext { tryNavigate() }
            .switchMapSingle {
                Single.timer(navigationTimeoutMs, TimeUnit.MILLISECONDS)
                    .doOnSuccess { clearOnTimeout() }
            }
            .subscribe()
    }

    override fun navigate(to: NavigationPoint, animated: Boolean) {
        navigateTo(to, animated)
    }

    override fun navigate(to: List<NavigationPoint>, animated: Boolean) {
        if (activeNavigation.isNotEmpty()) {
            throw IllegalStateException("Navigate to new route is not allowed during active navigation phase")
        }
        val path = pathProvider.currentPath ?: return
        var pathIndex = 0
        var routeIndex = 0
        while (routeIndex < to.size && pathIndex < path.size) {
            val point = to[routeIndex]
            val node = path[pathIndex]
            if (node != null) {
                if (node.origin != point) {
                    break
                }
                routeIndex++
            }
            pathIndex++
        }
        val navigateCount = to.size - routeIndex

        if (navigateCount <= 0) {
            return
        }

        if (navigateCount == 1) {
            navigateTo(to[routeIndex], animated)
            return
        }

        for (i in routeIndex until to.size) {
            activeNavigation.add(to[i])
        }
        activeNavigationAnimated = animated
        checkNavigationSubject.accept(Unit)
    }

    override fun checkActiveNavigation() {
        if (activeNavigation.isNotEmpty()) {
            checkNavigationSubject.accept(Unit)
        }
    }

    private fun navigateTo(point: NavigationPoint, animated: Boolean): Boolean {
        val path = pathProvider.currentPath?.asReversed() ?: return false
        return path.any {
            (it as? NavigationController)?.navigateTo(point, animated) ?: false
        }
    }

    private fun tryNavigate() {
        if (activeNavigation.isNotEmpty()) {
            if (navigateTo(activeNavigation.first, activeNavigationAnimated)) {
                activeNavigation.removeFirst()
                if (activeNavigation.isNotEmpty()) {
                    checkNavigationSubject.accept(Unit)
                }
            }
        }
    }

    private fun clearOnTimeout() {
        activeNavigation.clear()
        activeNavigationAnimated = false
    }

    companion object {
        const val navigationTimeoutMs = 300L
    }
}