import varvara.Controller
import varvara.Mouse
import varvara.Screen
import varvara.Varvara
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.awt.image.BufferedImage
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

class ControllerListener(varvara: Varvara) : KeyListener {

    private val controller = varvara.controller
    private val machine = varvara.machine

    private fun onKeyEvent(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_UP -> controller.setButton(Controller.UP)
            KeyEvent.VK_DOWN -> controller.setButton(Controller.DOWN)
            KeyEvent.VK_LEFT -> controller.setButton(Controller.LEFT)
            KeyEvent.VK_RIGHT -> controller.setButton(Controller.RIGHT)
            KeyEvent.VK_CONTROL -> controller.setButton(Controller.A)
            KeyEvent.VK_ALT -> controller.setButton(Controller.B)
            KeyEvent.VK_SHIFT -> controller.setButton(Controller.SELECT)
            KeyEvent.VK_HOME -> controller.setButton(Controller.START)
            else -> return
        }
        machine.eval(controller.vector)
        controller.setKey(0)
    }

    override fun keyTyped(e: KeyEvent) {
        var keyCode = e.keyChar.code
        if (keyCode in 0x0..<0x80) {
            if (e.isControlDown && keyCode in 0x1..0x1a) {
                keyCode += if (e.isShiftDown) 0x40 else 0x60
            }
            controller.setKey(keyCode)
            machine.eval(controller.vector)
            controller.setKey(0)
        }
    }

    override fun keyPressed(e: KeyEvent) = onKeyEvent(e)
    override fun keyReleased(e: KeyEvent) = onKeyEvent(e)
}

class MouseEventListener(varvara: Varvara, private val screen: ScreenPanel) : MouseListener, MouseMotionListener, MouseWheelListener {

    companion object {
        const val WINDOW_BORDER = 5
    }

    private val mouse = varvara.mouse
    private val machine = varvara.machine

    private fun onMouseMoved(e: MouseEvent) {
        // TODO: This range check doesn't seem to be the optimal way to solve this.
        //  Also x y don't seem to be correct in perifs test when compared to SDL2 implementation
        if (e.x in WINDOW_BORDER..screen.width-WINDOW_BORDER && e.y in WINDOW_BORDER..screen.height-WINDOW_BORDER) {
            mouse.setXY(e.x, e.y)
            machine.eval(mouse.vector)
        }
    }

    private fun onMouseClicked(e: MouseEvent) {
        when (e.button) {
            MouseEvent.BUTTON1 -> mouse.setButton(Mouse.BUTTON_1)
            MouseEvent.BUTTON2 -> mouse.setButton(Mouse.BUTTON_2)
            MouseEvent.BUTTON3 -> mouse.setButton(Mouse.BUTTON_3)
            else -> return
        }
        machine.eval(mouse.vector)
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        // TODO: Set scrollX and scrollY
    }

    override fun mouseMoved(e: MouseEvent) = onMouseMoved(e)
    override fun mouseDragged(e: MouseEvent) = onMouseMoved(e)
    override fun mousePressed(e: MouseEvent) = onMouseClicked(e)
    override fun mouseReleased(e: MouseEvent) = onMouseClicked(e)

    // No-Ops
    override fun mouseClicked(e: MouseEvent?) = Unit
    override fun mouseEntered(e: MouseEvent?) = Unit
    override fun mouseExited(e: MouseEvent?) = Unit
}

fun main() {
    val varvara = Varvara()
    val uxn = varvara.machine
    val screenPanel = ScreenPanel(varvara.screen)
    val rom = File("rom/perifs.rom").readBytes().toUByteArray()
    uxn.loadRom(rom)

    val timer = Timer(1000 / 60) {
        uxn.eval(varvara.screen.vector)
        screenPanel.repaint()
    }

    thread {
        uxn.eval()
        timer.start()
    }

    val frame = JFrame("UXN")
    val controllerListener = ControllerListener(varvara)
    val mouseEventListener = MouseEventListener(varvara, screenPanel)
    frame.addKeyListener(controllerListener)
    frame.addMouseMotionListener(mouseEventListener)
    frame.addMouseListener(mouseEventListener)

    frame.cursor = frame.toolkit.createCustomCursor(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), Point(), null)

    screenPanel.background = Color.BLACK // TODO: Make dynamic
    frame.add(screenPanel)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}