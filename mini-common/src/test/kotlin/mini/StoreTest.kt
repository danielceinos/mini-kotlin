package mini

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Test

class StoreTest {


    @Test(timeout = 1000)
    fun `flow sends initial state on collection`(): Unit = runBlocking {
        val store = SampleStore()
        store.updateState("abc") //Set before collect
        var sentState = ""
        val job = GlobalScope.launch {
            store.flow(hotStart = true).take(1).collect {
                if (it == "abc")
                    sentState = it
            }
        }
        job.join()
        sentState `should be equal to` "abc"
        Unit
    }

    @Test(timeout = 1000)
    fun `flow sends updates`(): Unit = runBlocking {
        val store = SampleStore()
        var sentState = ""
        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.flow(hotStart = false).take(1).collect {
                if (it == "abc")
                    sentState = it
            }
        }
        store.updateState("abc")
        job.join()
        sentState `should be equal to` "abc"
        Unit
    }

    @Test(timeout = 1000)
    fun `flow sends updates to all`() {
        runBlocking {
            val store = SampleStore()
            val called = intArrayOf(0, 0)
            val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    store.flow(hotStart = false).take(1).collect {
                        if (it == "abc")
                            called[0]++
                    }
                }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    store.flow(hotStart = false).take(1).collect {
                        if (it == "abc")
                            called[1]++
                    }
                }
            }
            store.updateState("abc")
            job.join()  //Wait for both to have their values
            //Each called two times, one for initial state, another for sent state
            called.`should equal`(intArrayOf(1, 1))
            Unit
        }
    }
}