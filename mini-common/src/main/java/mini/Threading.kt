package mini

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Semaphore

val uiHandler by lazy {
    Handler(Looper.getMainLooper())
}

fun assertOnUiThread() {
    if (!isAndroid) return
    if (Looper.myLooper() != Looper.getMainLooper()) {
        error("This method can only be called from the main application thread")
    }
}

@JvmOverloads
inline fun onUi(delayMs: Long = 0, crossinline block: () -> Unit) {
    if (delayMs > 0) uiHandler.postDelayed({ block() }, delayMs)
    else uiHandler.post { block() }
}

inline fun <T> onUiSync(crossinline block: () -> T) {
    uiHandler.postSync(block)
}

inline fun <T> Handler.postSync(crossinline block: () -> T) {
    if (Looper.myLooper() == this.looper) {
        block()
    } else {
        val sem = Semaphore(0)
        post {
            block()
            sem.release()
        }
        sem.acquireUninterruptibly()
    }
}