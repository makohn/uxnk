data class UxnMachine(
    private val memory: UByteArray = UByteArray(65536),
    private val workingStack: ArrayDeque<UByte> = ArrayDeque(256),
    private val returnStack: ArrayDeque<UByte> = ArrayDeque(256),
    private var pc: Int = 0x100
) {

    enum class MachineState {
        Running, Stopped
    }

    fun loadRom(rom: UByteArray) {
        rom.forEachIndexed { idx, byte ->
            memory[0x100 + idx] = byte
        }
    }

    fun step(): MachineState {
        val opCode = OpCode[memory[pc++]]
        when (opCode) {
            OpCode.BRK -> {
                return MachineState.Stopped
            }

            OpCode.LIT -> {
                workingStack.addLast(memory[pc])
                pc++
            }

            OpCode.DEO -> {
                val device = workingStack.removeLast()
                print(workingStack.removeLast().toInt().toChar())
            }
            else -> with(if (opCode.short()) ShortMode() else ByteMode()) {
                evaluate(opCode, workingStack)
            }
        }
        return MachineState.Running
    }

    context(OpMode)
    private fun evaluate(opCode: OpCode, stack: ArrayDeque<UByte>) {
        when (opCode.base()) {
            OpCode.INC -> stack.push(stack.pop().inc())
            OpCode.POP -> stack.pop()
            OpCode.NIP -> {
                val v = stack.pop()
                stack.pop()
                stack.push(v)
            }
            OpCode.SWP -> {
                val (b, a) = stack.pop(2)
                stack.push(b)
                stack.push(a)
            }
            OpCode.ROT -> {
                val (c, b, a) = stack.pop(3)
                stack.push(b)
                stack.push(c)
                stack.push(a)
            }
            OpCode.DUP -> {
                val v = stack.pop()
                stack.push(v)
                stack.push(v)
            }
            OpCode.OVR -> {
                val (b, a) = stack.pop(2)
                stack.push(a)
                stack.push(b)
                stack.push(a)
            }
            OpCode.EQU -> stack.pushBool(stack.pop() == stack.pop())
            OpCode.NEQ -> stack.pushBool(stack.pop() != stack.pop())
            OpCode.GTH -> stack.pushBool(stack.pop() > stack.pop())
            OpCode.LTH -> stack.pushBool(stack.pop() < stack.pop())
            OpCode.ADD -> stack.push(stack.pop() + stack.pop())
            OpCode.SUB -> {
                val (b, a) = stack.pop(2)
                stack.push(a - b)
            }
            OpCode.MUL -> stack.push(stack.pop() * stack.pop())
            OpCode.DIV -> {
                val (b, a) = stack.pop(2)
                require(b != 0.toUShort()) { "error: division by zero!" }
                stack.push(a / b)
            }
            OpCode.AND -> stack.push(stack.pop() and stack.pop())
            OpCode.ORA -> stack.push(stack.pop() or stack.pop())
            OpCode.EOR -> stack.push(stack.pop() xor stack.pop())
            else -> error("error: $opCode not implemented!")
        }
    }

    // As Kotlin does not have union types (and UByte and UShort do not share a common interface for math operations),
    // we have to coerce into UShort as an upper boundary to allow for executing math operations regardless of the
    // concrete context used.
    //
    // Otherwise, we could have introduced a generic type parameter T where T is either UShort or UByte (union type)
    interface OpMode {
        fun ArrayDeque<UByte>.push(element: UShort)
        fun ArrayDeque<UByte>.pop(): UShort

        fun ArrayDeque<UByte>.pop(n: Int) = buildList {
            repeat(n) {
                add(pop())
            }
        }

        fun ArrayDeque<UByte>.pushBool(bool : Boolean) = push(if (bool) 1u else 0u)

        fun ArrayDeque<UByte>.push(int: UInt) = push(int.toUShort())
    }

    class ByteMode: OpMode {
        override fun ArrayDeque<UByte>.push(element: UShort) = addLast(element.toUByte())
        override fun ArrayDeque<UByte>.pop() = removeLast().toUShort()
    }

    class ShortMode: OpMode {
        override fun ArrayDeque<UByte>.push(element: UShort) {
            addLast((element.toUInt() shr 8).toUByte())
            addLast(element.toUByte())
        }
        override fun ArrayDeque<UByte>.pop() = (removeLast().toUShort() + removeLast().toUInt() shl 8).toUShort()
    }
}