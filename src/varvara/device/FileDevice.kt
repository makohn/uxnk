package varvara.device

import util.*
import varvara.Device
import varvara.Varvara
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileDevice(varvara: Varvara) : Device() {

    companion object {
        const val SUCCESS: UByte = 0x2u
        const val STAT: UByte = 0x4u
        const val DELETE: UByte = 0x6u
        const val NAME: UByte = 0x8u
        const val READ: UByte = 0xcu
        const val WRITE: UByte = 0xeu
    }

    private val uxn = varvara.uxn

    private val length: Int get() = UShort(memory[0xau], memory[0xbu]).toInt()
    private val name: String get() = UShort(memory[0x8u], memory[0x9u]).let {
        String(buildList {
            var i = it
            while(uxn.memory[i] != UByte_0) add(uxn.memory[i++].toByte())
        }.toByteArray())
    }
    private val append: Boolean get() = memory[0x7u].test(0x1u)
    private var success: UShort
        get() = readShort(SUCCESS)
        set(value) = writeShort(SUCCESS, value)

    private var reader: FileInputStream? = null
    private var writer: FileOutputStream? = null

    override fun write(port: UByte, value: UByte) {
        super.write(port, value)
        when (port) {
            DELETE -> {
                val file = File(name)
                val deleted = file.delete()
                success = if (deleted) 1u else 0u
            }
        }
    }

    override fun writeShort(port: UByte, value: UShort) {
        super.writeShort(port, value)
        when (port) {
            NAME -> closeFile()
            READ -> {
                val file = File(name)
                val address = value.toInt()
                val size = length.coerceAtMost(uxn.memory.size - address)
                if (file.isFile) {
                    if (reader == null) {
                        reader = FileInputStream(file)
                    }
                    val buffer = ByteArray(size)
                    val read = reader!!.read(buffer)
                    buffer.toUByteArray().copyInto(uxn.memory, destinationOffset = address)
                    success = read.coerceAtLeast(0).toUShort()
                } else if (file.isDirectory) {
                    val files = file.listFiles()
                    if (files != null) {
                        val buffer = ByteArray(size)
                        files
                            .joinToString("\n") { "${it.stats()} ${it.name}${if (it.isDirectory) "/" else ""}" }
                            .toByteArray()
                            .copyInto(buffer)
                            .toUByteArray()
                            .copyInto(uxn.memory, destinationOffset = address)
                        success = size.toUShort()
                    }
                }
            }
            WRITE -> {
                val file = File(name)
                if (!file.exists()) {
                    file.createNewFile()
                }
                if (writer == null) {
                    writer = FileOutputStream(file, append)
                }
                val address = value.toInt()
                val size = length.coerceAtMost(uxn.memory.size - address)
                val buffer = ByteArray(size) { i -> uxn.memory[value + i.toUShort()].toByte() }
                writer!!.write(buffer)
                success = size.toUShort()
            }
            STAT -> {
                val file = File(name)
                val address = value.toInt()
                val size = length.coerceAtMost(uxn.memory.size - address)
                val stats = file.stats(size).map { it.code.toUByte() }.toUByteArray()
                stats.copyInto(uxn.memory, destinationOffset = address)
                success = size.toUShort()
            }
        }
    }

    private fun File.stats(n: Int = 4): String {
        val size = length()
        return when {
            !exists() -> "!".repeat(n)
            isDirectory -> "-".repeat(n)
            size > 0xffff -> "?".repeat(n)
            else -> size.toHexString(HexFormat {
                upperCase = false
                number {
                    minLength = n
                    removeLeadingZeros = true
                }
            })
        }
    }

    private fun closeFile() {
        reader?.close()
        reader = null
        writer?.close()
        writer = null
    }
}