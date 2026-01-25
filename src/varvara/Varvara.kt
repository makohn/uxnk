package varvara

import uxn.UxnDevice
import uxn.UxnMachine

class Varvara : UxnDevice {

    companion object {
        const val SYSTEM: UByte = 0x00u
        const val CONSOLE: UByte = 0x10u
        const val SCREEN: UByte = 0x20u
        const val CONTROLLER: UByte = 0x80u
        const val MOUSE: UByte = 0x90u
        const val FILE_A: UByte = 0xa0u
        const val FILE_B: UByte = 0xb0u
        const val TIME: UByte = 0xc0u
    }

    val machine = UxnMachine(this)
    private val console = Console()
    private val dateTime = DateTime()
    val system = System()
    val screen = Screen(this)
    val controller = Controller()
    val mouse = Mouse()

    override fun output(port: UByte, value: UByte) {
        val device = port and 0xf0u
        val p = port and 0xfu
        when (device) {
            SYSTEM -> system.write(p, value)
            CONSOLE -> console.write(p, value)
            SCREEN -> screen.write(p, value)
            CONTROLLER -> controller.write(p, value)
            MOUSE -> mouse.write(p, value)
            FILE_A -> Unit
            FILE_B -> Unit
            TIME -> dateTime.write(p, value)
        }
    }

    override fun outputShort(port: UByte, value: UShort) {
        val device = port and 0xf0u
        val p = port and 0xfu
        when (device) {
            SYSTEM -> system.writeShort(p, value)
            CONSOLE -> console.writeShort(p, value)
            SCREEN -> screen.writeShort(p, value)
            CONTROLLER -> controller.writeShort(p, value)
            MOUSE -> mouse.writeShort(p, value)
            FILE_A -> Unit
            FILE_B -> Unit
            TIME -> dateTime.writeShort(p, value)
        }
    }

    override fun input(port: UByte): UByte {
        val device = port and 0xf0u
        val p = port and 0xfu
        return when (device) {
            SYSTEM -> system.read(p)
//            CONSOLE -> console.writeShort(p, value)
            SCREEN -> screen.read(p)
            CONTROLLER -> controller.read(p)
            MOUSE -> mouse.read(p)
//            FILE_A -> Unit
//            FILE_B -> Unit
            TIME -> dateTime.read(p)
            else -> 0x0u
        }
    }

    override fun inputShort(port: UByte): UShort {
        val device = port and 0xf0u
        val p = port and 0xfu
        return when (device) {
            SYSTEM -> system.readShort(p)
//            CONSOLE -> console.writeShort(p, value)
            SCREEN -> screen.readShort(p)
            CONTROLLER -> controller.readShort(p)
            MOUSE -> mouse.readShort(p)
//            FILE_A -> Unit
//            FILE_B -> Unit
            TIME -> dateTime.readShort(p)
            else -> 0x0u
        }
    }
}