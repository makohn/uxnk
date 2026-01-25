package varvara

import util.*

class System : IODevice {

    private val memory = UByteArray(16)

    val red: UShort get() = UShort(memory[0x8], memory[0x9])
    val green: UShort get() = UShort(memory[0xa], memory[0xb])
    val blue: UShort get() = UShort(memory[0xc], memory[0xd])

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
}