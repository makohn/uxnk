package varvara

import uxn.UxnMachine
import uxn.test
import java.awt.Color
import java.awt.image.BufferedImage

class Screen(val varvara: Varvara) {

    val uxn: UxnMachine
        get() = varvara.machine

    val bg = BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB)
    val fg = BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB)

    companion object {
        const val WIDTH: UByte = 0x2u
        const val HEIGHT: UByte = 0x4u

        const val AUTO: UByte = 0x6u
        const val X: UByte = 0x8u
        const val Y: UByte = 0xau

        const val ADDRESS: UByte = 0xcu

        const val PIXEL: UByte = 0xeu
        const val SPRITE: UByte = 0xfu

        const val BOTTOM_RIGHT: UByte = 0x00u
        const val BOTTOM_LEFT: UByte = 0x10u
        const val UPPER_RIGHT: UByte = 0x20u
        const val UPPER_LEFT: UByte = 0x30u
    }

    private val blendingModes = arrayOf(
        intArrayOf(0, 0, 0, 0, 1, 0, 1, 1, 2, 2, 0, 2, 3, 3, 3, 0),
        intArrayOf(0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3),
        intArrayOf(1, 2, 3, 1, 1, 2, 3, 1, 1, 2, 3, 1, 1, 2, 3, 1),
        intArrayOf(2, 3, 1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2, 3, 1, 2),
    )

    // TODO: This should actually be provided by the system device
    private val colors = arrayOf(
        Color(255, 255, 255),
        Color(0, 0, 0),
        Color(119, 221, 187),
        Color(255, 102, 34)
    )

    private var auto: UByte = 0x0u
    private var x: UShort = 0x0u
    private var y: UShort = 0x0u
    private var address: UShort = 0x0u
    private var pixel: UByte = 0x0u
    private var sprite: UByte = 0x0u

    fun read(port: UByte): UByte {
        return when (port) {
            PIXEL -> pixel
            SPRITE -> sprite
            AUTO -> auto
            else -> 0x0u
        }
    }

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
            AUTO -> auto = value
            PIXEL -> {
//                pixel = value
                drawPixel(value)
            }
            SPRITE -> {
//                sprite = value
                drawSprite(value)
            }
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
        val autoX = auto.test(0x1u)
        val autoY = auto.test(0x2u)
        if (autoX) {
            this.x = (this.x + 1u).toUShort()
        }
        if (autoY) {
            this.y = (this.y + 1u).toUShort()
        }
    }

    private fun drawSprite(params: UByte) {
        val twoBitMode = params.test(0x80u)
        val fg = params.test(0x40u)
        val layer = if (fg) this.fg else this.bg
        val flipX = params.test(0x10u)
        val flipY = params.test(0x20u)
        val c = (params and 0xfu).toInt()
        val drawZero = c == 0 || c.mod(5) != 0

        var sx = x.toInt()
        var sy = y.toInt()
        var dx = 1
        var dy = 1

        if (!flipX) {
            sx += 7
            dx = -1
        }
        if (flipY) {
            sy += 7
            dy = -1
        }

        val count = auto.toInt() shr 4
        val autoX = auto.test(0x1u)
        val autoY = auto.test(0x2u)
        val autoAddress = auto.test(0x4u)

//        println("drawSprite: ${address.toString(16)}, $twoBitMode, $count, $auto, $flipX, $flipY, $sx, $sy, $dx, $dy")

        repeat(count + 1) {
            var x = sx
            var y = sy
            for (i in 0..<8) {
                val idxA = address.toInt() + i
                val idxB = address.toInt() + i + 8
                val pxA = uxn.memory[idxA].toInt()
                val pxB = uxn.memory[idxB].toInt()

                for (j in 0..<8) {
                    var px = ((pxA shr j) and 1)
                    if (twoBitMode) {
                        px = px or (((pxB shr j) and 1) shl 1)
                    }
                    px = blendingModes[px][c]
                    if (drawZero || px > 0) {
                        val color = if (!fg || px > 0) colors[px] else Color.white // TODO
                        layer.setRGB(x, y, color.rgb)
                    }
                    x += dx
                }
                x += -dx * 8
                y += dy
            }
            if (autoX) {
                sy += 8 * dy
            }
            if (autoY) {
                sx += -dx * 8
            }
            if (autoAddress) {
                address = if (twoBitMode) {
                    address + 0x10u
                } else {
                    address + 0x08u
                }.toUShort()
            }
        }
        if (autoX) {
            this.x = (this.x + (-dx * 8).toUShort()).toUShort()
        }
        if (autoY) {
            this.y = (this.y + (dy * 8).toUShort()).toUShort()
        }
    }
}