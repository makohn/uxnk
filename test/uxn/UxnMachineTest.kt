package uxn

import Console
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class UxnMachineTest {

    // @formatter:off
    private val tests = listOf(
        // JCI
        JCI.wst(0x0u).memory(0x101u, 0x2u, 0x2u) expect pc(0x103u),
        JCI.wst(0x1u).memory(0x101u, 0x7u, 0x5u) expect pc(0x808u),
        // JMI
        JMI.memory(0x101u, 0x7u, 0x5u) expect pc(0x808u),
        // JSI
        JSI.memory(0x101u, 0x7u, 0x5u) expect pc(0x808u).rst(0x1u, 0x3u),
        // LIT
        LIT.memory(0x101u, 0x1u) expect wst(0x1u).pc(0x102u),
        LIT.s.memory(0x101u, 0x1u, 0x2u) expect wst(0x1u, 0x2u).pc(0x103u),
        // INC
        INC.wst(0x1u) expect wst(0x2u),
        INC.s.wst(0x00u, 0x01u) expect wst(0x00u, 0x02u),
        INC.s.k.wst(0x00u, 0x01u) expect wst(0x00u, 0x01u, 0x00u, 0x02u),
        // POP
        POP.wst(0x12u, 0x34u) expect wst(0x12u),
        POP.s.wst(0x12u, 0x34u) expect wst(),
        POP.s.k.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u),
        // NIP
        NIP.wst(0x12u, 0x34u) expect wst(0x34u),
        NIP.s.wst(0x12u, 0x34u, 0x56u, 0x78u) expect wst(0x56u, 0x78u),
        NIP.s.k.wst(0x12u, 0x34u, 0x56u, 0x78u) expect wst(0x12u, 0x34u, 0x56u, 0x78u, 0x56u, 0x78u),
        // SWP
        SWP.wst(0x12u, 0x34u) expect wst(0x34u, 0x12u),
        SWP.k.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x34u, 0x12u),
        SWP.s.wst(0x12u, 0x34u, 0x56u, 0x78u) expect wst(0x56u, 0x78u, 0x12u, 0x34u),
        SWP.s.k.wst(0x12u, 0x34u, 0x56u, 0x78u) expect wst(0x12u, 0x34u, 0x56u, 0x78u, 0x56u, 0x78u, 0x12u, 0x34u),
        // ROT
        ROT.wst(0x12u, 0x34u, 0x56u) expect wst(0x34u, 0x56u, 0x12u),
        ROT.k.wst(0x12u, 0x34u, 0x56u) expect wst(0x12u, 0x34u, 0x56u, 0x34u, 0x56u, 0x12u),
        ROT.s.wst(0x12u, 0x34u, 0x56u, 0x78u, 0x9au, 0xbcu) expect wst(0x56u, 0x78u, 0x9au, 0xbcu, 0x12u, 0x34u),
        ROT.s.k.wst(0x12u, 0x34u, 0x56u, 0x78u, 0x9au, 0xbcu) expect wst(0x12u, 0x34u, 0x56u, 0x78u, 0x9au, 0xbcu, 0x56u, 0x78u, 0x9au, 0xbcu, 0x12u, 0x34u),
        // DUP
        DUP.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x34u),
        DUP.k.wst(0x12u) expect wst(0x12u, 0x12u),
        DUP.s.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x12u, 0x34u),
        // OVR
        OVR.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x12u),
        OVR.k.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x12u, 0x34u, 0x12u),
        OVR.s.wst(0x12u, 0x34u, 0x56u, 0x78u) expect wst(0x12u, 0x34u, 0x56u, 0x78u, 0x12u, 0x34u),
        OVR.s.k.wst(0x12u, 0x34u, 0x56u, 0x78u) expect wst(0x12u, 0x34u, 0x56u, 0x78u, 0x12u, 0x34u, 0x56u, 0x78u, 0x12u, 0x34u),
        // EQU
        EQU.wst(0x12u, 0x12u) expect wst(0x01u),
        EQU.k.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x00u),
        EQU.s.wst(0xabu, 0xcdu, 0xefu, 0x01u) expect wst(0x00u),
        EQU.s.k.wst(0xabu, 0xcdu, 0xabu, 0xcdu) expect wst(0xabu, 0xcdu, 0xabu, 0xcdu, 0x01u),
        // NEQ
        NEQ.wst(0x12u, 0x12u) expect wst(0x00u),
        NEQ.k.wst(0x12u, 0x34u) expect wst(0x12u, 0x34u, 0x01u),
        NEQ.s.wst(0xabu, 0xcdu, 0xefu, 0x01u) expect wst(0x01u),
        NEQ.s.k.wst(0xabu, 0xcdu, 0xabu, 0xcdu) expect wst(0xabu, 0xcdu, 0xabu, 0xcdu, 0x00u),
        // GTH
        GTH.wst(0x12u, 0x34u) expect wst(0x00u),
        GTH.k.wst(0x34u, 0x12u) expect wst(0x34u, 0x12u, 0x01u),
        GTH.s.wst(0x34u, 0x56u, 0x12u, 0x34u) expect wst(0x01u),
        GTH.s.k.wst(0x12u, 0x34u, 0x34u, 0x56u) expect wst(0x12u, 0x34u, 0x34u, 0x56u, 0x00u),
        // LTH
        LTH.wst(0x01u, 0x01u) expect wst(0x00u),
        LTH.k.wst(0x01u, 0x00u) expect wst(0x01u, 0x00u, 0x00u),
        LTH.s.wst(0x00u, 0x01u, 0x00u, 0x00u) expect wst(0x00u),
        LTH.s.k.wst(0x00u, 0x01u, 0x00u, 0x00u) expect wst(0x00u, 0x01u, 0x00u, 0x00u, 0x00u),
        // JMP
        JMP.wst(0x1u) expect pc(0x102u),
        JMP.k.wst(0x1u) expect wst(0x1u).pc(0x102u),
        JMP.s.wst(0x3u, 0x4u) expect pc(0x304u),
        JMP.s.k.wst(0x3u, 0x4u) expect wst(0x3u, 0x4u).pc(0x304u),
        // JCN
        JCN.wst(0x1u, 0x4u) expect pc(0x105u),
        JCN.k.wst(0x1u, 0x4u) expect wst(0x1u, 0x4u).pc(0x105u),
        JCN.s.wst(0x1u, 0x2u, 0x7u) expect pc(0x207u),
        JCN.s.k.wst(0x1u, 0x2u, 0x7u) expect wst(0x1u, 0x2u, 0x7u).pc(0x207u),
        // JSR
        JSR.wst(0x4u) expect pc(0x105u).rst(0x1u, 0x1u),
        JSR.k.wst(0x4u) expect wst(0x4u).pc(0x105u).rst(0x1u, 0x1u),
        JSR.s.wst(0x2u, 0x7u) expect pc(0x207u).rst(0x1u, 0x1u),
        JSR.s.k.wst(0x2u, 0x7u) expect wst(0x2u, 0x7u).pc(0x207u).rst(0x1u, 0x1u),
        // STH
        STH.wst(0x7u) expect rst(0x7u),
        STH.r.rst(0x7u) expect wst(0x7u),
        STH.k.wst(0x7u) expect wst(0x7u).rst(0x7u),
        STH.s.wst(0x7u, 0x8u) expect rst(0x7u, 0x8u),
        STH.s.r.rst(0x7u, 0x8u) expect wst(0x7u, 0x8u),
        STH.s.k.wst(0x7u, 0x8u) expect wst(0x7u, 0x8u).rst(0x7u, 0x8u),
        // LDZ
        LDZ.memory(0x71u, 0x42u).wst(0x71u) expect wst(0x42u),
        LDZ.s.memory(0x71u, 0x42u, 0x69u).wst(0x71u) expect wst(0x42u, 0x69u),
        // STZ
        STZ.wst(0x42u, 0x71u) expect memory(0x71u, 0x42u),
        STZ.s.wst(0x42u, 0x69u, 0x71u) expect memory(0x71u, 0x42u, 0x69u),
        // LDR
        LDR.memory(0xf1u, 0x42u).wst((-16).toUByte()) expect wst(0x42u),
        LDR.s.memory(0xf1u, 0x42u, 0x69u).wst((-16).toUByte()) expect wst(0x42u, 0x69u),
        // STR
        STR.wst(0x42u, (-16).toUByte()) expect memory(0xf1u, 0x42u),
        STR.s.wst(0x42u, 0x69u, (-16).toUByte()) expect memory(0xf1u, 0x42u, 0x69u),
        // LDA
        LDA.wst(0x1u, 0x9u).memory(0x109u, 0x42u) expect wst(0x42u),
        LDA.s.wst(0x1u, 0x9u).memory(0x109u, 0x42u, 0x69u) expect wst(0x42u, 0x69u),
        // STA
        STA.wst(0x42u, 0x1u, 0x9u) expect memory(0x109u, 0x42u),
        STA.s.wst(0x42u, 0x69u, 0x1u, 0x9u) expect memory(0x109u, 0x42u, 0x69u),
        // ADD
        ADD.wst(0x1au, 0x2eu) expect wst(0x48u),
        ADD.k.wst(0x02u, 0x5du) expect wst(0x02u, 0x5du, 0x5fu),
        ADD.s.wst(0x00u, 0x001u, 0x00u, 0x02u) expect wst(0x00u, 0x03u),
        // SUB
        SUB.wst(0x08u, 0x03u) expect wst(0x05u),
        SUB.k.wst(0x08u, 0x02u) expect wst(0x08u, 0x02u, 0x06u),
        SUB.s.wst(0x20u, 0x00u, 0x10u, 0x00u) expect wst(0x10u, 0x00u),
        // MUL
        MUL.wst(0x06u, 0x02u) expect wst(0xcu),
        MUL.k.wst(0x08u, 0x02u) expect wst(0x08u, 0x02u, 0x10u),
        MUL.s.wst(0x08u, 0x00u, 0x00u, 0x02u) expect wst(0x10u, 0x00u),
        // DIV
        DIV.wst(0x10u, 0x2u) expect wst(0x8u),
        DIV.k.wst(0x10u, 0x3u) expect wst(0x10u, 0x3u, 0x5u),
        DIV.s.wst(0x0010u, 0x0000u) expect wst(0x00u, 0x00u),
        // AND
        AND.wst(0xfcu, 0x3fu) expect wst(0x3cu),
        // ORA
        ORA.wst(0xfcu, 0x3fu) expect wst(0xffu),
        // EOR
        EOR.wst(0xfcu, 0x3fu) expect wst(0xc3u),
        // SFT
        SFT.wst(0x34u, 0x10u) expect wst(0x68u),
        SFT.wst(0x34u, 0x01u) expect wst(0x1au),
        SFT.k.wst(0x34u, 0x33u) expect wst(0x34u, 0x33u, 0x30u),
        SFT.k.s.wst(0x12u, 0x48u, 0x34u) expect wst(0x12u, 0x48u, 0x34u, 0x09u, 0x20u),
    )
    // @formatter:on

    @TestFactory
    fun testOps() = tests.map { (state, expectation) ->
        DynamicTest.dynamicTest(state.opCode.str()) {

            expectation.wst?.reversed()?.forEach { expected ->
                val actual = state.machine.workingStack.pop()
                assertEquals(
                    expected, actual, """
                    expected: <${expected.toString(16)}> but was: <${actual.toString(16)}>
                """.trimIndent()
                )
            }
            expectation.rst?.reversed()?.forEach { expected ->
                val actual = state.machine.returnStack.pop()
                assertEquals(
                    expected, actual, """
                    expected: <${expected.toString(16)}> but was: <${actual.toString(16)}>
                """.trimIndent()
                )
            }
            expectation.memory?.forEachIndexed { idx, expected ->
                val actual = state.machine.memory[expectation.address.toInt() + idx]
                assertEquals(
                    expected, actual, """
                    expected: <${expected.toString(16)}> but was: <${actual.toString(16)}>

                """.trimIndent()
                )
            }
            expectation.pc?.let { pc ->
                assertEquals(pc, state.machine.pc, """
                    expected: <${pc.toString(16)}> but was: <${state.machine.pc.toString(16)}>
                """.trimIndent())
            }
        }
    }

    private val UByte.k: UByte get() = (this + 0x80u).toUByte()
    private val UByte.r: UByte get() = (this + 0x40u).toUByte()
    private val UByte.s: UByte get() = (this + 0x20u).toUByte()

    private class State(val opCode: OpCode, wst: UByteArray?, rst: UByteArray?, memory: UByteArray?, address: UShort) {
        val machine = UxnMachine(Console())

        init {
            val address = address.toInt()
            memory?.forEachIndexed { index, byte -> machine.memory[address + index] = byte }
            wst?.forEach { machine.workingStack.push(it) }
            rst?.forEach { machine.returnStack.push(it) }
            machine.loadRom(UByteArray(1) { opCode })
            machine.step()
        }
    }

    private class Expectation(
        val wst: UByteArray?,
        val rst: UByteArray?,
        val memory: UByteArray?,
        val address: UShort,
        val pc: UShort?
    )

    private class Builder(val opCode: OpCode? = null) {
        var wst: UByteArray? = null
        var rst: UByteArray? = null
        var pc: UShort? = null
        var address: UShort? = null
        var memory: UByteArray? = null

        fun wst(vararg wst: UByte): Builder {
            this.wst = wst
            return this
        }

        fun rst(vararg rst: UByte): Builder {
            this.rst = rst
            return this
        }

        fun pc(pc: UShort): Builder {
            this.pc = pc
            return this
        }

        fun memory(address: UShort, vararg memory: UByte): Builder {
            this.address = address
            this.memory = memory
            return this
        }

        fun toState() = State(opCode!!, wst, rst, memory, address ?: 0u)
        fun toExpectation() = Expectation(wst, rst, memory, address ?: 0u, pc)
    }

    private fun UByte.rst(vararg rst: UByte) = Builder(this).rst(*rst)
    private fun UByte.wst(vararg wst: UByte) = Builder(this).wst(*wst)
    private fun UByte.memory(address: UShort, vararg memory: UByte) = Builder(this).memory(address, *memory)

    private fun wst(vararg wst: UByte) = Builder().wst(*wst)
    private fun rst(vararg rst: UByte) = Builder().rst(*rst)
    private fun pc(pc: UShort) = Builder().pc(pc)
    private fun memory(address: UShort, vararg memory: UByte) = Builder().memory(address, *memory)

    private data class Test(val state: State, val expectation: Expectation)

    private infix fun Builder.expect(other: Builder): Test {
        return Test(this.toState(), other.toExpectation())
    }
}