typealias Stack = ArrayDeque<UByte>

fun Stack.popAndKeep(n: Int) = takeLast(n).reversed()
fun Stack.popAndRemove(n: Int) = buildList<UByte> {
    add(removeLast())
}

fun Stack.pushShort(element: UShort) {
    addLast((element.toUInt() shr 8).toUByte())
    addLast(element.toUByte())
}

fun Stack.pushByte(element: UShort) = addLast(element.toUByte())
fun Stack.popShort(n: Int, keep: Boolean = false) =
    (if (keep) popAndKeep(n * 2) else popAndRemove(n * 2))
        .chunked(2).map { (lo, hi) -> toUShort(lo, hi) }

fun Stack.popByte(n: Int, keep: Boolean = false) =
    (if (keep) popAndKeep(n) else popAndRemove(n)).map { it.toUShort() }

fun Stack.popShort() = popShort(1).first()
fun Stack.popByte() = popByte(1).first().toUShort()

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

@JvmInline
value class ByteMode(private val keep: Boolean) : OpMode {
    override fun Stack.push(element: UShort) = pushByte(element)
    override fun Stack.pop() = popByte()
    override fun Stack.pop(n: Int) = popByte(n, keep)
}

@JvmInline
value class ShortMode(private val keep: Boolean) : OpMode {
    override fun Stack.push(element: UShort) = pushShort(element)
    override fun Stack.pop() = popShort()
    override fun Stack.pop(n: Int) = popShort(n, keep)
}