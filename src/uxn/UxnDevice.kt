package uxn

interface UxnDevice {
    fun output(port: UByte, value: UByte)
    fun outputShort(port: UByte, value: UShort)
    fun input(port: UByte): UByte
    fun inputShort(port: UByte): UShort
}