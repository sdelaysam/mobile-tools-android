package org.sdelaysam.util.view.provider

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/**
 * Created on 7/25/20.
 * @author sdelaysam
 */

interface TextRelayProvider {
    val textRelay: BehaviorRelay<String>
}

interface ActionRelayProvider {
    val actionRelay: PublishRelay<Unit>
}

interface EnabledObservableProvider {
    val enabledObservable: Observable<Boolean>
}

interface ActivityObservableProvider {
    val activityObservable: Observable<Boolean>
}

interface ErrorObservableProvider {
    val errorObservable: Observable<String>
}

interface TextResProvider {
    val textRes: Int
}

interface PlaceholderResProvider {
    val placeholderRes: Int
}

interface HeightProvider {
    val height: Int
}

interface HeightResProvider {
    val heightRes: Int
}