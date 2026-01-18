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
        g!!.apply {
            drawImage(screen.bg, 0, 0, null)
            drawImage(screen.fg, 0, 0, null)
        }
    }
}

fun main() {
    val varvara = Varvara()
    val uxn = UxnMachine(varvara)
    varvara.machine = uxn
    val screenPanel = ScreenPanel(varvara.screen)
    val rom = File("rom/catclock.rom").readBytes().toUByteArray()
    uxn.loadRom(rom)

    varvara.screen.repaint = { screenPanel.repaint() }

    val timer = Timer(1000 / 60) {
        uxn.eval(varvara.screen.vector)
    }

    thread {
        uxn.eval()
        timer.start()
    }

    val frame = JFrame("UXN")

    screenPanel.background = Color.WHITE
    frame.add(screenPanel)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}