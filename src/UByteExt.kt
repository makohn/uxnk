
operator fun UByteArray.get(index: UByte): UByte = this[index.toInt()]
operator fun UByteArray.get(index: UShort): UByte = this[index.toInt()]
operator fun UByteArray.set(index: UByte, value: UByte) {
    this[index.toInt()] = value
}

operator fun UByteArray.get(index: UInt): UByte = this[index.toUShort().toInt()]
operator fun UByteArray.set(index: UInt, value: UByte) {
    this[index.toUShort().toInt()] = value
}