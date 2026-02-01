import varvara.Varvara
import java.io.File
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
    val romFile = args[0]
    val varvara = Varvara()
    val uxn = varvara.uxn
    val rom = File(romFile).readBytes().toUByteArray()
    uxn.loadRom(rom)
    SwingUtilities.invokeLater { Gui(varvara).start() }
}