package varvara

import uxn.test
import java.awt.Color
import java.awt.image.BufferedImage

class Screen {

    val bg = BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB)
    val fg = BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB)

    companion object {
        const val WIDTH: UByte = 0x2u
        const val HEIGHT: UByte = 0x4u

        const val X: UByte = 0x8u
        const val Y: UByte = 0xau

        const val ADDRESS: UByte = 0x2cu

        const val PIXEL: UByte = 0xeu
        const val SPRITE: UByte = 0xfu

        const val BOTTOM_RIGHT: UByte = 0x00u
        const val BOTTOM_LEFT: UByte = 0x10u
        const val UPPER_RIGHT: UByte = 0x20u
        const val UPPER_LEFT: UByte = 0x30u
    }

    // TODO: This should actually be provided by the system device
    private val colors = arrayOf(
        Color(255, 255, 255),
        Color(0, 0, 0),
        Color(119, 221, 187),
        Color(255, 102, 34)
    )

    private var x: UShort = 0x0u
    private var y: UShort = 0x0u
    private var address: UShort = 0x0u

    fun readShort(port: UByte): UShort {
        return when (port) {
            WIDTH -> bg.width.toUShort()
            HEIGHT -> bg.height.toUShort()
            X -> x
            Y -> y
            ADDRESS -> address
            else -> 0x0u
        }
    }

    fun write(port: UByte, value: UByte) {
        when (port) {
            PIXEL -> drawPixel(value)
            SPRITE -> drawSprite(value)
        }
    }

    fun writeShort(port: UByte, value: UShort) {
        when (port) {
            X -> x = value
            Y -> y = value
            ADDRESS -> address = value
        }
    }

    private fun drawPixel(params: UByte) {
        val color = colors[(params and 0x3u).toInt()]
        val flip = params and 0x30u
        val layer = if (params.test(0x40u)) fg else bg
        val fill = params.test(0x80u)

        val x = this.x.toInt()
        val y = this.y.toInt()

        if (fill) {
            val g = layer.createGraphics()
            g.color = color
            when (flip) {
                BOTTOM_RIGHT -> g.fillRect(x, y, layer.width, layer.height)
                BOTTOM_LEFT -> g.fillRect(0, y, layer.width - x, layer.height)
                UPPER_RIGHT -> g.fillRect(x, 0, layer.width, layer.height - y)
                UPPER_LEFT -> g.fillRect(0, 0, layer.width - x, layer.height - y)
            }
        } else {
            layer.setRGB(x, y, color.rgb)
        }
    }

    private fun drawSprite(params: UByte) {
        val twoBitMode = params.test(0x80u)
        val fg = params.test(0x40u)
        val flip = params and 0x30u
        val c = params and (0xfu)
    }
}