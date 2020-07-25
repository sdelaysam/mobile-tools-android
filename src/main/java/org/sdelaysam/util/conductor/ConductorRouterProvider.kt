package org.sdelaysam.util.conductor

import com.bluelinelabs.conductor.Router

/**
 * Created on 4/17/20.
 * @author sdelaysam
 */

interface ConductorRouterOwner {
    val router: Router?
}

interface ConductorRouterProvider {
    val currentRouter: Router?
}