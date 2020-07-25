package org.sdelaysam.util.conductor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Created on 4/17/20.
 * @author sdelaysam
 */

abstract class BaseTabBarController(
    @LayoutRes private val layoutId: Int,
    args: Bundle? = null
): BaseViewController(layoutId, args) {

    abstract val tabsContainer: ViewGroup?

    abstract val bottomNavigation: BottomNavigationView?

    abstract fun getItemId(controller: Controller): Int

    abstract fun createTabController(itemId: Int): Controller

    private var exitOnFirstTag = true

    private var useNavigationListener = true

    override fun onViewCreated(view: View, savedViewState: Bundle?) {
        super.onViewCreated(view, savedViewState)
        bottomNavigation?.setOnNavigationItemSelectedListener {
            if (useNavigationListener) {
                showTab(it)
            }
            true
        }
        val router = tabsRouter ?: return
        if (!router.hasRootController()) {
            bottomNavigation?.menu?.selectedItem?.let {
                showTab(it)
            }
        }
    }

    fun notifyMenuChanged() {
        val router = tabsRouter ?: return
        val menu = bottomNavigation?.menu ?: return

        if (menu.size() == 0) {
            router.setBackstack(emptyList(), null)
            return
        }

        val set = HashSet<String>()
        for (i in 0 until menu.size()) {
            set.add(menu.getItem(i).tag)
        }

        val backstack = router.backstack
        val iterator = backstack.asReversed().listIterator()
        while (iterator.hasNext()) {
            val transaction = iterator.next()
            if (!set.contains(transaction.tag())) {
                iterator.remove()
            }
        }

        if (backstack.isEmpty()) {
            val item = menu.getItem(0)
            val controller = createTabController(item.itemId)
            updateSelectedTab(controller)
            router.setRoot(RouterTransaction.with(controller).tag(item.tag))
        } else {
            updateSelectedTab(backstack.last().controller)
            router.setBackstack(backstack, null)
        }
    }

    override fun handleBack(): Boolean {
        val router = tabsRouter ?: return super.handleBack()

        val backstack = router.backstack
        if (backstack.isEmpty()) {
            return super.handleBack()
        }

        val lastIndex = backstack.size - 1
        if (backstack[lastIndex].controller.handleBack()) {
            return true
        }

        if (backstack[lastIndex].tag() == firstTag) {
            if (exitOnFirstTag) {
                router.setBackstack(emptyList(), null)
                return super.handleBack()
            }
            exitOnFirstTag = true
        }

        backstack.add(0, backstack.removeAt(lastIndex))
        updateSelectedTab(backstack.last().controller)
        router.setBackstack(backstack, null)
        return true
    }

    private fun showTab(item: MenuItem) {
        val router = tabsRouter ?: return

        val tag = item.tag
        val backstack = router.backstack

        val index = backstack.indexOfLast { it.tag() == tag }
        if (index == -1) {
            val controller = createTabController(item.itemId)
            updateSelectedTab(controller)
            router.pushController(RouterTransaction.with(controller).tag(tag))
            return
        }
        if (index == backstack.size - 1) {
            return
        }

        exitOnFirstTag = tag != firstTag || backstack.size <= 1

        backstack.add(backstack.removeAt(index))
        updateSelectedTab(backstack.last().controller)
        router.setBackstack(backstack, null)
    }

    private fun updateSelectedTab(controller: Controller) {
        useNavigationListener = false
        bottomNavigation?.selectedItemId = getItemId(controller)
        useNavigationListener = true
    }

    private val tabsRouter: Router?
        get() = tabsContainer?.let { getChildRouter(it) }

    private val firstTag: String?
        get() = bottomNavigation?.menu
            ?.takeIf { it.size() > 0 }
            ?.getItem(0)
            ?.tag

    private val MenuItem.tag: String
        get() = "$itemId"

    private val Menu.selectedItem: MenuItem?
        get() {
            for (i in 0 until size()) {
                val item = getItem(i)
                if (item.isChecked) {
                    return item
                }
            }
            return null
        }

}