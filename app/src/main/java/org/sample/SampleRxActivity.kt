package org.sample

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import mini.LoggerInterceptor
import mini.MiniGen
import mini.flow.flow
import mini.rx.android.activities.FluxRxActivity
import mini.rx.flowable

class SampleRxActivity : FluxRxActivity() {

    private val dispatcher = MiniGen.newDispatcher()
    private val dummyStore = DummyStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val stores = listOf(dummyStore)
        MiniGen.subscribe(dispatcher, stores)
        stores.forEach { it.initialize() }

        var flag = true
        lifecycleScope.launch {
            dummyStore.flow().collect {
                demo_text.text = it.text
                Log.e("SampleRxActivity", "1 = ${it.text}")
            }
        }

        lifecycleScope.launch {
            dummyStore.flow().collect {
                demo_text.text = it.text
                Log.e("SampleRxActivity", "2 = ${it.text}")
            }
        }

        lifecycleScope.launch {
            dummyStore.flow().collect {
                demo_text.text = it.text
                Log.e("SampleRxActivity", "3 = ${it.text}")
            }
        }

        dummyStore.flowable().subscribe {
            demo_text.text = it.text
            Log.e("SampleRxActivity", "4 = ${it.text}")
        }.track()

        flag = false

        Grove.plant(ConsoleLogTree())
        dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
            Grove.tag(tag).d { msg }
        }))

        Handler().postDelayed({
            lifecycleScope.launch {
                dummyStore.flow().collect {
                    demo_text.text = it.text
                    Log.e("SampleRxActivity", "5 = ${it.text}")
                }
            }
        },1000)

        dispatcher.dispatch(ActionTwo("asdfghjk"))
    }
}