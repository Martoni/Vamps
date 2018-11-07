// See LICENSE for details
//

package vamps

import chisel3._


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

  /* fetch */
  val IFID = RegNext(io.idata)
  io.debugIFID := IFID

  /* instruction decode */
  val rs1 = RegInit(0.U(32))
  val rs2 = RegInit(0.U(32))
  val opcode = RegInit(0.U(32))
  val rs1num = Wire(UInt(5.W))
  val rs2num = Wire(UInt(5.W))
  io.debugrs1num := rs1num
  io.debugrs2num := rs2num

  rs1num := IFID(19, 15)
  rs2num := IFID(24, 20)

  rs1 := Mux(rs1num =/= 0.U, regfile(rs1num - 1.U), 0.U)
  rs2 := Mux(rs2num =/= 0.U, regfile(rs2num - 1.U), 0.U)

  opcode := IFID(6,0)

  io.debugrs1 := rs1
  io.debugrs2 := rs2

  /* tester be quiet */
  io.wr := RegInit(false.B)
  io.rd := RegInit(false.B)
  io.datao := RegInit(12.U(32.W))
  io.daddr := RegInit(0.U(32.W))
  io.iaddr := RegInit(0.U(32.W))
}
