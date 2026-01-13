package uxn

// https://wiki.xxiivv.com/site/uxntal_reference.html
const val BRK: UByte = 0x00_u
const val INC: UByte = 0x01_u
const val POP: UByte = 0x02_u
const val NIP: UByte = 0x03_u
const val SWP: UByte = 0x04_u
const val ROT: UByte = 0x05_u
const val DUP: UByte = 0x06_u
const val OVR: UByte = 0x07_u
const val EQU: UByte = 0x08_u
const val NEQ: UByte = 0x09_u
const val GTH: UByte = 0x0a_u
const val LTH: UByte = 0x0b_u
const val JMP: UByte = 0x0c_u
const val JCN: UByte = 0x0d_u
const val JSR: UByte = 0x0e_u
const val STH: UByte = 0x0f_u
const val LDZ: UByte = 0x10_u
const val STZ: UByte = 0x11_u
const val LDR: UByte = 0x12_u
const val STR: UByte = 0x13_u
const val LDA: UByte = 0x14_u
const val STA: UByte = 0x15_u
const val DEI: UByte = 0x16_u
const val DEO: UByte = 0x17_u
const val ADD: UByte = 0x18_u
const val SUB: UByte = 0x19_u
const val JCI: UByte = 0x20_u
const val MUL: UByte = 0x1a_u
const val DIV: UByte = 0x1b_u
const val AND: UByte = 0x1c_u
const val ORA: UByte = 0x1d_u
const val EOR: UByte = 0x1e_u
const val SFT: UByte = 0x1f_u
const val JMI: UByte = 0x40_u
const val JSI: UByte = 0x60_u
const val LIT: UByte = 0x80_u

const val UShort_0: UShort = 0u
const val UByte_0: UByte = 0u

typealias OpCode = UByte

val OpCode.base: UByte get() = when {
    this and 0x1fu > 0u -> this and 0x1fu
    this and 0x9fu == UByte_0 -> this
    this and 0x9fu == 0x80u.toUByte() -> LIT
    else -> error("Unreachable")
}

val OpCode.shortFlag: Boolean get() = test(0x20u) && test(0x9fu)
val OpCode.returnFlag: Boolean get() = test(0x40u) && test(0x9fu)
val OpCode.keepFlag: Boolean get() = test(0x80u) && test(0x1fu)

internal fun UByte.test(mask: UByte) = (this and mask) > UByte_0
fun UShort(lo: UByte, hi: UByte) = ((hi.toUInt() shl 8) + lo).toUShort()

fun OpCode.str() = "${
    when (this.base) {
        BRK -> "BRK"
        INC -> "INC"
        POP -> "POP"
        NIP -> "NIP"
        SWP -> "SWP"
        ROT -> "ROT"
        DUP -> "DUP"
        OVR -> "OVR"
        EQU -> "EQU"
        NEQ -> "NEQ"
        GTH -> "GTH"
        LTH -> "LTH"
        JMP -> "JMP"
        JCN -> "JCN"
        JSR -> "JSR"
        STH -> "STH"
        LDZ -> "LDZ"
        STZ -> "STZ"
        LDR -> "LDR"
        STR -> "STR"
        LDA -> "LDA"
        STA -> "STA"
        DEI -> "DEI"
        DEO -> "DEO"
        ADD -> "ADD"
        SUB -> "SUB"
        JCI -> "JCI"
        MUL -> "MUL"
        DIV -> "DIV"
        AND -> "AND"
        ORA -> "ORA"
        EOR -> "EOR"
        SFT -> "SFT"
        JMI -> "JMI"
        JSI -> "JSI"
        LIT -> "LIT"
        else -> "???"
    }
}${if (shortFlag) "2" else ""}${if (keepFlag) "k" else ""}${if (returnFlag) "r" else ""}"