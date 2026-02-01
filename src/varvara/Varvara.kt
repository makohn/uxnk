package varvara

import uxn.UxnDevice
import uxn.UxnMachine
import varvara.device.*

class Varvara : UxnDevice {

    companion object {
        const val SYSTEM: UByte = 0x00u
        const val CONSOLE: UByte = 0x10u
        const val SCREEN: UByte = 0x20u
        const val CONTROLLER: UByte = 0x80u
        const val MOUSE: UByte = 0x90u
        const val TIME: UByte = 0xc0u

        val AUDIO = 0x30u..0x60u
        val FILE = 0xa0u..0xb0u
    }

    val uxn = UxnMachine(this)
    val system = SystemDevice()
    val console = ConsoleDevice()
    val screen = ScreenDevice(this)
    val audio = Array(4) { AudioDevice(this) }
    val controller = ControllerDevice()
    val mouse = MouseDevice()
    val file = Array(2) { FileDevice(this) }
    val dateTime = DateTimeDevice()

    override fun output(port: UByte, value: UByte) = Device(port).write(port and 0xfu, value)
    override fun outputShort(port: UByte, value: UShort) = Device(port).writeShort(port and 0xfu, value)
    override fun input(port: UByte) = Device(port).read(port and 0xfu)
    override fun inputShort(port: UByte) = Device(port).readShort(port and 0xfu)

    private fun Device(port: UByte) = when (val device = port and 0xf0u) {
        SYSTEM -> system
        CONSOLE -> console
        SCREEN -> screen
        in AUDIO -> audio[device]
        CONTROLLER -> controller
        MOUSE -> mouse
        in FILE -> file[device]
        TIME -> dateTime
        else -> error("Unsupported device: ${device.toString(16)}")
    }

    private operator fun Array<AudioDevice>.get(device: UByte): AudioDevice {
        return audio[(device.toInt() shr 4) - 0x3]
    }

    private operator fun Array<FileDevice>.get(device: UByte): FileDevice {
        return file[(device.toInt() shr 4) - 0xa]
    }
}