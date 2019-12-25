package mini.rx

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mini.SampleStore
import org.amshove.kluent.`should be equal to`
import org.junit.Test

class RxUtilsKtTest {
    @Test
    fun `flowable sends initial state`() {
        runBlocking {
            val store = SampleStore()
            store.updateState("abc") //Set before subscribe
            var sentState = ""
            store.flowable().subscribe {
                sentState = it
            }
            delay(10)
            sentState `should be equal to` "abc"
        }
    }

    @Test
    fun `flowable sends updates`() {
        runBlocking {

            val store = SampleStore()
            var sentState = ""
            store.flowable().subscribe {
                sentState = it
            }
            store.updateState("abc") //Set before subscribe

            delay(10)

            sentState `should be equal to` "abc"
        }
    }

    @Test
    fun `observable sends initial state`() {
        runBlocking {
            val store = SampleStore()
            store.updateState("abc") //Set before subscribe
            var sentState = ""
            store.observable().subscribe {
                sentState = it
            }

            delay(10)

            sentState `should be equal to` "abc"
        }
    }

    @Test
    fun `observable sends updates`() {
        runBlocking {
            val store = SampleStore()
            var sentState = ""
            store.observable().subscribe {
                sentState = it
            }
            store.updateState("abc") //Set before subscribe

            delay(10)

            sentState `should be equal to` "abc"
        }
    }
}