import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class UxnMachineTest {

    private val UByte.k: UByte get() = (this + 0x80u).toUByte()
    private val UByte.r: UByte get() = (this + 0x40u).toUByte()
    private val UByte.s: UByte get() = (this + 0x20u).toUByte()

    private val tests = listOf(
        // INC
        Triple(INC, ubyteArrayOf(0x1u), ubyteArrayOf(0x2u)),
        Triple(INC.s, ubyteArrayOf(0x00u, 0x01u), ubyteArrayOf(0x00u, 0x02u)),
        Triple(INC.s.k, ubyteArrayOf(0x00u, 0x01u), ubyteArrayOf(0x00u, 0x01u, 0x00u, 0x02u)),
        // POP
        Triple(POP, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u)),
        Triple(POP.s, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf()), // TODO: Emptiness check
        Triple(POP.s.k, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u)),
        // NIP
        Triple(NIP, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x34u)),
        Triple(NIP.s, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u), ubyteArrayOf(0x56u, 0x78u)),
        Triple(NIP.s.k, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u), ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x56u, 0x78u)),
        // SWP
        Triple(SWP, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x34u, 0x12u)),
        Triple(SWP.k, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x34u, 0x12u)),
        Triple(SWP.s, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u), ubyteArrayOf(0x56u, 0x78u, 0x12u, 0x34u)),
        Triple(SWP.s.k, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u), ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x56u, 0x78u, 0x12u, 0x34u)),
        // ROT
        Triple(ROT, ubyteArrayOf(0x12u, 0x34u, 0x56u), ubyteArrayOf(0x34u, 0x56u, 0x12u)),
        Triple(ROT.k, ubyteArrayOf(0x12u, 0x34u, 0x56u), ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x34u, 0x56u, 0x12u)),
        Triple(ROT.s, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x9au, 0xbcu), ubyteArrayOf(0x56u, 0x78u, 0x9au, 0xbcu, 0x12u, 0x34u)),
        Triple(ROT.s.k, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x9au, 0xbcu), ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x9au, 0xbcu, 0x56u, 0x78u, 0x9au, 0xbcu, 0x12u, 0x34u)),
        // DUP
        Triple(DUP, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x34u)),
        Triple(DUP.k, ubyteArrayOf(0x12u), ubyteArrayOf(0x12u, 0x12u)),
        Triple(DUP.s, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x12u, 0x34u)),
        // OVR
        Triple(OVR, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x12u)),
        Triple(OVR.k, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x12u, 0x34u, 0x12u)),
        Triple(OVR.s, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u), ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x12u, 0x34u)),
        Triple(OVR.s.k, ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u), ubyteArrayOf(0x12u, 0x34u, 0x56u, 0x78u, 0x12u, 0x34u, 0x56u, 0x78u, 0x12u, 0x34u)),
        // EQU
        Triple(EQU, ubyteArrayOf(0x12u, 0x12u), ubyteArrayOf(0x01u)),
        Triple(EQU.k, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x00u)),
        Triple(EQU.s, ubyteArrayOf(0xabu, 0xcdu, 0xefu, 0x01u), ubyteArrayOf(0x00u)),
        Triple(EQU.s.k, ubyteArrayOf(0xabu, 0xcdu, 0xabu, 0xcdu), ubyteArrayOf(0xabu, 0xcdu, 0xabu, 0xcdu, 0x01u)),
        // NEQ
        Triple(NEQ, ubyteArrayOf(0x12u, 0x12u), ubyteArrayOf(0x00u)),
        Triple(NEQ.k, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x12u, 0x34u, 0x01u)),
        Triple(NEQ.s, ubyteArrayOf(0xabu, 0xcdu, 0xefu, 0x01u), ubyteArrayOf(0x01u)),
        Triple(NEQ.s.k, ubyteArrayOf(0xabu, 0xcdu, 0xabu, 0xcdu), ubyteArrayOf(0xabu, 0xcdu, 0xabu, 0xcdu, 0x00u)),
        // GTH
        Triple(GTH, ubyteArrayOf(0x12u, 0x34u), ubyteArrayOf(0x00u)),
        Triple(GTH.k, ubyteArrayOf(0x34u, 0x12u), ubyteArrayOf(0x34u, 0x12u, 0x01u)),
        Triple(GTH.s, ubyteArrayOf(0x34u, 0x56u, 0x12u, 0x34u), ubyteArrayOf(0x01u)),
        Triple(GTH.s.k, ubyteArrayOf(0x12u, 0x34u, 0x34u, 0x56u), ubyteArrayOf(0x12u, 0x34u, 0x34u, 0x56u, 0x00u)),
        // LTH
        Triple(LTH, ubyteArrayOf(0x01u, 0x01u), ubyteArrayOf(0x00u)),
        Triple(LTH.k, ubyteArrayOf(0x01u, 0x00u), ubyteArrayOf(0x01u, 0x00u, 0x00u)),
        Triple(LTH.s, ubyteArrayOf(0x00u, 0x01u, 0x00u, 0x00u), ubyteArrayOf(0x00u)),
        Triple(LTH.s.k, ubyteArrayOf(0x00u, 0x01u, 0x00u, 0x00u), ubyteArrayOf(0x00u, 0x01u, 0x00u, 0x00u, 0x00u)),
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
        Triple(ADD, ubyteArrayOf(0x1au, 0x2eu), ubyteArrayOf(0x48u)),
        Triple(ADD.k, ubyteArrayOf(0x02u, 0x5du), ubyteArrayOf(0x02u, 0x5du, 0x5fu)),
        Triple(ADD.s, ubyteArrayOf(0x00u, 0x001u, 0x00u, 0x02u), ubyteArrayOf(0x00u, 0x03u)),
        // SUB
        Triple(SUB, ubyteArrayOf(0x08u, 0x03u), ubyteArrayOf(0x05u)),
        Triple(SUB.k, ubyteArrayOf(0x08u, 0x02u), ubyteArrayOf(0x08u, 0x02u, 0x06u)),
        Triple(SUB.s, ubyteArrayOf(0x20u, 0x00u, 0x10u, 0x00u), ubyteArrayOf(0x10u, 0x00u)),
        // MUL
        Triple(MUL, ubyteArrayOf(0x06u, 0x02u), ubyteArrayOf(0xcu)),
        Triple(MUL.k, ubyteArrayOf(0x08u, 0x02u), ubyteArrayOf(0x08u, 0x02u, 0x10u)),
        Triple(MUL.s, ubyteArrayOf(0x08u, 0x00u, 0x00u, 0x02u), ubyteArrayOf(0x10u, 0x00u)),
        // DIV
        Triple(DIV, ubyteArrayOf(0x10u, 0x2u), ubyteArrayOf(0x8u)),
        Triple(DIV.k, ubyteArrayOf(0x10u, 0x3u), ubyteArrayOf(0x10u, 0x3u, 0x5u)),
        Triple(DIV.s, ubyteArrayOf(0x0010u, 0x0000u), ubyteArrayOf(0x00u, 0x00u)),
        // AND
        Triple(AND, ubyteArrayOf(0xfcu, 0x3fu), ubyteArrayOf(0x3cu)),
        // ORA
        Triple(ORA, ubyteArrayOf(0xfcu, 0x3fu), ubyteArrayOf(0xffu)),
    )

    @TestFactory
    fun testOps() = tests.map { (opCode, before, after) ->
        DynamicTest.dynamicTest(opCode.str()) {
            val machine = UxnMachine()
            for (byte in before) {
                machine.workingStack.push(byte)
            }
            machine.loadRom(UByteArray(1) { opCode })
            machine.step()
            for (expected in after.reversed()) {
                val actual = machine.workingStack.pop()
                assertEquals(expected, actual, """
                    expected: <${expected.toString(16)}> but was: <${actual.toString(16)}>
                    WS: ${machine.workingStack}
                    RS: ${machine.returnStack}
                """.trimIndent())
            }
        }
    }
}