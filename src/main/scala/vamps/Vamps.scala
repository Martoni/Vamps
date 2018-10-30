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
  })

  val LUI = "b0110111".U

  /* tester be quiet */
  io.wr := RegInit(false.B)
  io.rd := RegInit(false.B)
  io.datao := RegInit(12.U(32.W))
  io.daddr := RegInit(0.U(32.W))
  io.iaddr := RegInit(0.U(32.W))
}
