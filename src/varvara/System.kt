package varvara

import uxn.*

class System : IODevice {

    private val memory = UByteArray(16)

    val red: UShort get() = UShort(memory[0x9u], memory[0x8u])
    val green: UShort get() = UShort(memory[0xbu], memory[0xau])
    val blue: UShort get() = UShort(memory[0xdu], memory[0xcu])

    override fun write(port: UByte, value: UByte) {
        TODO("Not yet implemented")
    }

    override fun writeShort(port: UByte, value: UShort) {
        TODO("Not yet implemented")
    }

    override fun read(port: UByte): UByte {
        TODO("Not yet implemented")
    }

    override fun readShort(port: UByte): UShort {
        TODO("Not yet implemented")
    }
}