package varvara

import util.*

class Mouse : IODevice {

    companion object {
        const val X: UByte = 0x2u
        const val Y: UByte = 0x4u
        const val STATE: UByte = 0x6u
        const val SCROLL_X: UByte = 0xau
        const val SCROLL_Y: UByte = 0xcu

        const val BUTTON_1: UByte = 0x1u
        const val BUTTON_2: UByte = 0x2u
        const val BUTTON_3: UByte = 0x4u
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

    fun setXY(x: Int, y: Int) {
        writeShort(X, x.toUShort())
        writeShort(Y, y.toUShort())
    }

    fun setButton(state: UByte) {
        write(STATE, memory[STATE] xor state)
    }
}