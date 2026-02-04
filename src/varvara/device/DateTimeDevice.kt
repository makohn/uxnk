package varvara.device

import util.*
import varvara.Device
import java.time.LocalDateTime
import java.util.*

class DateTimeDevice : Device() {

    companion object {
        const val YEAR: UByte = 0x0u
        const val MONTH: UByte = 0x2u
        const val DAY: UByte = 0x3u
        const val HOUR: UByte = 0x4u
        const val MINUTE: UByte = 0x5u
        const val SECOND: UByte = 0x6u
        const val DOTW: UByte = 0x7u
        const val DOTY: UByte = 0x8u
        const val ISDST: UByte = 0xau
    }

    override fun write(port: UByte, value: UByte) {
        // no-op
    }

    override fun writeShort(port: UByte, value: UShort) {
        // no-op
    }

    override fun read(port: UByte): UByte {
        val date = LocalDateTime.now()
        return when (port) {
            MONTH -> (date.monthValue - 1).toUByte()
            DAY -> date.dayOfMonth.toUByte()
            HOUR -> date.hour.toUByte()
            MINUTE -> date.minute.toUByte()
            SECOND -> date.second.toUByte()
            DOTW -> date.dayOfWeek.value.toUByte()
            ISDST -> if (TimeZone.getDefault().useDaylightTime()) 0x1u else 0x0u
            else -> error("read port=${port.toString(16)}")
        }
    }

    override fun readShort(port: UByte): UShort {
        val date = LocalDateTime.now()
        return when (port) {
            YEAR -> date.year.toUShort()
            DOTY -> (date.dayOfYear - 1).toUShort()
            else -> {
                val hi = read(port)
                val lo = read((port + 1u).toUByte())
                UShort(hi, lo)
            }
        }
    }
}