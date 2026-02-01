import varvara.device.ControllerDevice
import varvara.device.MouseDevice
import varvara.device.ScreenDevice
import varvara.Varvara
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
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

class ScreenPanel(private val screen: ScreenDevice) : JPanel() {

    init {
        preferredSize = Dimension(screen.bg.width, screen.bg.height)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        (g as Graphics2D).apply {
            setRenderingHint(RenderingHints.KEY_RESOLUTION_VARIANT, RenderingHints.VALUE_RESOLUTION_VARIANT_DEFAULT)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
            setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED)
            setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED)
            drawImage(screen.bg, 0, 0, null)
            drawImage(screen.fg, 0, 0, null)
        }
    }
}

class ControllerListener(varvara: Varvara) : KeyListener {

    private val controller = varvara.controller
    private val uxn = varvara.uxn

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_UP -> controller.setButton(ControllerDevice.UP)
            KeyEvent.VK_DOWN -> controller.setButton(ControllerDevice.DOWN)
            KeyEvent.VK_LEFT -> controller.setButton(ControllerDevice.LEFT)
            KeyEvent.VK_RIGHT -> controller.setButton(ControllerDevice.RIGHT)
            KeyEvent.VK_CONTROL -> controller.setButton(ControllerDevice.A)
            KeyEvent.VK_ALT -> controller.setButton(ControllerDevice.B)
            KeyEvent.VK_SHIFT -> controller.setButton(ControllerDevice.SELECT)
            KeyEvent.VK_HOME -> controller.setButton(ControllerDevice.START)
            else -> return
        }
        uxn.eval(controller.vector)
        controller.setKey(0)
    }

    override fun keyReleased(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_UP -> controller.unsetButton(ControllerDevice.UP)
            KeyEvent.VK_DOWN -> controller.unsetButton(ControllerDevice.DOWN)
            KeyEvent.VK_LEFT -> controller.unsetButton(ControllerDevice.LEFT)
            KeyEvent.VK_RIGHT -> controller.unsetButton(ControllerDevice.RIGHT)
            KeyEvent.VK_CONTROL -> controller.unsetButton(ControllerDevice.A)
            KeyEvent.VK_ALT -> controller.unsetButton(ControllerDevice.B)
            KeyEvent.VK_SHIFT -> controller.unsetButton(ControllerDevice.SELECT)
            KeyEvent.VK_HOME -> controller.unsetButton(ControllerDevice.START)
            else -> return
        }
        uxn.eval(controller.vector)
        controller.setKey(0)
    }

    override fun keyTyped(e: KeyEvent) {
        var keyCode = e.keyChar.code
        if (keyCode in 0x0..<0x80) {
            if (e.isControlDown && keyCode in 0x1..0x1a) {
                keyCode += if (e.isShiftDown) 0x40 else 0x60
            }
            controller.setKey(keyCode)
            uxn.eval(controller.vector)
            controller.setKey(0)
        }
    }
}

class MouseEventListener(varvara: Varvara, private val screen: ScreenPanel) : MouseListener, MouseMotionListener, MouseWheelListener {

    companion object {
        const val WINDOW_BORDER = 5
    }

    private val mouse = varvara.mouse
    private val uxn = varvara.uxn

    private fun onMouseMoved(e: MouseEvent) {
        // TODO: This range check doesn't seem to be the optimal way to solve this.
        //  Also x y don't seem to be correct in perifs test when compared to SDL2 implementation
        if (e.x in WINDOW_BORDER..screen.width-WINDOW_BORDER && e.y in WINDOW_BORDER..screen.height-WINDOW_BORDER) {
            mouse.setPos(e.x, e.y)
            uxn.eval(mouse.vector)
        }
    }

    override fun mousePressed(e: MouseEvent) {
        when (e.button) {
            MouseEvent.BUTTON1 -> mouse.setButton(MouseDevice.BUTTON_1)
            MouseEvent.BUTTON2 -> mouse.setButton(MouseDevice.BUTTON_2)
            MouseEvent.BUTTON3 -> mouse.setButton(MouseDevice.BUTTON_3)
            else -> return
        }
        uxn.eval(mouse.vector)
    }

    override fun mouseReleased(e: MouseEvent) {
        when (e.button) {
            MouseEvent.BUTTON1 -> mouse.unsetButton(MouseDevice.BUTTON_1)
            MouseEvent.BUTTON2 -> mouse.unsetButton(MouseDevice.BUTTON_2)
            MouseEvent.BUTTON3 -> mouse.unsetButton(MouseDevice.BUTTON_3)
            else -> return
        }
        uxn.eval(mouse.vector)
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        // TODO: Set scrollX and scrollY
    }

    override fun mouseMoved(e: MouseEvent) = onMouseMoved(e)
    override fun mouseDragged(e: MouseEvent) = onMouseMoved(e)

    // No-Ops
    override fun mouseClicked(e: MouseEvent?) = Unit
    override fun mouseEntered(e: MouseEvent?) = Unit
    override fun mouseExited(e: MouseEvent?) = Unit
}

fun main() {
    val varvara = Varvara()
    val uxn = varvara.uxn
    val screenPanel = ScreenPanel(varvara.screen)
    val rom = File("rom/catclock.rom").readBytes().toUByteArray()
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