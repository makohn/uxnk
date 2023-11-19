typealias Stack = ArrayDeque<UByte>

interface StackMode {
    fun Stack.pushImpl(element: UByte) = addLast(element)
    fun Stack.popImpl(n: Int): List<UByte>
}

class KeepMode : StackMode {
    override fun Stack.popImpl(n: Int) = takeLast(n).reversed()
}

class RemoveMode : StackMode {
    override fun Stack.popImpl(n: Int) = buildList<UByte> {
        add(removeLast())
    }
}

interface OpMode {
    fun Stack.push(element: UShort)
    fun Stack.pop(): UShort

    fun Stack.pop(n: Int): List<UShort>

    fun Stack.pushBool(fn: (UShort, UShort) -> Boolean) = push(pop(2).reduce { a, b -> if (fn(a, b)) 1u else 0u })
    fun Stack.pushUInt(fn: (UShort, UShort) -> UInt) = push(pop(2).reduce { a, b -> fn(a, b).toUShort() })
    fun Stack.pushUShort(fn: (UShort) -> UShort) = push(fn(pop()))
    fun Stack.pushUShort(fn: (UShort, UShort) -> UShort) = push(pop(2).reduce(fn))
    fun Stack.pushList(fn: (UShort) -> List<UShort>) = fn(pop()).reversed().forEach { push(it) }
    fun Stack.pushList(fn: (UShort, UShort) -> List<UShort>) {
        val (a, b) = pop(2)
        fn(a, b).reversed().forEach { push(it) }
    }

    fun Stack.pushList(fn: (UShort, UShort, UShort) -> List<UShort>) {
        val (a, b, c) = pop(3)
        fn(a, b, c).reversed().forEach { push(it) }
    }
}

class ByteMode : OpMode {
    context(StackMode) override fun Stack.push(element: UShort) = pushImpl(element.toUByte())
    context(StackMode) override fun Stack.pop() = pop(1).first().toUShort()
    context(StackMode) override fun Stack.pop(n: Int) = popImpl(n).map { it.toUShort() }
}

class ShortMode : OpMode {
    context(StackMode) override fun Stack.push(element: UShort) {
        pushImpl((element.toUInt() shr 8).toUByte())
        pushImpl(element.toUByte())
    }

    context(StackMode) override fun Stack.pop() = pop(1).first()

    context(StackMode) override fun Stack.pop(n: Int) = popImpl(n * 2).chunked(2).map { (lo, hi) ->
        ((hi.toUInt() shl 8) + lo).toUShort()
    }
}