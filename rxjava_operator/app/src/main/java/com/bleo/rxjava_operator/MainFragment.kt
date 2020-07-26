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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.*

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

    private val countValue: String
        get() = countEditText.text.toString()

    // MARK: - UI
    private lateinit var observableResultTextView: TextView
    private lateinit var observableJustTriggerButton: Button
    private lateinit var observableRangeTriggerButton: Button
    private lateinit var observableRepeatTriggerButton: Button
    private lateinit var observableIntervalTriggerButton: Button
    private lateinit var observableFromTriggerButton: Button
    private lateinit var observableStartTriggerButton: Button

    private lateinit var countEditText: EditText
    private lateinit var clearButton: Button

    private lateinit var disposeResultTextView: TextView
    private lateinit var disposeTriggerButton: Button

    private var count: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {

            // Observable
            observableResultTextView = findViewById(R.id.observable_just_result)
            observableJustTriggerButton = findViewById(R.id.observable_just_trigger)
            observableRangeTriggerButton = findViewById(R.id.observable_range_trigger)
            observableRepeatTriggerButton = findViewById(R.id.observable_repeat_trigger)
            observableIntervalTriggerButton = findViewById(R.id.observable_interval_trigger)
            observableFromTriggerButton = findViewById(R.id.observable_from_trigger)
            observableStartTriggerButton = findViewById(R.id.observable_start_trigger)

            // Count
            countEditText = findViewById(R.id.countEditText)
            clearButton = findViewById(R.id.clearButton)

            // Dispose
            disposeResultTextView = findViewById(R.id.dispose_result_textview)
            disposeTriggerButton = findViewById(R.id.dispose_trigger)

            setupObservable()
        }
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
    }

    private fun bind() {

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

        resultObservable
            .subscribe {
                Log.d(TAG, "Observable just:: emit $it")
                observableResultTextView.text = it
            }
            .disposed(by = disposeBag)

        clearButton
            .clicks()
            .subscribe {
                observableResultTextView.text = ""
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
        Observable
            .just(countValue)
            .subscribe {
                observableEmitter.onNext(it)
            }
            .disposed(disposeBag)
    }

    private fun observableRange() {
        Observable
            .range(0, countValue.toInt())
            .subscribe {
                val result = "${observableResultTextView.text} $it"
                observableResultTextView.text = result
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
                observableResultTextView.text = result
            }
            .disposed(by = disposeBag)
    }

    private fun observableInterval() {
        Observable
            .interval(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.d(TAG, "observableInterval: $it")
                observableResultTextView.text = it.toString()
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
                    observableResultTextView.text = result
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
                observableResultTextView.text = it
            }
            .disposed(by = disposeBag)
    }
}

fun Disposable.disposed(by: CompositeDisposable): Disposable {
    by.add(this)
    return this
}
