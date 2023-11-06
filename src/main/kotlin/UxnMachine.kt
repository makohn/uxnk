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

            OpCode.INC -> {
                workingStack.addLast(workingStack.removeLast().inc())
            }

            OpCode.POP -> {
                workingStack.removeLast()
            }

            OpCode.NIP -> {
                workingStack.removeAt(workingStack.size - 2)
            }

            OpCode.SWP -> {

            }

            OpCode.LIT -> {
                workingStack.addLast(memory[pc])
                pc++
            }

            OpCode.DEO -> {
                val device = workingStack.removeLast()
                print(workingStack.removeLast().toInt().toChar())
            }
            else -> println("Opcode '$opCode' is not implemented yet!")
        }
        return MachineState.Running
    }

    context(StackContext<*>)
    fun evaluate(opCode: OpCode) {
        when (opCode.base()) {
            else -> Unit
        }
    }

    interface StackContext<E> {
        fun ArrayDeque<UByte>.push(element: E)
        fun ArrayDeque<UByte>.pop(): E
    }

    interface ByteStack: StackContext<UByte> {
        override fun ArrayDeque<UByte>.push(element: UByte) = addLast(element)
        override fun ArrayDeque<UByte>.pop() = removeLast()
    }

    interface ShortStack: StackContext<UShort> {
        override fun ArrayDeque<UByte>.push(element: UShort) {
            addLast((element.toUInt() shr 8).toUByte())
            addLast(element.toUByte())
        }
        override fun ArrayDeque<UByte>.pop() = (removeLast().toUShort() + removeLast().toUInt() shl 8).toUShort()
    }
}