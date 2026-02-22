import varvara.Varvara
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread

object AudioPlayer {

    private val format = AudioFormat(44100f, 16, 2, true, false)
    private val info = DataLine.Info(SourceDataLine::class.java, format)
    private val line = AudioSystem.getLine(info) as SourceDataLine

    private val ready = Semaphore(1)
    private val samples = ShortArray(1024)
    private val executor = Executors.newSingleThreadExecutor()

    val lock = ReentrantLock()

    fun start(varvara: Varvara) {
        line.open(format)
        line.start()

        thread(start = true, isDaemon = true) {
            while (true) {
                ready.acquire()
                line.flush()
                executor.execute {
                    var running = true
                    while (running) {
                        running = false
                        for (device in varvara.audio.indices) {
                            if (varvara.audio[device].render(samples)) {
                                running = true
                            } else {
                                events.trySend(Event.AudioFinished(device))
                            }
                        }
                        play(samples)
                        samples.fill(0)
                    }
                }
            }
        }
    }

    fun unpause() {
        ready.release()
    }

    private fun play(buffer: ShortArray): Int {
        val len = buffer.size * 2
        val byteBuffer = ByteArray(len)
        for (i in 0..<buffer.size) {
            byteBuffer[i * 2] = buffer[i].toByte()
            byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8)).toByte()
        }
        line.write(byteBuffer, 0, byteBuffer.size)
        return buffer.size
    }
}