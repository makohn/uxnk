package uxn

import util.*

class UxnStack {

    private val bytes = UByteArray(256)
    var ptr: UByte = 255u
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

    override fun toString() = buildString {
        val p = ptr.toInt()
        val start = (p - 8) and 0xff

        append(if (start != 0) " " else "|")

        var i = start
        while (i != p) {
            append(bytes[i].toString(16).padStart(2, '0'))
            append(if (i == 0xff) "|" else " ")
            i = (i + 1) and 0xff
        }

        append("<")
        append(p.toString(16).padStart(2, '0'))
    }
}