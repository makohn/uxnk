package varvara.device

import util.*
import varvara.Device
import varvara.Varvara
import kotlin.concurrent.withLock

class AudioDevice(varvara: Varvara) : Device() {

    companion object {
        const val PITCH: UByte = 0xfu

        const val SAMPLE_RATE = 44100
        const val NOTE_PERIOD = SAMPLE_RATE * 0x4000 / 11025
        const val ADSR_STEP: Int = SAMPLE_RATE / 0x0f

        private val ADVANCES = uintArrayOf(
            0x80000u, 0x879c8u, 0x8facdu, 0x9837fu, 0xa1451u, 0xaadc1u,
            0xb504fu, 0xbfc88u, 0xcb2ffu, 0xd7450u, 0xe411fu, 0xf1a1cu
        )
    }

    val vector: UShort get() = UShort(memory[0], memory[1])

    private val uxn = varvara.uxn
    private var audio: Audio = NoopAudio

    private interface Audio {
        fun render(samples: ShortArray): Boolean
    }

    private object NoopAudio : Audio {
        override fun render(samples: ShortArray) = false
    }

     private class AudioImpl(
        private val uxnMemory: UByteArray,
        private val address: UShort,
        private val length: UShort,
        private val repeat: Boolean,
        private val period: UInt,
        private val attack: Int,
        private val decay: Int,
        private val sustain: Int,
        private val release: Int,
        private val volumeLeft: Byte,
        private val volumeRight: Byte,
        private var advance: UInt
    ): Audio {
        private var count = 0u
        private var age = 0
        private var i: UShort = 0u

        private fun envelope(age: Int): Int {
            return when {
                release == 0 -> 0x0888
                age < attack -> 0x0888 * age / attack
                age < decay -> 0x0444 * (2 * decay - attack - age) / (decay - attack)
                age < sustain -> 0x0444
                age < release -> 0x0444 * (release - age) / (release - sustain)
                else -> {
                    advance = 0u
                    0x0000
                }
            }
        }

        override fun render(samples: ShortArray): Boolean {
            if (advance == 0u || period == 0u) return false
            var j = 0
            while (j < samples.size) {
                count += advance
                i = (i + (count / period)).toUShort()
                count %= period
                if (i >= length) {
                    if (!repeat) {
                        advance = 0u
                        break
                    }
                    i = (i % length).toUShort()
                }
                val sample = (uxnMemory[address + i] + 0x80u).toByte() * envelope(age++)
                samples[j] = (samples[j++] + (sample * volumeLeft / 0x180)).toShort()
                samples[j] = (samples[j++] + (sample * volumeRight / 0x180)).toShort()
            }
            return true
        }
    }

    private fun audioStart(value: UByte) {
        val adsr = UShort(memory[8], memory[9]).toInt()
        val address = UShort(memory[0xc], memory[0xd])
        val len = UShort(memory[0xa], memory[0xb])
        val length = if (len > (0x10000u - address)) (0x10000u - address).toUShort() else len
        val volumeLeft = ((memory[0xe].toInt() shr 4)).toByte()
        val volumeRight = (memory[0xe] and 0xfu).toByte()
        val repeat = (value and 0x80u) == UByte_0
        val pitch = (value and 0x7fu).toInt()
        val advance = if (pitch < 108 && len > 0u) ADVANCES[pitch % 12] shr (8 - pitch / 12) else 0u
        val attack = (ADSR_STEP * (adsr shr 12))
        val decay = (ADSR_STEP * (adsr shr 8 and 0xf)) + attack
        val sustain = (ADSR_STEP * (adsr shr 4 and 0xf)) + decay
        val release = (ADSR_STEP * (adsr and 0xf)) + sustain
        val period = if (length <= 0x100u) (NOTE_PERIOD * 337 / 2 / len.toInt()).toUInt() else NOTE_PERIOD.toUInt()

        this.audio = AudioImpl(
            uxn.memory,
            address,
            length,
            repeat,
            period,
            attack,
            decay,
            sustain,
            release,
            volumeLeft,
            volumeRight,
            advance
        )
    }

    override fun write(port: UByte, value: UByte) {
        super.write(port, value)
        when (port) {
            PITCH -> {
                AudioPlayer.lock.withLock {
                    audioStart(value)
                }
                AudioPlayer.unpause()
            }
        }
    }

    fun render(samples: ShortArray) = audio.render(samples)
}