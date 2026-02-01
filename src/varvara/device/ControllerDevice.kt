package varvara.device

import util.*
import varvara.Device

class ControllerDevice : Device() {

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

    val vector: UShort get() = UShort(memory[0], memory[1])

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