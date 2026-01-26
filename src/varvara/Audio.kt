package varvara

import util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.math.pow

class Audio(private val varvara: Varvara) : IODevice {

    private val player = AudioPlayer()

    companion object {
        const val PITCH: UByte = 0xfu

        const val BASE_NOTE = 60
    }

    private val memory = UByteArray(16)

    var first = false

    override fun write(port: UByte, value: UByte) {
        memory[port] = value
        when (port) {
            PITCH -> {
                val adsr = UShort(memory[8], memory[9])
                val addr = UShort(memory[0xc], memory[0xd])
                val len = UShort(memory[0xa], memory[0xb]).toInt()
                val loop = value and 0x80u
                val note = value and 0x7fu
                val bytes = ByteArray(len)
                for (i in 0..<len) {
                    bytes[i] = varvara.machine.memory[addr + i.toUShort()].toByte()
                }
                println("""
                    adsr: ${adsr.toString(2)}
                    len:  $len
                    addr: $addr
                    ptch: ${value.toString(2)}
                    loop: ${loop}
                    note: ${note}
                    samp: ${bytes.contentToString()}
                """.trimIndent())


                val step = 2.0.pow((note.toInt() - BASE_NOTE) / 12.0)
                var phase = 0.0
                val buffer = ByteArray(512)

                for (i in 0..511) {
                    buffer[i] = bytes[phase.toInt() and 255]
                    phase += step
                }

                val attack = adsr and 0xf000u
                val decay = adsr and 0x0f00u
                val sustain = adsr and 0x00f0u
                val release = adsr and 0x000fu
                play(attack, buffer)
                play(decay, buffer.map { (it * 0.5).toInt().toByte() }.toByteArray())
                play(sustain, buffer.map { (it * 0.5).toInt().toByte() }.toByteArray())
                play(release, ByteArray(buffer.size))
            }
        }
    }

    private fun play(type: UShort, bytes: ByteArray) {
        var env = type
        while (env > 0u) {
            if (env and 0x1u != UShort_0) {
                val durationMs = 125
                val totalSamples = (44100f * durationMs / 1000f).toInt()
                var samplesWritten = 0
                while (samplesWritten < totalSamples) {
                    samplesWritten += player.play(bytes)
                }
            }
            env = (env.toUInt() shr 1).toUShort()
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

    private val format = AudioFormat(44100f, 8, 1, false, false)
    private val info = DataLine.Info(SourceDataLine::class.java, format)
    private val line = AudioSystem.getLine(info) as SourceDataLine

    init {
        line.open(format)
        line.start()
    }

    fun play(buffer: ByteArray): Int {
        line.write(buffer, 0, buffer.size)
        return buffer.size
    }

    fun close() {
        line.drain()
        line.stop()
        line.close()
    }
}

fun main() {
    val sr = 44100f
    val format = AudioFormat(sr, 8, 1, false, false)
    val info = DataLine.Info(SourceDataLine::class.java, format)
    val line = AudioSystem.getLine(info) as SourceDataLine
    val sine = byteArrayOf(0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45, 48, 51, 54, 57, 59, 62, 65, 67, 70, 73, 75, 78, 80, 82, 85, 87, 89, 91, 94, 96, 98, 100, 102, 103, 105, 107, 108, 110, 112, 113, 114, 116, 117, 118, 119, 120, 121, 122, 123, 123, 124, 125, 125, 126, 126, 126, 126, 126, 127, 126, 126, 126, 126, 126, 125, 125, 124, 123, 123, 122, 121, 120, 119, 118, 117, 116, 114, 113, 112, 110, 108, 107, 105, 103, 102, 100, 98, 96, 94, 91, 89, 87, 85, 82, 80, 78, 75, 73, 70, 67, 65, 62, 59, 57, 54, 51, 48, 45, 42, 39, 36, 33, 30, 27, 24, 21, 18, 15, 12, 9, 6, 3, 0, -3, -6, -9, -12, -15, -18, -21, -24, -27, -30, -33, -36, -39, -42, -45, -48, -51, -54, -57, -59, -62, -65, -67, -70, -73, -75, -78, -80, -82, -85, -87, -89, -91, -94, -96, -98, -100, -102, -103, -105, -107, -108, -110, -112, -113, -114, -116, -117, -118, -119, -120, -121, -122, -123, -123, -124, -125, -125, -126, -126, -126, -126, -126, -127, -126, -126, -126, -126, -126, -125, -125, -124, -123, -123, -122, -121, -120, -119, -118, -117, -116, -114, -113, -112, -110, -108, -107, -105, -103, -102, -100, -98, -96, -94, -91, -89, -87, -85, -82, -80, -78, -75, -73, -70, -67, -65, -62, -59, -57, -54, -51, -48, -45, -42, -39, -36, -33, -30, -27, -24, -21, -18, -15, -12, -9, -6, -3)

    line.open(format)
    line.start()

    repeat(5) {
        val note = 84 + it
        val baseNote = 60

        val step = 2.0.pow((note - baseNote) / 12.0)
        var phase = 0.0
        val buffer = ByteArray(512)

        repeat(80) {
            for (i in 0..511) {
                buffer[i] = sine[phase.toInt() and 255]
                phase += step
            }
            line.write(buffer, 0, 512)
        }
    }

    line.drain()
    line.stop()
    line.close()
}