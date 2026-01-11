class UxnMachine {

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
                        val device = stack.pop()
                        if (device == 0x18u.toUByte()) {
                            if (short) {
                                val output = stack.popShort()
                                print(output.toInt().toChar())
                            } else {
                                val output = stack.pop()
                                print(output.toInt().toChar())
                            }
                        }
                        stack.endPop(keep)
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
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(if (b == a) 1u else 0u)
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
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(if (b != a) 1u else 0u)
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
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(if (b > a) 1u else 0u)
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
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(if (b < a) 1u else 0u)
                        }
                    }

                    ADD -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort((b + a).toUShort())
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push((b + a).toUByte())
                        }
                    }

                    DIV -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(if (a == UShort_0) 0u else (b / a).toUShort())
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(if (a == UByte_0) 0u else (b / a).toUByte())
                        }
                    }

                    MUL -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort((b * a).toUShort())
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push((b * a).toUByte())
                        }
                    }

                    SUB -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort((b - a).toUShort())
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push((b - a).toUByte())
                        }
                    }

                    ORA -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(b or a)
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(b or a)
                        }
                    }

                    AND -> {
                        if (short) {
                            stack.beginPop()
                            val a = stack.popShort()
                            val b = stack.popShort()
                            stack.endPop(keep)
                            stack.pushShort(b and a)
                        } else {
                            stack.beginPop()
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.endPop(keep)
                            stack.push(b and a)
                        }
                    }

                    else -> error("Unsupported op: 0x${op.toString(16)}")
                }
            }
        }
        return MachineState.Running
    }

    private fun pcAdd(uShort: UShort) {
        pc = (pc + uShort).toUShort()
    }
}