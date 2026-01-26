package varvara

import util.*

class Controller : IODevice {

    companion object {
        const val BUTTON: UByte = 0x2u
        const val KEY: UByte = 0x3u

        const val A: UByte = 0x01u
        const val B: UByte = 0x02u
        const val SELECT: UByte = 0x04u
        const val START: UByte = 0x08u
        const val UP: UByte = 0x10u
        const val DOWN: UByte = 0x20u
        const val LEFT: UByte = 0x40u
        const val RIGHT: UByte = 0x80u
    }

    private val memory = UByteArray(16)
    val vector: UShort get() = UShort(memory[0], memory[1])

    override fun write(port: UByte, value: UByte) {
        memory[port] = value
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

    fun setKey(key: Int) {
        write(KEY, key.toUByte())
    }

    fun setButton(button: UByte) {
        write(BUTTON, memory[BUTTON] or button)
    }

    fun unsetButton(button: UByte) {
        write(BUTTON, memory[BUTTON] and button.inv())
    }
}