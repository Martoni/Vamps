// See LICENSE for details
// simple RV32I cpu core

package vamps

import chisel3._
import chisel3.util._


class Vamps extends Module {
  val io = IO(new Bundle {
    /* instructions */
    val idata = Input(UInt(32.W))
    val iaddr = Output(UInt(32.W))

    /* data */
    val datai = Input(UInt(32.W))
    val datao = Output(UInt(32.W))
    val daddr = Output(UInt(32.W))

    val wr = Output(Bool())
    val rd = Output(Bool())

    val debugIFID = Output(UInt(32.W))
    val debugrs1 = Output(UInt(32.W))
    val debugrs2 = Output(UInt(32.W))
    val debugrs1num = Output(UInt(5.W))
    val debugrs2num = Output(UInt(5.W))
    val debugrdnum = Output(UInt(5.W))
    val debugpseudo_pipe = Output(UInt(5.W))
  })

  val INIT_ADDR = 0

  val PCREG = RegInit(INIT_ADDR.U(32.W))
  io.iaddr := PCREG

  /* OPCODE */
  val LUI  = "b0110111".U
  val LOAD = "b0000011".U
  val CTRL = "b1110011".U
  /* FUNC3 */
  val LB  = "b000".U
  val LH  = "b001".U
  val LW  = "b010".U
  val LD  = "b011".U
  val LBU = "b100".U
  val LHU = "b101".U
  val LWU = "b110".U

  /* registers file */
  val regfile = RegInit(VecInit(Seq.fill(31)(0.U(32.W))))

  /* Pseudo pipe declaration */

  /*  fetch   | decode    |execution | memory| writeback */
  val P_FETCH  = "b00001".U
  val P_DECODE = "b00010".U
  val P_EXEC   = "b00100".U
  val P_MEM    = "b01000".U
  val P_WB     = "b10000".U

  /* Pseudo pipe declaration */
  val p_pipe = RegInit(1.U(5.W))
  p_pipe := Cat(p_pipe(3, 0), p_pipe(4))
  io.debugpseudo_pipe := p_pipe

  /* Fetch */
  val IFID = RegInit(0.U(32.W))
  io.debugIFID := IFID

  when(p_pipe === P_FETCH) {
    IFID := io.idata
    PCREG := PCREG + 4.U
  }

  /* instruction decode */
  val decoded = Instructions.decode(IFID)
  val rs1 = RegInit(0.U(32))
  val rs2 = RegInit(0.U(32))
  val imm = RegInit(0.U(32))

  val opcode = Wire(UInt(7.W))
  val func3 = Wire(UInt(3.W))
  val func7 = Wire(UInt(7.W))
  val rs1num = Wire(UInt(5.W))
  val rs2num = Wire(UInt(5.W))
  val rdnum = Wire(UInt(5.W))

  io.debugrs1num := rs1num
  io.debugrs2num := rs2num
  rs1num := IFID(19, 15)
  rs2num := IFID(24, 20)
  rdnum  := IFID(11, 7)
  opcode := IFID(6, 0)
  func3  := IFID(14, 12)
  func7  := IFID(31, 25)

  io.debugrs1 := rs1
  io.debugrs2 := rs2
  io.debugrdnum := rdnum

  when(p_pipe === P_DECODE) {
    rs1 := Mux(rs1num =/= 0.U, regfile(rs1num - 1.U), 0.U)
    rs2 := Mux(rs2num =/= 0.U, regfile(rs2num - 1.U), 0.U)
    when((opcode === "b0010011".U) && func3 === "b000".U) { // ADDI
      imm := IFID(31, 20)
    }
  }

  /* Execute stage */

  val ALURes = RegInit(0.U(32.W))
  when(p_pipe === P_EXEC) {
    when(opcode === "b0010011".U && func3 === "b000".U) { // ADDI
      ALURes := imm + rs1 
    }
  }

  when(p_pipe === P_MEM) {
  }

  when(p_pipe === P_WB) {
    when(opcode === "b0010011".U && func3 === "b000".U) { // ADDI
      when(rdnum =/= 0.U) {
        regfile(rdnum - 1.U) := ALURes
      }
    }
  }

  /* tester be quiet */
  io.wr := RegInit(false.B)
  io.rd := RegInit(false.B)
  io.datao := RegInit(0.U(32.W))
  io.daddr := RegInit(0.U(32.W))
}
