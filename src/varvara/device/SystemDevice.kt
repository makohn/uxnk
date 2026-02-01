package varvara.device

import util.*
import varvara.Device

class SystemDevice : Device() {
    val red: UShort get() = UShort(memory[0x8], memory[0x9])
    val green: UShort get() = UShort(memory[0xa], memory[0xb])
    val blue: UShort get() = UShort(memory[0xc], memory[0xd])
}