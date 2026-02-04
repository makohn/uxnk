package varvara.device

import util.*
import varvara.Device

class ConsoleDevice : Device() {

    companion object {
        const val WRITE: UByte = 0x8u
        const val ERROR: UByte = 0x9u
        const val READ: UInt = 0x2u
        const val TYPE: UInt = 0x7u

        const val NO_QUEUE: UByte = 0x0u
        const val STDIN: UByte = 0x1u
        const val ARGUMENT: UByte = 0x2u
        const val ARGUMENT_SPACER: UByte = 0x3u
        const val ARGUMENT_END: UByte = 0x4u
    }

    val vector: UShort get() = UShort(memory[0], memory[1])
    val exec: UByte get() = memory[0xf]

    override fun write(port: UByte, value: UByte) {
        super.write(port, value)
        when (port) {
            WRITE -> print(value.toInt().toChar())
            ERROR -> System.err.print(value.toInt().toChar())
        }
    }

    fun input(c: UByte, type: UByte) {
        memory[READ] = c
        memory[TYPE] = type
    }
}