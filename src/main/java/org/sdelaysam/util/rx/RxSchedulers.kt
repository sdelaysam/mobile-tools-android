package org.sdelaysam.util.rx

import io.reactivex.Scheduler

/**
 * Created on 6/7/20.
 * @author sdelaysam
 */

object RxSchedulers {
    lateinit var main: Scheduler
    lateinit var network: Scheduler
    lateinit var timer: Scheduler
}