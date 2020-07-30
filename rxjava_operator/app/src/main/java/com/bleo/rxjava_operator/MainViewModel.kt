package com.bleo.rxjava_operator

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.subjects.BehaviorSubject

class MainViewModel : ViewModel() {

    val countBehaviorSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("0")
    val countBehaviorRelay: BehaviorRelay<String> = BehaviorRelay.createDefault("0")

    fun tapBehaviorSubjectButton() {
        val result = countBehaviorSubject.value.toInt() + 10
        countBehaviorSubject.onNext(result.toString())

        if (result > 50) {
            countBehaviorSubject.onComplete()
        }
    }

    fun tapBehaviorRelayButton() {
        val result = countBehaviorRelay.value.toInt() + 10
        countBehaviorRelay.accept(result.toString())

        if (result > 50) {
            countBehaviorRelay.onErrorComplete()
        }
    }
}