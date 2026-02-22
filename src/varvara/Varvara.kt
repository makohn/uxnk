package varvara

import uxn.UxnDevice
import uxn.UxnMachine
import varvara.device.*

class Varvara : UxnDevice {

    val ram = Array(16) { UByteArray(0x10000) }
    val uxn = UxnMachine(this, ram[0])
    val system = SystemDevice(this)
    val console = ConsoleDevice(this)
    val screen = ScreenDevice(this)
    val audio = Array(4) { AudioDevice(it, this) }
    val controller = ControllerDevice()
    val mouse = MouseDevice()
    val file = Array(2) { FileDevice(this) }
    val dateTime = DateTimeDevice()

    private fun Device(port: UByte) = when (val device = port and 0xf0u) {
        0x00u.toUByte() -> system
        0x10u.toUByte() -> console
        0x20u.toUByte() -> screen
        0x30u.toUByte() -> audio[0]
        0x40u.toUByte() -> audio[1]
        0x50u.toUByte() -> audio[2]
        0x60u.toUByte() -> audio[3]
        0x80u.toUByte() -> controller
        0x90u.toUByte() -> mouse
        0xa0u.toUByte() -> file[0]
        0xb0u.toUByte() -> file[1]
        0xc0u.toUByte() -> dateTime
        else -> error("Unsupported device: ${device.toString(16)}")
    }

    override fun output(port: UByte, value: UByte) = Device(port).write(port and 0xfu, value)
    override fun outputShort(port: UByte, value: UShort) = Device(port).writeShort(port and 0xfu, value)
    override fun input(port: UByte) = Device(port).read(port and 0xfu)
    override fun inputShort(port: UByte) = Device(port).readShort(port and 0xfu)
}