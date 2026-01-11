import java.io.File

fun main() {
    val rom = File("rom/fib.rom").readBytes().toUByteArray()
    val uxn = UxnMachine()
    uxn.loadRom(rom)
    while (true) if (uxn.step() == UxnMachine.MachineState.Stopped) break
}