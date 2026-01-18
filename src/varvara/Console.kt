package varvara

import toUBytes
import uxn.UShort
import uxn.UxnMachine
import uxn.get
import uxn.set
import java.lang.System

class Console : IODevice {

    companion object {
        const val WRITE: UByte = 0x18u
        const val ERROR: UByte = 0x19u
        const val READ: UInt = 0x12u
        const val TYPE: UInt = 0x17u
    }

    val memory = UByteArray(16)
    val vector: UShort
        get() = UShort(memory[0x11u], memory[0x10u])

    override fun write(port: UByte, value: UByte) {
        val index = port and 0xfu
        memory[index] = value
        when (index) {
            WRITE -> print(value.toInt().toChar())
            ERROR -> System.err.print(value.toInt().toChar())
        }
    }

    override fun writeShort(port: UByte, value: UShort) {
        val (hi, lo) = value.toUBytes()
        write(port, hi)
        write((port + 1u).toUByte(), lo)
    }

    override fun read(port: UByte): UByte {
        val index = port and 0xfu
        return memory[index]
    }

    override fun readShort(port: UByte): UShort {
        val index = port and 0xfu
        val hi = memory[index]
        val lo = memory[index - 1u]
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