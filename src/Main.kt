import uxn.*
import java.io.File

class Console : Device {

    val mem = UByteArray(0x100)
    var consoleVector: UShort = 0u; private set

    override fun out(port: UByte, value: UByte) {
        mem[port] = value
        when (port) {
            0x11u.toUByte() -> consoleVector = UShort(value, mem[0x10u])
            0x18u.toUByte() -> print(value.toInt().toChar())
            0x19u.toUByte() -> System.err.print(value.toInt().toChar())
        }
    }

    override fun outShort(port: UByte, value: UShort) {
        val (hi, lo) = value.toUBytes()
        out(port, hi)
        out((port + 1u).toUByte(), lo)
    }

    override fun inp(port: UByte): UByte {
        return mem[port]
    }

    override fun inpShort(port: UByte): UShort {
        val hi = mem[port]
        val lo = mem[port - 1u]
        return UShort(lo, hi)
    }

    fun consoleInput(uxn: UxnMachine, c: UByte, type: UByte) {
        mem[0x12u] = c
        mem[0x17u] = type
        if (consoleVector > 0u) {
            uxn.eval(consoleVector)
        }
    }
}

fun main() {
    val console = Console()
    val rom = File("rom/sierpinski.rom").readBytes().toUByteArray()
    val uxn = UxnMachine(console)
    uxn.loadRom(rom)
    if (uxn.eval() && console.consoleVector > 0u) {
        while (console.mem[0x0fu] == UByte_0) {
            val c = readln()[0].code.toUByte()
            console.consoleInput(uxn, c, 1u)
        }
    }
}

fun UShort.toUBytes(): UByteArray {
    val hi = (this.toUInt() shr 8).toUByte()
    val lo = this.toUByte()
    return ubyteArrayOf(hi, lo)
}