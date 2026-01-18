package uxn

interface Device {
    fun out(port: UByte, value: UByte)
    fun outShort(port: UByte, value: UShort)
    fun inp(port: UByte): UByte
    fun inpShort(port: UByte): UShort
}

class UxnMachine(val device: Device) {

    val memory = UByteArray(65536)
    val workingStack = UxnStack()
    val returnStack = UxnStack()
    private var pc: UShort = 0x100u

    enum class MachineState {
        Running, Stopped
    }

    fun loadRom(rom: UByteArray) {
        rom.forEachIndexed { idx, byte ->
            memory[0x100 + idx] = byte
        }
    }

    fun eval(pc: UShort): Boolean {
        this.pc = pc
        return eval()
    }

    fun eval(): Boolean {
        while (true) if (step() == MachineState.Stopped) break
        return true
    }

    fun step(): MachineState {
        val op = memory[pc++]
//        println("Op: ${op.str()}")
        when (op) {
            BRK -> {
                return MachineState.Stopped
            }

            JCI -> {
                pcAdd(2u)
                if (workingStack.pop() != UByte_0) {
                    pcAdd(UShort(memory[pc - 1u], memory[pc - 2u]))
                }
            }

            JMI -> {
                pcAdd(2u)
                pcAdd(UShort(memory[pc - 1u], memory[pc - 2u]))
            }

            JSI -> {
                pcAdd(2u)
                returnStack.pushShort(pc)
                pcAdd(UShort(memory[pc - 1u], memory[pc - 2u]))
            }

            else -> {
                val stack = if (op.returnFlag) returnStack else workingStack
                val keep = op.keepFlag
                val short = op.shortFlag

                when (op.base) {

                    DEO -> {
                        stack.beginPop()
                        val port = stack.pop()
                        if (short) {
                            val output = stack.popShort()
                            device.outShort(port, output)
                        } else {
                            val output = stack.pop()
                            device.out(port, output)
                        }
                        stack.endPop(keep)
                    }

                    DEI -> {
                        stack.beginPop()
                        val port = stack.pop()
                        stack.endPop(keep)
                        if (short) {
                            stack.pushShort(device.inpShort(port))
                        } else {
                            stack.push(device.inp(port))
                        }
                    }

                    LIT -> {
                        stack.push(memory[pc++])
                        if (short) {
                            stack.push(memory[pc++])
                        }
                    }

                    JMP -> {
                        stack.beginPop()
                        if (short) {
                            pc = stack.popShort()
                        } else {
                            pcAdd(stack.pop().toByte().toUShort())
                        }
                        stack.endPop(keep)
                    }

                    LDZ -> {
                        stack.beginPop()
                        val address = stack.pop()
                        stack.endPop(keep)
                        stack.push(memory[address])
                        if (short) {
                            stack.push(memory[address + 1u])
                        }
                    }

                    STZ -> {
                        stack.beginPop()
                        val address = stack.pop()
                        if (short) {
                            memory[address + 1u] = stack.pop()
                        }
                        memory[address] = stack.pop()
                        stack.endPop(keep)
                    }

                    LDR -> {
                        stack.beginPop()
                        val offset = stack.pop().toByte().toUShort()
                        stack.endPop(keep)
                        stack.push(memory[pc + offset])
                        if (short) {
                            stack.push(memory[pc + offset + 1u])
                        }
                    }

                    STR -> {
                        stack.beginPop()
                        val offset = stack.pop().toByte().toUShort()
                        if (short) {
                            memory[pc + offset + 1u] = stack.pop()
                        }
                        memory[pc + offset] = stack.pop()
                        stack.endPop(keep)
                    }

                    LDA -> {
                        stack.beginPop()
                        val address = stack.popShort()
                        stack.endPop(keep)
                        stack.push(memory[address])
                    }

                    STA -> {
                        stack.beginPop()
                        val address = stack.popShort()
                        if (short) {
                            memory[address + 1u] = stack.pop()
                        }
                        memory[address] = stack.pop()
                        stack.endPop(keep)
                    }

                    OVR -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(b)
                            stack.pushShort(a)
                            stack.pushShort(b)
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(b)
                            stack.push(a)
                            stack.push(b)
                        }
                    }

                    NIP -> {
                        if (short) {
                            stack.beginPop()
                            val v = stack.popShort()
                            stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(v)
                        } else {
                            stack.beginPop()
                            val v = stack.pop()
                            stack.pop()
                            stack.endPop(keep)
                            stack.push(v)
                        }
                    }

                    SWP -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(a)
                            stack.pushShort(b)
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(a)
                            stack.push(b)
                        }
                    }

