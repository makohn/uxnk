package varvara

import util.*

abstract class Device {

    protected val memory = UByteArray(16)

    open fun write(port: UByte, value: UByte) {
        memory[port] = value
    }

    open fun writeShort(port: UByte, value: UShort) {
        write(port, value.hi)
        write((port + 1u).toUByte(), value.lo)
    }

    open fun read(port: UByte): UByte {
        return memory[port]
    }

    open fun readShort(port: UByte): UShort {
        val hi = memory[port]
        val lo = memory[port + 1u]
        return UShort(hi, lo)
    }
}