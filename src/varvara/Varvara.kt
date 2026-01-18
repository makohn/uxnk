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
    val screen = Screen()

    override fun out(port: UByte, value: UByte) {
        val device = port and 0xf0u
        val p = port and 0xfu
        when (device) {
            DEV_SYSTEM -> Unit
            DEV_CONSOLE -> console.write(p, value)
            DEV_SCREEN -> screen.write(p, value)
            DEV_CONTROLLER -> Unit
            DEV_MOUSE -> Unit
            DEV_FILE_A -> Unit
            DEV_FILE_B -> Unit
            DEV_TIME -> Unit
        }
    }

    override fun outShort(port: UByte, value: UShort) {
        val device = port and 0xf0u
        val p = port and 0xfu
        when (device) {
            DEV_SYSTEM -> Unit
            DEV_CONSOLE -> console.writeShort(p, value)
            DEV_SCREEN -> screen.writeShort(p, value)
            DEV_CONTROLLER -> Unit
            DEV_MOUSE -> Unit
            DEV_FILE_A -> Unit
            DEV_FILE_B -> Unit
            DEV_TIME -> Unit
        }
    }

    override fun inp(port: UByte): UByte {
        return 0x0u
    }

    override fun inpShort(port: UByte): UShort {
        val device = port and 0xf0u
        val p = port and 0xfu
        return when (device) {
//            DEV_SYSTEM -> Unit
//            DEV_CONSOLE -> console.writeShort(p, value)
            DEV_SCREEN -> screen.readShort(p)
//            DEV_CONTROLLER -> Unit
//            DEV_MOUSE -> Unit
//            DEV_FILE_A -> Unit
//            DEV_FILE_B -> Unit
//            DEV_TIME -> Unit
            else -> 0x0u
        }
    }
}