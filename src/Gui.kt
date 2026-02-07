import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import varvara.Varvara
import varvara.device.ControllerDevice
import varvara.device.MouseDevice
import varvara.device.ScreenDevice
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

class Gui(
    private val varvara: Varvara,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : JFrame("uxnk") {

    private lateinit var screenPanel: ScreenPanel

    fun start() {
        screenPanel = ScreenPanel(varvara.screen)
        val controllerListener = ControllerListener(scope, screenPanel, this)
        val mouseEventListener = MouseEventListener(scope)

        val timer = Timer(1000 / 60) {
            events.trySend(Event.Repaint)
        }
        timer.start()

        addKeyListener(controllerListener)
        screenPanel.addMouseMotionListener(mouseEventListener)
        screenPanel.addMouseListener(mouseEventListener)
        screenPanel.addMouseWheelListener(mouseEventListener)

        screenPanel.background = varvara.screen.colors[0]
        add(screenPanel)
        pack()

        cursor = toolkit.createCustomCursor(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), Point(), null)
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        isVisible = true
    }

    fun redraw() {
        screenPanel.repaint()
    }

    private class ScreenPanel(private val screen: ScreenDevice) : JPanel() {

        var scale = 1
            set(value) {
                field = value
                preferredSize = Dimension(screen.width.toInt() * value, screen.height.toInt() * value)
                revalidate()
            }

        init {
            preferredSize = Dimension(screen.width.toInt(), screen.height.toInt())
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            (g as Graphics2D).apply {
                setRenderingHint(RenderingHints.KEY_RESOLUTION_VARIANT, RenderingHints.VALUE_RESOLUTION_VARIANT_DEFAULT)
                setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
                setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED)
                setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED)
                val s = scale.toDouble()
                scale(s, s)
                val w = screen.wmar2
                val h = screen.hmar2
                drawImage(screen.bg, 0, 0, w, h, 8, 8, w + 8, h + 8, null)
                drawImage(screen.fg, 0, 0, w, h, 8, 8, w + 8, h + 8, null)
            }
        }
    }

    private class ControllerListener(private val scope: CoroutineScope, private val screen: ScreenPanel, private val gui: JFrame) : KeyListener {

        private inline fun onKey(keyCode: Int, fn: (UByte) -> Unit) {
            when (keyCode) {
                KeyEvent.VK_UP -> fn(ControllerDevice.UP)
                KeyEvent.VK_DOWN -> fn(ControllerDevice.DOWN)
                KeyEvent.VK_LEFT -> fn(ControllerDevice.LEFT)
                KeyEvent.VK_RIGHT -> fn(ControllerDevice.RIGHT)
                KeyEvent.VK_CONTROL -> fn(ControllerDevice.A)
                KeyEvent.VK_ALT -> fn(ControllerDevice.B)
                KeyEvent.VK_SHIFT -> fn(ControllerDevice.SELECT)
                KeyEvent.VK_HOME -> fn(ControllerDevice.START)
            }
        }

        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_F1) {
                screen.scale = 1 + (screen.scale % 3)
                gui.pack()
            }
            scope.launch {
                onKey(e.keyCode) { events.send(Event.ButtonPressed(it)) }
            }
        }

        override fun keyReleased(e: KeyEvent) {
            scope.launch {
                onKey(e.keyCode) { events.send(Event.ButtonReleased(it)) }
            }
        }

        override fun keyTyped(e: KeyEvent) {
            var keyCode = e.keyChar.code
            if (keyCode in 0x0..<0x80) {
                if (e.isControlDown && keyCode in 0x1..0x1a) {
                    keyCode += if (e.isShiftDown) 0x40 else 0x60
                }
                scope.launch {
                    events.send(Event.KeyTyped(keyCode))
                }
            }
        }
    }

    private class MouseEventListener(val scope: CoroutineScope) : MouseListener,
        MouseMotionListener, MouseWheelListener {

        private fun onMouseMoved(e: MouseEvent) {
            events.trySend(Event.MouseMoved(e.x, e.y))
        }

        private inline fun onMouse(button: Int, fn: (UByte) -> Unit) {
            when (button) {
                MouseEvent.BUTTON1 -> fn(MouseDevice.BUTTON_1)
                MouseEvent.BUTTON2 -> fn(MouseDevice.BUTTON_2)
                MouseEvent.BUTTON3 -> fn(MouseDevice.BUTTON_3)
            }
        }

        override fun mousePressed(e: MouseEvent) {
            scope.launch {
                onMouse(e.button) { events.send(Event.MousePressed(it)) }
            }
        }

        override fun mouseReleased(e: MouseEvent) {
            scope.launch {
                onMouse(e.button) { events.send(Event.MouseReleased(it)) }
            }
        }

        override fun mouseWheelMoved(e: MouseWheelEvent) {
            val dir = e.wheelRotation.coerceIn(-1, 1)
            scope.launch {
                if (e.isShiftDown) {
                    events.send(Event.MouseScrolled(dir, 0))
                } else {
                    events.send(Event.MouseScrolled(0, dir))
                }
            }
        }

        override fun mouseMoved(e: MouseEvent) = onMouseMoved(e)
        override fun mouseDragged(e: MouseEvent) = onMouseMoved(e)

        // No-Ops
        override fun mouseClicked(e: MouseEvent?) = Unit
        override fun mouseEntered(e: MouseEvent?) = Unit
        override fun mouseExited(e: MouseEvent?) = Unit
    }
}