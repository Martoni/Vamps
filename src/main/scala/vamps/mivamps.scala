package vamps

// scala imports
import scala.collection.mutable.ListBuffer
import scala.io.Source // for files 
import java.io.File

import chisel3._
import chisel3.util._

class MiVamps extends Module {
  val io = IO(new Bundle {
    val led01 = Output(Bool())
  })

  val hex_asm_path = "src/firmware/vamps.hex"

  var irom = new ListBuffer[Int]()
  for(line <- Source.fromFile(hex_asm_path).getLines){
    irom += Integer.parseUnsignedInt(line, 16)
  }

//  val imem = VecInit(irom)
  
  io.led01 := true.B

  //Vamps processor connection
  val vamps = Module(new Vamps())
  vamps.io.idata := 0.U(32.W)
  vamps.io.datai := 0.U(32.W) 
}

// Driver for verilog generation code
object MiVampsDriver extends App {
  chisel3.Driver.execute(args, () => new MiVamps)
}
