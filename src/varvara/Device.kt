package varvara

interface Device {
    fun output(port: UByte, value: UByte)
    fun input(port: UByte): UByte
}