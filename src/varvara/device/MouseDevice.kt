package varvara.device

import util.*
import varvara.Device

class MouseDevice : Device() {

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

    val vector: UShort get() = UShort(memory[0], memory[1])

    fun setPos(x: Int, y: Int) {
        writeShort(X, x.toUShort())
        writeShort(Y, y.toUShort())
    }

    fun setButton(button: UByte) {
        write(STATE, memory[STATE] or button)
    }

    fun unsetButton(button: UByte) {
        write(STATE, memory[STATE] and button.inv())
    }
}