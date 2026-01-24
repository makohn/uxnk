package util

const val UShort_0: UShort = 0u
const val UByte_0: UByte = 0u

operator fun UByteArray.get(index: UByte): UByte = this[index.toInt()]
operator fun UByteArray.set(index: UByte, value: UByte) {
    this[index.toInt()] = value
}

operator fun UByteArray.get(index: UShort): UByte = this[index.toInt()]
operator fun UByteArray.set(index: UShort, value: UByte) {
    this[index.toInt()] = value
}

operator fun UByteArray.get(index: UInt): UByte = this[index.toUShort().toInt()]
operator fun UByteArray.set(index: UInt, value: UByte) {
    this[index.toUShort().toInt()] = value
}

fun UShort(hi: UByte, lo: UByte) = ((hi.toUInt() shl 8) + lo).toUShort()
val UShort.hi: UByte get() = (this.toUInt() shr 8).toUByte()
val UShort.lo: UByte get() = this.toUByte()