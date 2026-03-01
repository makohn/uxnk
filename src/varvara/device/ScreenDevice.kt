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

        private val TRANSPARENT = Color(0, 0, 0, 0)

        private val BLENDING = arrayOf(
            intArrayOf(0, 0, 0, 0, 1, 0, 1, 1, 2, 2, 0, 2, 3, 3, 3, 0),
            intArrayOf(0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3),
            intArrayOf(1, 2, 3, 1, 1, 2, 3, 1, 1, 2, 3, 1, 1, 2, 3, 1),
            intArrayOf(2, 3, 1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2, 3, 1, 2),
        )

        private val OPAQUE = arrayOf(
            false, true, true, true, true, false, true, true, true, true, false, true, true, true, true, false
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
        val flipX = params.test(0x10u)
        val flipY = params.test(0x20u)
        val layer = if (params.test(0x40u)) fg else bg
        val fill = params.test(0x80u)

        val x = this.x.toInt()
        val y = this.y.toInt()

        if (fill) {
            val xr = if (flipX) 0..<x else x..<layer.width
            val yr = if (flipY) 0..<y else y..<layer.height
            for (x in xr) for (y in yr) layer.setRGB(x, y, color.rgb)
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
        val color = (params and 0xfu).toInt()
        val count = auto.toInt() shr 4
        val autoX = auto.test(0x1u)
        val autoY = auto.test(0x2u)
        val autoAddress = auto.test(0x4u)

        var x = x
        var y = y
        repeat(count + 1) {
            var addr = address
            for (dy in 0..<8) {
                val lo = uxn.memory[addr].toInt()
                val hi = if (twoBitMode) uxn.memory[addr + 8u].toInt() else 0
                addr++

                val y = (y + (if (flipY) 7 - dy else dy).toUShort()).toUShort()
                if (y.toInt() >= bg.height) continue

                for (dx in 0..<8) {
                    val x = (x + (if (flipX) 7 - dx else dx).toUShort()).toUShort()
                    if (x.toInt() >= bg.width) continue

                    val loBit = (lo shr (7 - dx)) and 0b1
                    val hiBit = (hi shr (7 - dx)) and 0b1
                    val data = (loBit or (hiBit shl 1))

                    if (data != 0 || OPAQUE[color]) {
                        val c = BLENDING[data][color]
                        val rgb = (if (fg && c == 0) TRANSPARENT else colors[c]).rgb
                        layer.setRGB(x.toInt(), y.toInt(), rgb)
                    }
                }
            }
            if (autoY) {
                x = (x + (if (flipX) -8 else 8).toUShort()).toUShort()
            }
            if (autoX) {
                y = (y + (if (flipY) -8 else 8).toUShort()).toUShort()
            }

            if (autoAddress) {
                writeShort(ADDRESS, if (twoBitMode) (addr + 8u).toUShort() else addr)
            }
        }
        if (autoX) {
            val xx = if (flipX) this.x - 8u else this.x + 8u
            writeShort(X, xx.toUShort())
        }
        if (autoY) {
            val yy = if (flipY) this.y - 8u else this.y + 8u
            writeShort(Y, yy.toUShort())
        }
    }
}