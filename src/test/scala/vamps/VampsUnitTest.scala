// See README.md for license details.

package vamps

// scala imports
import scala.collection.mutable.ListBuffer
import scala.io.Source // for files 
import java.io.File

// chisel tester imports
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class VampsUnitTester(c: Vamps) extends PeekPokeTester(c) {

  val hex_asm_path = "src/firmware/vamps.hex"

  def toBinary(i: Int, digits: Int = 8) =
    String.format("%" + digits + "s", i.toBinaryString).replace(' ', '0')

  
  var irom = new ListBuffer[Int]()
  for(line <- Source.fromFile(hex_asm_path).getLines){
    irom += Integer.parseUnsignedInt(line, 16)
  }

  var i = 0;
  for(i <- 0 until 7){
	val test = peek(c.io.iaddr)/4
	println(test.toString)
    poke(c.io.idata, irom(peek(c.io.iaddr).toInt/4))
    step(5)
  }

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
