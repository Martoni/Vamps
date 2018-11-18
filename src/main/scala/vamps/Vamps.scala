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
  }

  /* instruction decode */
  val decoded = Instructions.decode(IFID)
  val rs1 = RegInit(0.U(32.W))
  val rs2 = RegInit(0.U(32.W))
  val immreg = RegInit(0.S(32.W))

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
    when(decoded(3) === Instructions.OP2_ITYPE){
      immreg := Cat(Fill(20, IFID(31)), IFID(31, 20)).asSInt
    }
    when(decoded(3) === Instructions.OP2_UTYPE) {
      immreg := Cat(IFID(31, 12), 0.U(12.W)).asSInt
    }
	when(decoded(3) === Instructions.OP2_UJTYPE) {
	  immreg := Cat(Fill(12, IFID(31)), IFID(19, 12), IFID(20), IFID(30, 21), 0.U(1.W)).asSInt
	}
  }

  /* Execute stage */

  val ALURes = RegInit(0.S(32.W))
  when(p_pipe === P_EXEC) {
    when(opcode === VOP.ADDI && func3 === VFUNC3.ADDI) {
      ALURes := rs1.asSInt + immreg
    }
    when(opcode === VOP.ADD && func3 === VFUNC3.ADD) {
      ALURes := (rs1 + rs2).asSInt
    }

    PCREG := PCREG + 4.U
    when(opcode === VOP.AUIPC ||
         opcode === VOP.JAL) {
      PCREG  := (PCREG.asSInt + immreg).asUInt
      ALURes := PCREG.asSInt + immreg
    }
  }

  when(p_pipe === P_MEM) {
  }

  when(p_pipe === P_WB) {
    when(rdnum =/= 0.U) {
      when(opcode === VOP.ADDI && func3 === VFUNC3.ADDI) {
        regfile(rdnum - 1.U) := ALURes.asUInt
      }
      when(opcode === VOP.ADD && func3 === VFUNC3.ADD) {
        regfile(rdnum - 1.U) := ALURes.asUInt
      }
      when(opcode === VOP.AUIPC) {
		regfile(rdnum - 1.U) := ALURes.asUInt
	  }
      when(opcode === VOP.LUI) {
        regfile(rdnum - 1.U) := immreg.asUInt
      }
    }
  }

  /* tester be quiet */
  io.wr := RegInit(false.B)
  io.rd := RegInit(false.B)
  io.datao := RegInit(0.U(32.W))
  io.daddr := RegInit(0.U(32.W))
}
