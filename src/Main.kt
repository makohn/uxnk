import util.*
import uxn.UxnDevice
import uxn.UxnMachine
import java.io.File

class Console : UxnDevice {

    companion object {
        const val WRITE: UByte = 0x8u
        const val ERROR: UByte = 0x9u
    }

    val mem = UByteArray(16)
    val consoleVector: UShort
        get() = UShort(mem[0x0u], mem[0x1u])

    override fun output(port: UByte, value: UByte) {
        val index = port and 0xfu
        mem[index] = value
        when (index) {
            WRITE -> print(value.toInt().toChar())
            ERROR -> System.err.print(value.toInt().toChar())
        }
    }

    override fun outputShort(port: UByte, value: UShort) {
        output(port, value.hi)
        output((port + 1u).toUByte(), value.lo)
    }

    override fun input(port: UByte): UByte {
        val index = port and 0xfu
        return mem[index]
    }

    override fun inputShort(port: UByte): UShort {
        val index = port and 0xfu
        val hi = mem[index]
        val lo = mem[index - 1u]
        return UShort(lo, hi)
    }

    fun consoleInput(uxn: UxnMachine, c: UByte, type: UByte) {
        mem[0x2u] = c
        mem[0x7u] = type
        if (consoleVector > 0u) {
            uxn.eval(consoleVector)
        }
    }
}

fun main() {
    val console = Console()
    val rom = File("rom/pig.rom").readBytes().toUByteArray()
    val uxn = UxnMachine(console)
    uxn.loadRom(rom)
    if (uxn.eval() && console.consoleVector > 0u) {
        while (console.mem[0x0fu] == UByte_0) {
            val c = readln()[0].code.toUByte()
            console.consoleInput(uxn, c, 1u)
        }
    }
}