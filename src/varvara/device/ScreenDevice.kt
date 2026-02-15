package varvara.device

import util.*
import varvara.Device
import varvara.Varvara
import java.awt.Color
import java.awt.image.BufferedImage

class ScreenDevice(varvara: Varvara) : Device() {

    companion object {
        const val DEFAULT_WIDTH: Int = 64 * 8
        const val DEFAULT_HEIGHT: Int = 40 * 8

        const val WIDTH: UByte = 0x2u
        const val HEIGHT: UByte = 0x4u
        const val X: UByte = 0x8u
        const val Y: UByte = 0xau
        const val ADDRESS: UByte = 0xcu
        const val PIXEL: UByte = 0xeu
        const val SPRITE: UByte = 0xfu

        const val BOTTOM_RIGHT: UByte = 0x00u
        const val BOTTOM_LEFT: UByte = 0x10u
        const val UPPER_RIGHT: UByte = 0x20u
        const val UPPER_LEFT: UByte = 0x30u

        private val TRANSPARENT = Color(0, 0, 0, 0)

        private val blendingModes = arrayOf(
            intArrayOf(0, 0, 0, 0, 1, 0, 1, 1, 2, 2, 0, 2, 3, 3, 3, 0),
            intArrayOf(0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3),
            intArrayOf(1, 2, 3, 1, 1, 2, 3, 1, 1, 2, 3, 1, 1, 2, 3, 1),
            intArrayOf(2, 3, 1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2, 3, 1, 2),
        )
    }

    var bg = BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB); private set
    var fg = BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB); private set

    private val uxn = varvara.uxn

    val colors: Array<Color> by lazy {
        val system = varvara.system
        val r = system.red.toInt()
        val g = system.green.toInt()
        val b = system.blue.toInt()
        arrayOf(
            Color((r and 0xf000) shr 8, (g and 0xf000) shr 8, (b and 0xf000) shr 8),
            Color((r and 0x0f00) shr 4, (g and 0x0f00) shr 4, (b and 0x0f00) shr 4),
            Color((r and 0x00f0), (g and 0x00f0), (b and 0x00f0)),
            Color((r and 0x000f) shl 4, (g and 0x000f) shl 4, (b and 0x000f) shl 4),
        )
    }

    val vector: UShort get() = UShort(memory[0x0], memory[0x1])
    private val x: UShort get() = UShort(memory[0x8], memory[0x9])
    private val y: UShort get() = UShort(memory[0xa], memory[0xb])
    private val address: UShort get() = UShort(memory[0xc], memory[0xd])
    private val auto: UByte get() = memory[0x6]

    override fun readShort(port: UByte): UShort {
        return when (port) {
            WIDTH -> bg.width.toUShort()
            HEIGHT -> bg.height.toUShort()
            else -> super.readShort(port)
        }
    }

    override fun write(port: UByte, value: UByte) {
        super.write(port, value)
        when (port) {
            PIXEL -> drawPixel(value)
            SPRITE -> drawSprite(value)
        }
    }

    override fun writeShort(port: UByte, value: UShort) {
        super.writeShort(port, value)
        when (port) {
            WIDTH -> {
                bg = BufferedImage(value.toInt(), bg.height, bg.type)
                fg = BufferedImage(value.toInt(), fg.height, fg.type)
            }
            HEIGHT -> {
                bg = BufferedImage(bg.width, value.toInt(), bg.type)
                fg = BufferedImage(fg.width, value.toInt(), fg.type)
            }
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
            if (x < bg.width && y < bg.height) {
                layer.setRGB(x, y, color.rgb)
            }
        }
        val autoX = auto.test(0x1u)
        val autoY = auto.test(0x2u)
        if (autoX) {
            writeShort(X, (this.x + 1u).toUShort())
        }
        if (autoY) {
            writeShort(Y, (this.y + 1u).toUShort())
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
                        val color = if (!fg || px > 0) colors[px] else TRANSPARENT
                        if (x < bg.width && y < bg.height) {
                            layer.setRGB(x, y, color.rgb)
                        }
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
                writeShort(ADDRESS, if (twoBitMode) {
                    address + 0x10u
                } else {
                    address + 0x08u
                }.toUShort())
            }
        }
        if (autoX) {
            writeShort(X, (this.x + (-dx * 8).toUShort()).toUShort())
        }
        if (autoY) {
            writeShort(Y, (this.y + (dy * 8).toUShort()).toUShort())
        }
    }
}