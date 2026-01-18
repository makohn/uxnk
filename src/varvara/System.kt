package varvara

class System : IODevice {

    companion object {
        const val METADATA: UByte = 0x6u
        const val RED: UByte = 0x8u
        const val GREEN: UByte = 0xau
        const val BLUE: UByte = 0xcu
    }

    var metadata: UShort = 0x0u; private set
    var red: UShort = 0xf07fu; private set
    var green: UShort = 0xf0d6u; private set
    var blue: UShort = 0xf0b2u; private set

    override fun write(port: UByte, value: UByte) {
        TODO("Not yet implemented")
    }

    override fun writeShort(port: UByte, value: UShort) {
        when (port) {
            METADATA -> metadata = value
            RED -> red = value
            GREEN -> green = value
            BLUE -> blue = value
            else -> error("writeShort port=${port.toString(16)}, value=${value.toString(16)}")
        }
    }

    override fun read(port: UByte): UByte {
        TODO("Not yet implemented")
    }

    override fun readShort(port: UByte): UShort {
        return when (port) {
            METADATA -> metadata
            RED -> red
            GREEN -> green
            BLUE -> blue
            else -> error("readShort port=${port.toString(16)}")
        }
    }
}