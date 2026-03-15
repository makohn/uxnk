package varvara.device

import util.*
import varvara.Device
import varvara.Varvara

class SystemDevice(private val varvara: Varvara) : Device() {

    companion object {
        const val EXPANSION: UByte = 0x2u
        const val WST: UByte = 0x3u
        const val RST: UByte = 0x4u
        const val DEBUG: UByte = 0xeu

        const val FILL: UByte = 0u
        const val COPY_LEFT: UByte = 1u
        const val COPY_RIGHT: UByte = 2u
    }

    private val uxn = varvara.uxn

    val red: UShort get() = UShort(memory[0x8], memory[0x9])
    val green: UShort get() = UShort(memory[0xa], memory[0xb])
    val blue: UShort get() = UShort(memory[0xc], memory[0xd])

    val state: UByte get() = memory[0xfu]

    override fun write(port: UByte, value: UByte) {
        super.write(port, value)
        when (port) {
            WST -> uxn.workingStack.ptr = value
            RST -> uxn.returnStack.ptr = value
            DEBUG -> {
                System.err.println("WST ${uxn.workingStack}")
                System.err.println("RST ${uxn.returnStack}")
            }
        }
    }

    override fun read(port: UByte): UByte {
        return when (port) {
            WST -> uxn.workingStack.ptr
            RST -> uxn.returnStack.ptr
            else -> super.read(port)
        }
    }

    override fun writeShort(port: UByte, value: UShort) {
        super.writeShort(port, value)
        when (port) {
            EXPANSION -> {
                val op = uxn.memory[value]
                val length = UShort(uxn.memory[value + 1u], uxn.memory[value + 2u])
                val bank = UShort(uxn.memory[value + 3u], uxn.memory[value + 4u]).toInt()
                val address = UShort(uxn.memory[value + 5u], uxn.memory[value + 6u])
                if (bank >= varvara.ram.size) return
                when (op) {
                    FILL -> {
                        val destinationValue = uxn.memory[value + 7u]
                        varvara.ram[bank].fill(
                            element = destinationValue,
                            fromIndex = address.toInt(),
                            toIndex = (address + length).toInt()
                        )
                    }
                    COPY_LEFT, COPY_RIGHT -> {
                        val destinationBank = UShort(uxn.memory[value + 7u], uxn.memory[value + 8u]).toInt()
                        val destinationAddress = UShort(uxn.memory[value + 9u], uxn.memory[value + 0xau]).toInt()
                        if (destinationBank >= varvara.ram.size) return
                        varvara.ram[bank].copyInto(
                            destination = varvara.ram[destinationBank],
                            destinationOffset = destinationAddress,
                            startIndex = address.toInt(),
                            endIndex = (address + length).toInt()
                        )
                    }
                }
            }
        }
    }
}