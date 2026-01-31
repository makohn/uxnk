package varvara

import util.*
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.experimental.and

class Audio(private val varvara: Varvara) : IODevice {

    private val player = AudioPlayer()

    companion object {
        const val PITCH: UByte = 0xfu

        const val SAMPLE_RATE = 44100
        const val NOTE_PERIOD = SAMPLE_RATE * 0x4000 / 11025
        const val ADSR_STEP: Int = SAMPLE_RATE / 0x0f


        val advances = uintArrayOf(
            0x80000u, 0x879c8u, 0x8facdu, 0x9837fu, 0xa1451u, 0xaadc1u,
            0xb504fu, 0xbfc88u, 0xcb2ffu, 0xd7450u, 0xe411fu, 0xf1a1cu
        )
    }

    class UxnAudio {
        var addr: UShort = 0x0u
        var count: UInt = 0x0u
        var advance: UInt = 0x0u
        var period: UInt = 0x0u
        var age: UInt = 0x0u
        var a: UInt = 0x0u
        var d: UInt = 0x0u
        var s: UInt = 0x0u
        var r: UInt = 0x0u
        var i: UShort = 0x0u
        var len: UShort = 0x0u
        val volume = ByteArray(2)
        var pitch: UByte = 0x0u
        var repeat: Boolean = false
    }

    fun envelope(c: UxnAudio, age: UInt): Int {
        if (c.r == 0u) return 0x0888
        if (age < c.a) return 0x0888 * age.toInt() / c.a.toInt()
        if (age < c.d) return 0x0444 * (2 * c.d.toInt() - c.a.toInt() - age.toInt()) / (c.d.toInt() - c.a.toInt())
        if (age < c.s) return 0x0444
        if (age < c.r) return 0x0444 * (c.r.toInt() - age.toInt()) / (c.r.toInt() - c.s.toInt())
        c.advance = 0u
        return 0x0000
    }

    fun audioRender(c: UxnAudio, sample: ShortArray, len: Short): Boolean {
        val ram = varvara.machine.memory
        if (c.advance == 0u || c.period == 0u) return false
        var s: Int
        var j = 0
        for (x in 0..<len) {
            c.count += c.advance
            c.i = (c.i + (c.count / c.period)).toUShort()
            c.count %= c.period
            if (c.i >= c.len) {
                if (!c.repeat) {
                    c.advance = 0u
                    break
                }
                c.i = (c.i % c.len).toUShort()
            }

//            print("${ram[c.addr + c.i]}")
            val env = envelope(c, c.age)
            s = (((ram[c.addr + c.i] + 0x80u).toByte() * env))
//            print(" -> $s (count=${c.count}, i=${c.i}, advance=${c.advance}, period=${c.period}, env=${env})")
            c.age++
            sample[j] = (sample[j] + (s * c.volume[0] / 0x180)).coerceIn(-32768, 32767).toShort()
            j++
            sample[j] = (sample[j] + (s * c.volume[1] / 0x180)).coerceIn(-32768, 32767).toShort()
            j++
//            println()
        }
//        readln()
        return true
    }

    fun audioStart(value: UByte): UxnAudio {
        val c = UxnAudio()

        val adsr = UShort(memory[8], memory[9]).toInt()
        val addr = UShort(memory[0xc], memory[0xd])
        val len = UShort(memory[0xa], memory[0xb])
        c.len = len
        if(len > (0x10000u - addr))
            c.len = (0x10000u - addr).toUShort()

        c.addr = addr
        c.volume[0] = ((memory[0xe].toInt() shr 4).toUInt() and 0xfu).toByte()
        c.volume[1] = (memory[0xe] and 0xfu).toByte()

        val repeat = (value and 0x80u) == UByte_0
        c.repeat = repeat

        val pitch = (value and 0x7fu).toInt()
        if (pitch < 108 && len > 0u)
            c.advance = advances[pitch % 12] shr (8 - pitch / 12)
        else
            c.advance = 0u

        c.a = (ADSR_STEP * (adsr shr 12)).toUInt()
        c.d = (ADSR_STEP * (adsr shr 8 and 0xf)).toUInt() + c.a
        c.s = (ADSR_STEP * (adsr shr 4 and 0xf)).toUInt() + c.d
        c.r = (ADSR_STEP * (adsr and 0xf)).toUInt() + c.s
        c.age = 0u
        c.i = 0u

        if (c.len <= 0x100u)
            c.period = (NOTE_PERIOD * 337 / 2 / len.toInt()).toUInt()
        else c.period = NOTE_PERIOD.toUInt()
        return c
    }

    private val memory = UByteArray(16)

    private val executor = Executors.newSingleThreadExecutor()
    val lock = Semaphore(1)

    override fun write(port: UByte, value: UByte) {
        memory[port] = value
        when (port) {
            PITCH -> {
                player.close()
                player.start()
                val uxnAudio = audioStart(value)
                val buffer = ShortArray(1024)
                executor.execute {
                    while (audioRender(uxnAudio, buffer, 512)) {
                        player.play(buffer)
                        for (i in buffer.indices) buffer[i] = 0
                    }
                }
            }
        }
    }

    override fun writeShort(port: UByte, value: UShort) {
        write(port, value.hi)
        write((port + 1u).toUByte(), value.lo)
    }

    override fun read(port: UByte): UByte {
        return memory[port]
    }

    override fun readShort(port: UByte): UShort {
        val hi = memory[port]
        val lo = memory[port + 1u]
        return UShort(hi, lo)
    }
}

class AudioPlayer {

    private val format = AudioFormat(44100f, 16, 2, true, false)
    private val info = DataLine.Info(SourceDataLine::class.java, format)
    private val line = AudioSystem.getLine(info) as SourceDataLine

    init {
        line.open(format)
        line.start()
    }

    fun play(buffer: ShortArray): Int {
        val len = buffer.size * 2
        val byteBuffer = ByteArray(len)
        for (i in 0..<buffer.size) {
            byteBuffer[i * 2] = (buffer[i] and 0xff).toByte()
            byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xff).toByte()
        }
        line.write(byteBuffer, 0, byteBuffer.size)
        return buffer.size
    }

    fun start() {
//        line.open(format)
//        line.start()
    }

    fun close() {
        line.flush()
//        line.stop()
//        line.close()
    }
}