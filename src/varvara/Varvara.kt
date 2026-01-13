package varvara

import uxn.Device
import uxn.UxnMachine

const val DEV_SYSTEM: UByte = 0x00u
const val DEV_CONSOLE: UByte = 0x10u
const val DEV_SCREEN: UByte = 0x20u
const val DEV_CONTROLLER: UByte = 0x80u
const val DEV_MOUSE: UByte = 0x90u
const val DEV_FILE_A: UByte = 0xa0u
const val DEV_FILE_B: UByte = 0xb0u
const val DEV_TIME: UByte = 0xc0u

class Varvara : Device {
    private val machine = UxnMachine(this)
    private val console = Console()

    override fun out(port: UByte, value: UByte) {
        val device = port and 0xf0u
        val p = port and 0xfu
        when (device) {
            DEV_SYSTEM -> TODO()
            DEV_CONSOLE -> console.output(p, value)
            DEV_SCREEN -> TODO()
            DEV_CONTROLLER -> TODO()
            DEV_MOUSE -> TODO()
            DEV_FILE_A -> TODO()
            DEV_FILE_B -> TODO()
            DEV_TIME -> TODO()
        }
    }

    override fun outShort(port: UByte, value: UShort) {
        TODO("Not yet implemented")
    }

    override fun inp(port: UByte): UByte {
        TODO("Not yet implemented")
    }

    override fun inpShort(port: UByte): UShort {
        TODO("Not yet implemented")
    }
}