                    ROT -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            val c = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(b)
                            stack.pushShort(a)
                            stack.pushShort(c)
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            val c = stack.pop()
                            stack.endPop(keep)
                            stack.push(b)
                            stack.push(a)
                            stack.push(c)
                        }
                    }

                    DUP -> {
                        if (short) {
                            stack.beginPop()
                            val v = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(v)
                            stack.pushShort(v)
                        } else {
                            stack.beginPop()
                            val v = stack.pop()
                            stack.endPop(keep)
                            stack.push(v)
                            stack.push(v)
                        }
                    }

                    JCN -> {
                        stack.beginPop()
                        val address = if (short) {
                            stack.popShort()
                        } else {
                            (pc + stack.pop().toByte().toUShort()).toUShort()
                        }
                        if (stack.pop() != UByte_0) {
                            pc = address
                        }
                        stack.endPop(keep)
                    }

                    JSR -> {
                        val p = pc
                        stack.beginPop()
                        if (short) {
                            pc = stack.popShort()
                        } else {
                            pcAdd(stack.pop().toByte().toUShort())
                        }
                        stack.endPop(keep)
                        returnStack.pushShort(p)
                    }

                    STH -> {
                        val targetStack = if (op.returnFlag) workingStack else returnStack
                        if (short) {
                            stack.beginPop()
                            val v = stack.popShort()
                            stack.endPop(keep)
                            targetStack.pushShort(v)
                        } else {
                            stack.beginPop()
                            val v = stack.pop()
                            stack.endPop(keep)
                            targetStack.push(v)
                        }
                    }

                    INC -> {
                        if (short) {
                            stack.beginPop()
                            val v = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort((v + 1u).toUShort())
                        } else {
                            stack.beginPop()
                            val v = stack.pop()
                            stack.endPop(keep)
                            stack.push((v + 1u).toUByte())
                        }
                    }

                    POP -> {
                        stack.beginPop()
                        if (short) {
                            stack.popShort()
                        } else {
                            stack.pop()
                        }
                        stack.endPop(keep)
                    }

                    EQU -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.push(if (b == a) 1u else 0u)
                        } else {
                            stack.binaryOp(keep) { a, b -> if (b == a) 1u else 0u }
                        }
                    }

                    NEQ -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.push(if (b != a) 1u else 0u)
                        } else {
                            stack.binaryOp(keep) { a, b -> if (b != a) 1u else 0u }
                        }
                    }

                    GTH -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.push(if (b > a) 1u else 0u)
                        } else {
                            stack.binaryOp(keep) { a, b -> if (b > a) 1u else 0u }
                        }
                    }

                    LTH -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.push(if (b < a) 1u else 0u)
                        } else {
                            stack.binaryOp(keep) { a, b -> if (b < a) 1u else 0u }
                        }
                    }

                    ADD -> {
                        if (short) {
                            stack.binaryOpShort(keep) { a, b -> (b + a).toUShort() }
                        } else {
                            stack.binaryOp(keep) { a, b -> (b + a).toUByte() }
                        }
                    }

                    DIV -> {
                        if (short) {
                            stack.binaryOpShort(keep) { a, b -> if (a == UShort_0) 0u else (b / a).toUShort() }
                        } else {
                            stack.binaryOp(keep) { a, b -> if (a == UByte_0) 0u else (b / a).toUByte() }
                        }
                    }

                    MUL -> {
                        if (short) {
                            stack.binaryOpShort(keep) { a, b -> (b * a).toUShort() }
                        } else {
                            stack.binaryOp(keep) { a, b -> (b * a).toUByte() }
                        }
                    }

                    SUB -> {
                        if (short) {
                            stack.binaryOpShort(keep) { a, b -> (b - a).toUShort() }
                        } else {
                            stack.binaryOp(keep) { a, b -> (b - a).toUByte() }
                        }
                    }

                    ORA -> {
                        if (short) {
                            stack.binaryOpShort(keep) { a, b -> b or a }
                        } else {
                            stack.binaryOp(keep) { a, b -> b or a }
                        }
                    }

                    AND -> {
                        if (short) {
                            stack.binaryOpShort(keep) { a, b -> b and a }
                        } else {
                            stack.binaryOp(keep) { a, b -> b and a }
                        }
                    }

                    SFT -> {
                        stack.beginPop()
                        val sh = stack.pop()
                        val l = (sh and 0xf0u).toInt() shr 4
                        val r = (sh and 0x0fu).toInt()
                        if (short) {
                            val v = stack.popShort().toUInt()
                            stack.endPop(keep)
                            stack.pushShort(((v shr r) shl l).toUShort())
                        } else {
                            val v = stack.pop().toUInt()
                            stack.endPop(keep)
                            stack.push(((v shr r) shl l).toUByte())
                        }
                    }

                    else -> error("Unsupported op: 0x${op.toString(16)}")
                }
            }
        }
        return MachineState.Running
    }

    private inline fun UxnStack.binaryOp(keep: Boolean, fn: (UByte, UByte) -> UByte) {
        beginPop()
        val a = pop()
        val b = pop()
        endPop(keep)
        push(fn(a, b))
    }

    private inline fun UxnStack.binaryOpShort(keep: Boolean, fn: (UShort, UShort) -> UShort) {
        beginPop()
        val a = popShort()
        val b = popShort()
        endPop(keep)
        pushShort(fn(a, b))
    }

    private fun pcAdd(uShort: UShort) {
        pc = (pc + uShort).toUShort()
    }
}