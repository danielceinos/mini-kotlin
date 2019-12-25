package org.sample

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mini.LoggerInterceptor
import mini.MiniGen
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

        lifecycleScope.launch {
            dummyStore.flow().collect {
                demo_text.text = it.text
                Log.e("SampleRxActivity", "flow = ${it.text}")
            }
        }

        dummyStore.flowable().subscribe {
            demo_text.text = it.text
            Log.e("SampleRxActivity", "rx = ${it.text}")
        }.track()


        Grove.plant(ConsoleLogTree())
        dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
            Grove.tag(tag).d { msg }
        }))

        dispatcher.dispatch(ActionTwo("accion1"))
        GlobalScope.launch(Dispatchers.IO) {
            dispatcher.dispatchAsync(ActionTwo("accion2"))
        }

        demo_text.setOnClickListener {
            dispatcher.dispatch(ActionTwo("accionCLick"))
        }
    }
}