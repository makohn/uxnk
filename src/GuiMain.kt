import uxn.UxnMachine
import varvara.Screen
import varvara.Varvara
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.concurrent.thread

class ScreenPanel(private val screen: Screen) : JPanel() {

    init {
        preferredSize = Dimension(screen.bg.width, screen.bg.height)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        g!!.drawImage(screen.bg, 0, 0, null)
        g.drawImage(screen.fg, 0, 0, null)
    }
}

fun main() {
    val varvara = Varvara()
    val uxn = UxnMachine(varvara)
    varvara.machine = uxn
    val screenPanel = ScreenPanel(varvara.screen)
    val rom = File("rom/label.rom").readBytes().toUByteArray()
    uxn.loadRom(rom)

    val timer = Timer(1000 / 60) {
        screenPanel.repaint()
    }
    timer.start()

    thread {
        uxn.eval()
    }

    val frame = JFrame("UXN")

    screenPanel.background = Color.WHITE
    frame.add(screenPanel)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}