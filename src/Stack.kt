class UxnStack {

    private val bytes = UByteArray(256)
    private var ptr: UByte = 255u
    private var keepPtr: UByte? = null

    fun push(element: UByte) {
        bytes[++ptr] = element
    }

    fun pushShort(element: UShort) {
        push((element.toUInt() shr 8).toUByte())
        push(element.toUByte())
    }

    fun pop(): UByte {
        return bytes[ptr--]
    }

    fun popShort(): UShort {
        return (pop() + (pop().toUInt() shl 8).toUShort()).toUShort()
    }

    fun beginPop() {
        keepPtr = ptr
    }

    fun endPop(keep: Boolean) {
        if (keep) {
            ptr = keepPtr!!
        }
        keepPtr = null
    }

    override fun toString(): String {
        return bytes.joinToString(" ") { it.toString(16) }
    }
}