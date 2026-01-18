package varvara

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.concurrent.thread

data class Pixel(val x: Int, val y: Int, val color: Color)

class ScreenPanel(width: Int, height: Int) : JPanel() {

    val buffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    lateinit var pixels: List<Pixel>

    init {
        preferredSize = Dimension(width, height)
    }

    fun updateModel(pixels: List<Pixel>) {
        this.pixels = pixels
        redrawBuffer()
        repaint()
    }

    private fun redrawBuffer() {
        val g = buffer.createGraphics()
        g.composite = AlphaComposite.Clear
        g.fillRect(0, 0, buffer.width, buffer.height)
        g.dispose()

        for (p in pixels) {
            if (p.x >= 0 && p.y >= 0) {
                buffer.setRGB(p.x, p.y, p.color.rgb)
            }
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        g!!.drawImage(buffer, 0, 0, null)
    }
}

fun main() {
    val frame = JFrame("UXN")
    val panel = ScreenPanel(400, 400)

    thread {
        val timer = Timer(1000 / 60) { e ->
            val pixels = ArrayList<Pixel>()
            for (x in 0..100) for (y in 0..100) {
                pixels.add(Pixel(x, y, Color.RED))
            }
            panel.updateModel(pixels)
        }
        timer.start()
    }

    frame.add(panel)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}