package org.sdelaysam.util.di

import org.koin.dsl.module
import org.sdelaysam.util.navigation.DefaultNavigationPathProvider
import org.sdelaysam.util.navigation.DefaultNavigationRouter
import org.sdelaysam.util.navigation.NavigationRouter

/**
 * Created on 4/13/20.
 * @author sdelaysam
 */

val navigationModule = module {
    single<NavigationRouter> {
        DefaultNavigationRouter(
            DefaultNavigationPathProvider(
                get()
            )
        )
    }
}