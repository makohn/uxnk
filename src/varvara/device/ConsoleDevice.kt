package varvara.device

import util.*
import uxn.UxnMachine
import varvara.Device

class ConsoleDevice : Device() {

    companion object {
        const val WRITE: UByte = 0x8u
        const val ERROR: UByte = 0x9u
        const val READ: UInt = 0x2u
        const val TYPE: UInt = 0x7u
    }

    val vector: UShort get() = UShort(memory[0], memory[1])

    override fun write(port: UByte, value: UByte) {
        super.write(port, value)
        when (port) {
            WRITE -> print(value.toInt().toChar())
            ERROR -> System.err.print(value.toInt().toChar())
        }
    }

    fun consoleInput(uxn: UxnMachine, c: UByte, type: UByte) {
        memory[READ] = c
        memory[TYPE] = type
        if (vector > 0u) {
            uxn.eval(vector)
        }
    }
}