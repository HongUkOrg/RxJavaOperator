package com.bleo.rxjava_operator

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observables.ConnectableObservable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.main_fragment.*
import java.lang.Error
import java.lang.RuntimeException
import java.util.concurrent.*
import kotlin.Exception as KotlinException

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        private const val TAG: String = "bleo"
    }

    // MARK: - Rx
    private var disposeBag = CompositeDisposable()

    // MARK: - Properties
    private lateinit var viewModel: MainViewModel

    private lateinit var resultObservable: Observable<String>
    private lateinit var observableEmitter: ObservableEmitter<String>

    // Connectable observable
    private lateinit var connectableObservable: ConnectableObservable<Long>
    private lateinit var connectedObservable: Disposable

    private val countValue: String
        get() = countEditText.text.toString()

    // MARK: - UI

    private lateinit var countEditText: EditText
    private lateinit var clearButton: Button

    private lateinit var disposeResultTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {

            setupObservable()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeBag.dispose()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        bind()
    }

    private fun setupObservable() {
        resultObservable = Observable.create<String> { observable ->
            observableEmitter = observable
        }

        connectableObservable = Observable
            .interval(100, TimeUnit.MILLISECONDS)
            .publish()
    }

    private fun bind() {

        resultObservable
            .subscribe {
                Log.d(TAG, "Emit: $it")
                observableResultTextView.text = it
            }
            .disposed(by = disposeBag)

        viewModel.countBehaviorSubject
            .subscribe {
                behaviorSubjectTextView.text = it
            }
            .disposed(by = disposeBag)

        viewModel.countBehaviorRelay
            .subscribe {
                behaviorRelayTextView.text = it
            }
            .disposed(by = disposeBag)

        observableJustTriggerButton
            .clicks()
            .subscribe {
                observableJust()
            }
            .disposed(by = disposeBag)

        observableRangeTriggerButton
            .clicks()
            .subscribe {
                observableRange()
            }
            .disposed(by = disposeBag)

        observableRepeatTriggerButton
            .clicks()
            .subscribe {
                observableRepeat()
            }
            .disposed(by = disposeBag)

        observableIntervalTriggerButton
            .clicks()
            .subscribe {
                observableInterval()
            }
            .disposed(by = disposeBag)

        observableFromTriggerButton
            .clicks()
            .subscribe {
                observableFrom()
            }
            .disposed(by = disposeBag)

        observableStartTriggerButton
            .clicks()
            .subscribe {
                observableStart()
            }
            .disposed(by = disposeBag)

        observableSubscribeTriggerButton
            .clicks()
            .subscribe {
                subscribeConnectableObservable()
            }
            .disposed(by = disposeBag)

        observableConnectTriggerButton
            .clicks()
            .subscribe {
                observableConnect()
            }
            .disposed(by = disposeBag)

        observableDisconnectTriggerButton
            .clicks()
            .subscribe {
                observableDisconnect()
            }
            .disposed(by = disposeBag)

        singleCreateTriggerButton
            .clicks()
            .subscribe {
                singleCreate()
            }
            .disposed(by = disposeBag)

        behaviorSubjectTriggerButton
            .clicks()
            .subscribe {
                viewModel.tapBehaviorSubjectButton()
            }
            .disposed(by = disposeBag)

        behaviorRelayTriggerButton
            .clicks()
            .subscribe {
                viewModel.tapBehaviorRelayButton()
            }
            .disposed(by = disposeBag)

        clearButton
            .clicks()
            .subscribe {
                observableEmitter.onNext("")
            }
            .disposed(by = disposeBag)

        disposeTriggerButton
            .clicks()
            .throttleFirst(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                disposeResultTextView.text = "Disposed"
                disposeBag.dispose()
            }
            .disposed(by = disposeBag)

    }

    private fun observableJust() {
        val disposable = Observable
            .just(countValue)
            .subscribe {
                observableEmitter.onNext(it)
            }
            .disposed(disposeBag)

    }

    private fun observableRange() {
        Observable
            .range(0, countValue.toInt())
            .map { it -> it * 10 }
            .subscribe {
                val result = "${observableResultTextView.text} $it"
                observableEmitter.onNext(result)
            }
            .disposed(disposeBag)
    }

    private fun observableRepeat() {
        Observable
            .just(countValue)
            .repeat()
            .take(10)
            .subscribe {
                Log.d(TAG, "observableRepeat: $it")
                val result = "${observableResultTextView.text} $it"
                observableEmitter.onNext(result)
            }
            .disposed(by = disposeBag)
    }

    private fun observableInterval() {
        Observable
            .interval(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.d(TAG, "observableInterval: $it")
                observableEmitter.onNext(it.toString())
            }
            .disposed(by = disposeBag)
    }

    private fun observableFrom() {

        // fromArray
        val arr = Array(10) { i -> i * countValue.toInt() }
        Observable.fromArray(arr)
            .subscribe {
                Log.d(TAG, "observableFrom: size - ${it.size}")
                for (element in it) {
                    val result = "${observableResultTextView.text} $element"
                    observableEmitter.onNext(result)
                }
            }
            .disposed(by = disposeBag)

    }

    private fun observableStart() {

        // from Callable
        val callable = Callable<String> {
            Thread.sleep(3000)
            "Hello"
        }

        Observable.fromCallable(callable)
            .subscribe {
                Log.d(TAG, "observableFrom: $it")
                observableEmitter.onNext(it)
            }
            .disposed(by = disposeBag)
    }

    private fun subscribeConnectableObservable() {
        connectableObservable
            .subscribe {
                Log.d(TAG, "connectable Observable result $it")
                observableEmitter.onNext(it.toString())
            }
            .disposed(by = disposeBag)
    }

    private fun observableConnect() {
        connectedObservable = connectableObservable.connect()
    }

    private fun observableDisconnect() {
        if (::connectedObservable.isInitialized == false) {
            Log.d(TAG, "observableConnect: not initialized")
            return
        }
        connectedObservable.dispose()
    }

    private fun singleCreate() {
        Single.create<String> {
            // Do something..
            // 퍼미션 받고 콜백핸들러로 값을 받아옴
            // API 콜을 통해서 repsonse값을 받아온다던가..!?
            when (countValue.toInt()) {
                200 -> {
                    it.onSuccess("Matched!")// case 1: disposed
                }
                400 -> {
                    it.onError(Throwable("Code: 400"))
                }
                else -> {
                    throw RuntimeException("Unknown response") // case 2: throw
                }
            }
        }.subscribe({
            // Success
            Log.d(TAG, "singleCreate: Success - $it")
        }, {
            // Error
            Log.d(TAG, "singleCreate: Error!! $it")
        })
    }

}

fun Disposable.disposed(by: CompositeDisposable): Disposable {
    by.add(this)
    return this
}
