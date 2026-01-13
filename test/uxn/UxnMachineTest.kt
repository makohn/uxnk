package uxn

import Console
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class UxnMachineTest {

    private val UByte.k: UByte get() = (this + 0x80_u).toUByte()
    private val UByte.r: UByte get() = (this + 0x40_u).toUByte()
    private val UByte.s: UByte get() = (this + 0x20_u).toUByte()

    private class State(
        val opCode: OpCode,
        val wst: UByteArray?,
    )

    private class Expectation(
        val wst: UByteArray?
    )

    private class Builder(val opCode: OpCode? = null) {
        var wst: UByteArray? = null

        fun wst(wst: UByteArray): Builder {
            this.wst = wst
            return this
        }

        fun toState() = State(opCode!!, wst)
        fun toExpectation() = Expectation(wst)
    }

    private fun UByte.wst(vararg wst: UByte) = Builder(this).wst(wst)

    private fun wst(vararg wst: UByte) = Builder().wst(wst)

    private data class Test(val state: State, val expectation: Expectation)

    private infix fun Builder.leadsTo(other: Builder): Test {
        return Test(this.toState(), other.toExpectation())
    }

    private val tests = listOf(
        // INC
        INC.wst(0x1_u) leadsTo wst(0x2_u),
        INC.s.wst(0x00_u, 0x01_u) leadsTo wst(0x00_u, 0x02_u),
        INC.s.k.wst(0x00_u, 0x01_u) leadsTo wst(0x00_u, 0x01_u, 0x00_u, 0x02_u),
        // POP
        POP.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u),
        POP.s.wst(0x12_u, 0x34_u) leadsTo wst(), // TODO: Emptiness check
        POP.s.k.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u),
        // NIP
        NIP.wst(0x12_u, 0x34_u) leadsTo wst(0x34_u),
        NIP.s.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u) leadsTo wst(0x56_u, 0x78_u),
        NIP.s.k.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u) leadsTo wst(0x12_u, 0x34_u, 0x56_u, 0x78_u, 0x56_u, 0x78_u),
        // SWP
        SWP.wst(0x12_u, 0x34_u) leadsTo wst(0x34_u, 0x12_u),
        SWP.k.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x34_u, 0x12_u),
        SWP.s.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u) leadsTo wst(0x56_u, 0x78_u, 0x12_u, 0x34_u),
        SWP.s.k.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u) leadsTo wst(
            0x12_u,
            0x34_u,
            0x56_u,
            0x78_u,
            0x56_u,
            0x78_u,
            0x12_u,
            0x34_u
        ),
        // ROT
        ROT.wst(0x12_u, 0x34_u, 0x56_u) leadsTo wst(0x34_u, 0x56_u, 0x12_u),
        ROT.k.wst(0x12_u, 0x34_u, 0x56_u) leadsTo wst(0x12_u, 0x34_u, 0x56_u, 0x34_u, 0x56_u, 0x12_u),
        ROT.s.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u, 0x9a_u, 0xbc_u) leadsTo wst(
            0x56_u,
            0x78_u,
            0x9a_u,
            0xbc_u,
            0x12_u,
            0x34_u
        ),
        ROT.s.k.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u, 0x9a_u, 0xbc_u) leadsTo wst(
            0x12_u,
            0x34_u,
            0x56_u,
            0x78_u,
            0x9a_u,
            0xbc_u,
            0x56_u,
            0x78_u,
            0x9a_u,
            0xbc_u,
            0x12_u,
            0x34_u
        ),
        // DUP
        DUP.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x34_u),
        DUP.k.wst(0x12_u) leadsTo wst(0x12_u, 0x12_u),
        DUP.s.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x12_u, 0x34_u),
        // OVR
        OVR.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x12_u),
        OVR.k.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x12_u, 0x34_u, 0x12_u),
        OVR.s.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u) leadsTo wst(0x12_u, 0x34_u, 0x56_u, 0x78_u, 0x12_u, 0x34_u),
        OVR.s.k.wst(0x12_u, 0x34_u, 0x56_u, 0x78_u) leadsTo wst(
            0x12_u,
            0x34_u,
            0x56_u,
            0x78_u,
            0x12_u,
            0x34_u,
            0x56_u,
            0x78_u,
            0x12_u,
            0x34_u
        ),
        // EQU
        EQU.wst(0x12_u, 0x12_u) leadsTo wst(0x01_u),
        EQU.k.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x00_u),
        EQU.s.wst(0xab_u, 0xcd_u, 0xef_u, 0x01_u) leadsTo wst(0x00_u),
        EQU.s.k.wst(0xab_u, 0xcd_u, 0xab_u, 0xcd_u) leadsTo wst(0xab_u, 0xcd_u, 0xab_u, 0xcd_u, 0x01_u),
        // NEQ
        NEQ.wst(0x12_u, 0x12_u) leadsTo wst(0x00_u),
        NEQ.k.wst(0x12_u, 0x34_u) leadsTo wst(0x12_u, 0x34_u, 0x01_u),
        NEQ.s.wst(0xab_u, 0xcd_u, 0xef_u, 0x01_u) leadsTo wst(0x01_u),
        NEQ.s.k.wst(0xab_u, 0xcd_u, 0xab_u, 0xcd_u) leadsTo wst(0xab_u, 0xcd_u, 0xab_u, 0xcd_u, 0x00_u),
        // GTH
        GTH.wst(0x12_u, 0x34_u) leadsTo wst(0x00_u),
        GTH.k.wst(0x34_u, 0x12_u) leadsTo wst(0x34_u, 0x12_u, 0x01_u),
        GTH.s.wst(0x34_u, 0x56_u, 0x12_u, 0x34_u) leadsTo wst(0x01_u),
        GTH.s.k.wst(0x12_u, 0x34_u, 0x34_u, 0x56_u) leadsTo wst(0x12_u, 0x34_u, 0x34_u, 0x56_u, 0x00_u),
        // LTH
        LTH.wst(0x01_u, 0x01_u) leadsTo wst(0x00_u),
        LTH.k.wst(0x01_u, 0x00_u) leadsTo wst(0x01_u, 0x00_u, 0x00_u),
        LTH.s.wst(0x00_u, 0x01_u, 0x00_u, 0x00_u) leadsTo wst(0x00_u),
        LTH.s.k.wst(0x00_u, 0x01_u, 0x00_u, 0x00_u) leadsTo wst(0x00_u, 0x01_u, 0x00_u, 0x00_u, 0x00_u),
        // JMP
        // JCN
        // JSR
        // STH
        // LDZ
        // STZ
        // LDR
        // STR
        // LDA
        // STA
        // DEI
        // ADD
        ADD.wst(0x1a_u, 0x2e_u) leadsTo wst(0x48_u),
        ADD.k.wst(0x02_u, 0x5d_u) leadsTo wst(0x02_u, 0x5d_u, 0x5f_u),
        ADD.s.wst(0x00_u, 0x001_u, 0x00_u, 0x02_u) leadsTo wst(0x00_u, 0x03_u),
        // SUB
        SUB.wst(0x08_u, 0x03_u) leadsTo wst(0x05_u),
        SUB.k.wst(0x08_u, 0x02_u) leadsTo wst(0x08_u, 0x02_u, 0x06_u),
        SUB.s.wst(0x20_u, 0x00_u, 0x10_u, 0x00_u) leadsTo wst(0x10_u, 0x00_u),
        // MUL
        MUL.wst(0x06_u, 0x02_u) leadsTo wst(0xc_u),
        MUL.k.wst(0x08_u, 0x02_u) leadsTo wst(0x08_u, 0x02_u, 0x10_u),
        MUL.s.wst(0x08_u, 0x00_u, 0x00_u, 0x02_u) leadsTo wst(0x10_u, 0x00_u),
        // DIV
        DIV.wst(0x10_u, 0x2_u) leadsTo wst(0x8_u),
        DIV.k.wst(0x10_u, 0x3_u) leadsTo wst(0x10_u, 0x3_u, 0x5_u),
        DIV.s.wst(0x0010_u, 0x0000_u) leadsTo wst(0x00_u, 0x00_u),
        // AND
        AND.wst(0xfc_u, 0x3f_u) leadsTo wst(0x3c_u),
        // ORA
        ORA.wst(0xfc_u, 0x3f_u) leadsTo wst(0xff_u),
    )

    @TestFactory
    fun testOps() = tests.map { (state, expectation) ->
        DynamicTest.dynamicTest(state.opCode.str()) {
            val machine = UxnMachine(Console())
            state.wst?.forEach { byte ->
                machine.workingStack.push(byte)
            }
            machine.loadRom(UByteArray(1) { state.opCode })
            machine.step()

            for (expected in expectation.wst!!.reversed()) {
                val actual = machine.workingStack.pop()
                assertEquals(
                    expected, actual, """
                    expected: <${expected.toString(16)}> but was: <${actual.toString(16)}>
                    WS: ${machine.workingStack}
                    RS: ${machine.returnStack}
                """.trimIndent()
                )
            }
        }
    }
}