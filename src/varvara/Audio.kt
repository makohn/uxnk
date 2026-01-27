package varvara

import util.*
import java.util.concurrent.Executors
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

class Audio(private val varvara: Varvara) : IODevice {

    private val player = AudioPlayer()

    companion object {
        const val PITCH: UByte = 0xfu

        const val SAMPLE_RATE = 44100
        const val NOTE_PERIOD = SAMPLE_RATE * 0x4000 / 11025
        const val ADSR_STEP: Int = SAMPLE_RATE / 0x0f
    }

    private class Voice(
        val data: ByteArray,
        val repeat: Boolean,
        val advance: UInt,
        val period: UInt,
        val attack: Int,
        val decay: Int,
        val sustain: Int,
        val release: Int,
        val volumeLeft: Int,
        val volumeRight: Int
    ) {
        var count: UInt = 0u
        var index: Int = 0
        var age: Int = 0
        var active = true

        fun envelope() = when {
            release == 0 -> 0x0888
            age < attack -> 0x0888 * age / attack
            age < decay -> 0x0444 * (2 * decay - attack - age) / (decay - attack)
            age < sustain -> 0x0444
            age < release -> 0x0444 * (release - age) / (release - sustain)
            else -> {
                active = false
                0x0000
            }
        }

        fun play(audioPlayer: AudioPlayer) {
            val buffer = ByteArray(256 * 2)
            while (active) {
                for (i in 0..<data.size) {
                    count += advance
                    index += (count / period).toInt()
                    count %= period

                    if (index >= data.size) {
                        if (!repeat) {
                            active = false
                            buffer[i * 2] = 0
                            buffer[i * 2 + 1] = 0
                            continue
                        }
                        index %= data.size
                    }

                    val raw = data[index].toInt()
                    val env = envelope()
                    age++
                    val s = raw * env

                    val left = (s * volumeLeft / 0x180).coerceIn(-128, 127).toByte()
                    val right = (s * volumeRight / 0x180).coerceIn(-128, 127).toByte()

                    buffer[i * 2] = left
                    buffer[i * 2 + 1] = right
                }
                audioPlayer.play(buffer)
            }
        }
    }

    private val advances = uintArrayOf(
        0x80000u, 0x879c8u, 0x8facdu, 0x9837fu, 0xa1451u, 0xaadc1u,
        0xb504fu, 0xbfc88u, 0xcb2ffu, 0xd7450u, 0xe411fu, 0xf1a1cu
    )

    private val memory = UByteArray(16)

    private val executor = Executors.newSingleThreadExecutor()

    override fun write(port: UByte, value: UByte) {
        memory[port] = value
        when (port) {
            PITCH -> {
                val adsr = UShort(memory[8], memory[9]).toInt()
                val addr = UShort(memory[0xc], memory[0xd])
                val len = UShort(memory[0xa], memory[0xb]).toInt()
                val repeat = (value and 0x80u) == UByte_0
                val note = (value and 0x7fu).toInt()
                val volumeLeft = (memory[0xe].toInt() ushr 4) and 0xf
                val volumeRight = memory[0xe].toInt() and 0xf
                val data = ByteArray(len)
                for (i in 0..<len) {
                    data[i] = (varvara.machine.memory[addr + i.toUShort()]).toByte()
                }
//                println("""
//                    adsr: ${adsr.toString(2)}
//                    len:  $len
//                    addr: $addr
//                    ptch: ${value.toString(2)}
//                    loop: ${loop}
//                    note: ${note}
//                    samp: ${bytes.contentToString()}
//                """.trimIndent())

                val advance = if (note < 108 && len > 0) advances[note % 12] shr (8 - note / 12) else return

                val attack = ADSR_STEP * (adsr shr 12)
                val decay = ADSR_STEP * (adsr shr 8 and 0xf) + attack
                val sustain = ADSR_STEP * (adsr shr 4 and 0xf) + decay
                val release = ADSR_STEP * (adsr and 0xf) + sustain

                val period = if (len <= 0x100) NOTE_PERIOD * 337 / 2 / len else NOTE_PERIOD

                val voice = Voice(
                    data,
                    repeat,
                    advance,
                    period.toUInt(),
                    attack,
                    decay,
                    sustain,
                    release,
                    volumeLeft,
                    volumeRight
                )
                voice.play(player)
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