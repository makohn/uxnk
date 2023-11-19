data class UxnMachine(
    private val memory: UByteArray = UByteArray(65536),
    private val workingStack: ArrayDeque<UByte> = Stack(256),
    private val returnStack: ArrayDeque<UByte> = Stack(256),
    private var pc: Int = 0x100
) {

    private val shortMode = ShortMode()
    private val byteMode = ByteMode()
    private val keepMode = KeepMode()
    private val removeMode = RemoveMode()

    enum class MachineState {
        Running, Stopped
    }

    fun loadRom(rom: UByteArray) {
        rom.forEachIndexed { idx, byte ->
            memory[0x100 + idx] = byte
        }
    }

    fun step(): MachineState {
        val instruction = memory[pc++]
        val opMode = if (instruction.hasShortFlag()) shortMode else byteMode
        val stackMode = if (instruction.hasKeepFlag()) keepMode else removeMode
        val stack = if (instruction.hasReturnFlag()) returnStack else workingStack
        with(stackMode) {
            with(opMode) {
                when (instruction) {
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

                    else -> evaluate(instruction, stack)
                }
            }
        }
        return MachineState.Running
    }

    context(StackMode, OpMode)
    private fun evaluate(instruction: UByte, stack: ArrayDeque<UByte>) {
        when (instruction.opCode()) {
            OpCode.INC -> stack.pushUShort { a -> a.inc() }
            OpCode.POP -> stack.pop()
            OpCode.NIP -> stack.pushList { a, _ -> listOf(a) }
            OpCode.SWP -> stack.pushList { a, b -> listOf(b, a) }
            OpCode.ROT -> stack.pushList { a, b, c -> listOf(b, c, a) }
            OpCode.DUP -> stack.pushList { a -> listOf(a, a) }
            OpCode.OVR -> stack.pushList { a, b -> listOf(a, b, a) }
            OpCode.EQU -> stack.pushBool { a, b -> a == b }
            OpCode.NEQ -> stack.pushBool { a, b -> a != b }
            OpCode.GTH -> stack.pushBool { a, b -> a > b }
            OpCode.LTH -> stack.pushBool { a, b -> a < b }
            OpCode.ADD -> stack.pushUInt { a, b -> a + b }
            OpCode.SUB -> stack.pushUInt { a, b -> b - a }
            OpCode.MUL -> stack.pushUInt { a, b -> a * b }
            OpCode.DIV -> stack.pushUInt { a, b -> b / a }
            OpCode.AND -> stack.pushUShort { a, b -> a and b }
            OpCode.ORA -> stack.pushUShort { a, b -> a or b }
            OpCode.EOR -> stack.pushUShort { a, b -> a xor b }
            else -> error("error: $instruction not implemented!")
        }
    }
}