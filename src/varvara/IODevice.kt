package varvara

interface IODevice {
    fun write(port: UByte, value: UByte)
    fun writeShort(port: UByte, value: UShort)
    fun read(port: UByte): UByte
    fun readShort(port: UByte): UShort
}