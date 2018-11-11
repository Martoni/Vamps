// See README.md for license details.

package vamps

import java.io.File

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class VampsUnitTester(c: Vamps) extends PeekPokeTester(c) {

  def toBinary(i: Int, digits: Int = 8) =
    String.format("%" + digits + "s", i.toBinaryString).replace(' ', '0')

  val LOAD = "0000011"
  val LUI =  "0110111"
  val rsnum = "00001"
  val OPADDI = "0010011"
  val OPADD = "0110011"
  val FUNCADDI = "000"
  val FUNCADD = "000"

  var data = "01"*10
  var imm = toBinary(0x7af, 12)
  var rs1num = toBinary(15, 5)
  var rs2num = toBinary(0, 5)
  var rdnum = toBinary(5, 5)

  imm = toBinary(0x1, 20)
  poke(c.io.idata, Integer.parseInt(imm + rdnum + LUI, 2))
  step(5)
  rdnum = toBinary(6, 5)
  poke(c.io.idata, Integer.parseInt(imm + rs1num + FUNCADDI + rdnum + OPADDI, 2))
  step(5)
  imm = toBinary(0xfae, 12)
  rs1num = toBinary(5, 5)
  rs2num = toBinary(6, 5)
  rdnum = toBinary(7, 5)
  poke(c.io.idata, Integer.parseUnsignedInt(rs2num + rs1num + FUNCADD + rdnum + OPADD, 2))
  step(5)
  data = toBinary(0x1cafe, 20)
  rdnum = toBinary(2, 5)
  poke(c.io.idata, Integer.parseInt(data + rdnum + LUI, 2))
  step(5)
  poke(c.io.idata, Integer.parseInt(data + rsnum + LOAD, 2))
  step(5)
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly example.test.VampsTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly example.test.VampsTester'
  * }}}
  */
class VampsTester extends ChiselFlatSpec {
  // Disable this until we fix isCommandAvailable to swallow stderr along with stdout
  private val backendNames = if(false && firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "Vamps" should s"calculate proper greatest common denominator (with $backendName)" in {
      Driver(() => new Vamps, backendName) {
        c => new VampsUnitTester(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new Vamps) {
      c => new VampsUnitTester(c)
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new Vamps) {
        c => new VampsUnitTester(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new Vamps) {
      c => new VampsUnitTester(c)
    } should be(true)
  }

  /**
    * By default verilator backend produces vcd file, and firrtl and treadle backends do not.
    * Following examples show you how to turn on vcd for firrtl and treadle and how to turn it off for verilator
    */

  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/make_a_vcd", "--top-name", "make_a_vcd"),
      () => new Vamps
    ) {

      c => new VampsUnitTester(c)
    } should be(true)

    new File("test_run_dir/make_a_vcd/make_a_vcd.vcd").exists should be (true)
  }

  "running with --generate-vcd-output off" should "not create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "off", "--target-dir", "test_run_dir/make_no_vcd", "--top-name", "make_no_vcd",
      "--backend-name", "verilator"),
      () => new Vamps
    ) {

      c => new VampsUnitTester(c)
    } should be(true)

    new File("test_run_dir/make_no_vcd/make_a_vcd.vcd").exists should be (false)

  }

}
