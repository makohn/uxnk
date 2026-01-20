package varvara

class Controller : IODevice {

    companion object {
        const val VECTOR: UByte = 0x0u
        const val BUTTON: UByte = 0x2u
        const val KEY: UByte = 0x3u
        const val BUTTON2: UByte = 0x4u
        const val KEY2: UByte = 0x5u
        const val BUTTON3: UByte = 0x6u
        const val KEY3: UByte = 0x7u
        const val BUTTON4: UByte = 0x8u
        const val KEY4: UByte = 0x9u
    }

    var vector: UShort = 0x0u; private set
    private var button: UByte = 0x0u
    private var button2: UByte = 0x0u
    private var button3: UByte = 0x0u
    private var button4: UByte = 0x0u
    private var key: UByte = 0x0u
    private var key2: UByte = 0x0u
    private var key3: UByte = 0x0u
    private var key4: UByte = 0x0u


    override fun write(port: UByte, value: UByte) {
        when (port) {
            BUTTON -> button = value
            BUTTON2 -> button2 = value
            BUTTON3 -> button3 = value
            BUTTON4 -> button4 = value
            KEY -> key = value
            KEY2 -> key2 = value
            KEY3 -> key3 = value
            KEY4 -> key4 = value
            else -> error("write port=${port.toString(16)}, value=${value.toString(16)}")
        }
    }

    override fun writeShort(port: UByte, value: UShort) {
        when (port) {
            VECTOR -> vector = value
            else -> error("writeShort port=${port.toString(16)}, value=${value.toString(16)}")
        }
    }

    override fun read(port: UByte): UByte {
        return when (port) {
            BUTTON -> button
            BUTTON2 -> button2
            BUTTON3 -> button3
            BUTTON4 -> button4
            KEY -> key
            KEY2 -> key2
            KEY3 -> key3
            KEY4 -> key4
            else -> error("read port=${port.toString(16)}")
        }
    }

    override fun readShort(port: UByte): UShort {
        return when (port) {
            VECTOR -> vector
            else -> error("readShort port=${port.toString(16)}")
        }
    }

    fun setKey(key: Int) {
        write(KEY, key.toUByte())
    }
}