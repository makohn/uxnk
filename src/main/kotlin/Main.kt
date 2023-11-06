import java.io.File

fun main() {
    val program = File("src/main/resources","hello.rom").readBytes().toUByteArray()
    println(program.joinToString { it.toString(16) })
    val uxn = UxnMachine()
    uxn.loadRom(program)

    val s = 8 shr 1

    while (true) if (uxn.step() == UxnMachine.MachineState.Stopped) break

    for (code in OpCode.entries) {
        println("$code -> ${code.base()} | s=${code.short()} | r=${code.ret()} | k=${code.keep()}")
    }
}