package varvara

import util.*
import uxn.UxnMachine
import java.lang.System

class Console : IODevice {

    companion object {
        const val WRITE: UByte = 0x8u
        const val ERROR: UByte = 0x9u
        const val READ: UInt = 0x2u
        const val TYPE: UInt = 0x7u
    }

    val memory = UByteArray(16)
    val vector: UShort
        get() = UShort(memory[0x10u], memory[0x11u])

    override fun write(port: UByte, value: UByte) {
        memory[port] = value
        when (port) {
            WRITE -> print(value.toInt().toChar())
            ERROR -> System.err.print(value.toInt().toChar())
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
        val lo = memory[port - 1u]
        return UShort(lo, hi)
    }

    fun consoleInput(uxn: UxnMachine, c: UByte, type: UByte) {
        memory[READ] = c
        memory[TYPE] = type
        if (vector > 0u) {
            uxn.eval(vector)
        }
    }
}