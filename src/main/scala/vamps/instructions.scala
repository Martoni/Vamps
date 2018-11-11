/* Note: Mainly copy/paste from riscv sodor processor */


package vamps

import chisel3._
import chisel3.util._

object VOP {  // Vamps opcode
  /* OPCODE */
  val LUI  = "b0110111".U
  val ADDI = "b0010011".U
  val ADD  = "b0110011".U

  val LOAD = "b0000011".U
  val CTRL = "b1110011".U
}

object VFUNC3 {
  /* FUNC3 */
  val LB  = "b000".U
  val LH  = "b001".U
  val LW  = "b010".U
  val LD  = "b011".U
  val LBU = "b100".U
  val LHU = "b101".U
  val LWU = "b110".U
  val ADDI= "b000".U
  val ADD = "b000".U
}

object Instructions {

  val r_type :: i_type:: s_type :: b_type :: u_type :: j_type :: Nil = Enum(6)

  def LUI                = BitPat("b?????????????????????????0110111")
  def AUIPC              = BitPat("b?????????????????????????0010111")
  def JAL                = BitPat("b?????????????????????????1101111")
  def JALR               = BitPat("b?????????????????000?????1100111")
  def BEQ                = BitPat("b?????????????????000?????1100011")
  def BNE                = BitPat("b?????????????????001?????1100011")
  def BLT                = BitPat("b?????????????????100?????1100011")
  def BGE                = BitPat("b?????????????????101?????1100011")
  def BLTU               = BitPat("b?????????????????110?????1100011")
  def BGEU               = BitPat("b?????????????????111?????1100011")
  def LB                 = BitPat("b?????????????????000?????0000011")
  def LH                 = BitPat("b?????????????????001?????0000011")
  def LW                 = BitPat("b?????????????????010?????0000011")
  def LBU                = BitPat("b?????????????????100?????0000011")
  def LHU                = BitPat("b?????????????????101?????0000011")
  def SB                 = BitPat("b?????????????????000?????0100011")
  def SH                 = BitPat("b?????????????????001?????0100011")
  def SW                 = BitPat("b?????????????????010?????0100011")
  def ADDI               = BitPat("b?????????????????000?????0010011")
  def SLTI               = BitPat("b?????????????????010?????0010011")
  def SLTIU              = BitPat("b?????????????????011?????0010011")
  def XORI               = BitPat("b?????????????????100?????0010011")
  def ORI                = BitPat("b?????????????????110?????0010011")
  def ANDI               = BitPat("b?????????????????111?????0010011")
  def SLLI               = BitPat("b000000???????????001?????0010011")
  def SRLI               = BitPat("b000000???????????101?????0010011")
  def SRAI               = BitPat("b010000???????????101?????0010011")
  def ADD                = BitPat("b0000000??????????000?????0110011")
  def SUB                = BitPat("b0100000??????????000?????0110011")
  def SLL                = BitPat("b0000000??????????001?????0110011")
  def SLT                = BitPat("b0000000??????????010?????0110011")
  def SLTU               = BitPat("b0000000??????????011?????0110011")
  def XOR                = BitPat("b0000000??????????100?????0110011")
  def SRL                = BitPat("b0000000??????????101?????0110011")
  def SRA                = BitPat("b0100000??????????101?????0110011")
  def OR                 = BitPat("b0000000??????????110?????0110011")
  def AND                = BitPat("b0000000??????????111?????0110011")
  def FENCE              = BitPat("b?????????????????000?????0001111")
  def FENCE_I            = BitPat("b?????????????????001?????0001111")
  def ECALL              = BitPat("b00000000000000000000000001110011")
  def EBREAK             = BitPat("b00000000000100000000000001110011")
  def CSRRW              = BitPat("b?????????????????001?????1110011")
  def CSRRS              = BitPat("b?????????????????010?????1110011")
  def CSRRC              = BitPat("b?????????????????011?????1110011")
  def CSRRWI             = BitPat("b?????????????????101?????1110011")
  def CSRRSI             = BitPat("b?????????????????110?????1110011")
  def CSRRCI             = BitPat("b?????????????????111?????1110011")

  def decode( ifid: UInt) : List[UInt] = {
      ListLookup(ifid,
                             List(N, BR_N  , OP1_X , OP2_X    , OEN_0, OEN_0, ALU_X   , WB_X  ,  REN_0, MEN_0, M_X  , MT_X, CSR.N, N),
               Array(       /* val  |  BR  |  op1  |   op2     |  R1  |  R2  |  ALU    |  wb   | rf   | mem  | mem  | mask | csr | fence.i */
                            /* inst | type |   sel |    sel    |  oen |  oen |   fcn   |  sel  | wen  |  en  |  wr  | type | cmd |         */
                  LW     -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_MEM, REN_1, MEN_1, M_XRD, MT_W, CSR.N, N),
                  LB     -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_MEM, REN_1, MEN_1, M_XRD, MT_B, CSR.N, N),
                  LBU    -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_MEM, REN_1, MEN_1, M_XRD, MT_BU,CSR.N, N),
                  LH     -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_MEM, REN_1, MEN_1, M_XRD, MT_H, CSR.N, N),
                  LHU    -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_MEM, REN_1, MEN_1, M_XRD, MT_HU,CSR.N, N),
                  SW     -> List(Y, BR_N  , OP1_RS1, OP2_STYPE , OEN_1, OEN_1, ALU_ADD , WB_X  , REN_0, MEN_1, M_XWR, MT_W, CSR.N, N),
                  SB     -> List(Y, BR_N  , OP1_RS1, OP2_STYPE , OEN_1, OEN_1, ALU_ADD , WB_X  , REN_0, MEN_1, M_XWR, MT_B, CSR.N, N),
                  SH     -> List(Y, BR_N  , OP1_RS1, OP2_STYPE , OEN_1, OEN_1, ALU_ADD , WB_X  , REN_0, MEN_1, M_XWR, MT_H, CSR.N, N),

                  AUIPC  -> List(Y, BR_N  , OP1_PC , OP2_UTYPE , OEN_0, OEN_0, ALU_ADD   ,WB_ALU,REN_1, MEN_0, M_X , MT_X,  CSR.N, N),
                  LUI    -> List(Y, BR_N  , OP1_X  , OP2_UTYPE , OEN_0, OEN_0, ALU_COPY_2,WB_ALU,REN_1, MEN_0, M_X , MT_X,  CSR.N, N),

                  ADDI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  ANDI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_AND , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  ORI    -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_OR  , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  XORI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_XOR , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SLTI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_SLT , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SLTIU  -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_SLTU, WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SLLI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_SLL , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SRAI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_SRA , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SRLI   -> List(Y, BR_N  , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_SRL , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),

                  SLL    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_SLL , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  ADD    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_ADD , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SUB    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_SUB , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SLT    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_SLT , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SLTU   -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_SLTU, WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  AND    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_AND , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  OR     -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_OR  , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  XOR    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_XOR , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SRA    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_SRA , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  SRL    -> List(Y, BR_N  , OP1_RS1, OP2_RS2   , OEN_1, OEN_1, ALU_SRL , WB_ALU, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),

                  JAL    -> List(Y, BR_J  , OP1_RS1, OP2_UJTYPE, OEN_0, OEN_0, ALU_X   , WB_PC4, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  JALR   -> List(Y, BR_JR , OP1_RS1, OP2_ITYPE , OEN_1, OEN_0, ALU_ADD , WB_PC4, REN_1, MEN_0, M_X  , MT_X, CSR.N, N),
                  BEQ    -> List(Y, BR_EQ , OP1_RS1, OP2_SBTYPE, OEN_1, OEN_1, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, N),
                  BNE    -> List(Y, BR_NE , OP1_RS1, OP2_SBTYPE, OEN_1, OEN_1, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, N),
                  BGE    -> List(Y, BR_GE , OP1_RS1, OP2_SBTYPE, OEN_1, OEN_1, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, N),
                  BGEU   -> List(Y, BR_GEU, OP1_RS1, OP2_SBTYPE, OEN_1, OEN_1, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, N),
                  BLT    -> List(Y, BR_LT , OP1_RS1, OP2_SBTYPE, OEN_1, OEN_1, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, N),
                  BLTU   -> List(Y, BR_LTU, OP1_RS1, OP2_SBTYPE, OEN_1, OEN_1, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, N),

                  CSRRWI -> List(Y, BR_N  , OP1_IMZ, OP2_X     , OEN_1, OEN_1, ALU_COPY_1,WB_CSR,REN_1, MEN_0, M_X  , MT_X, CSR.W, N),
                  CSRRSI -> List(Y, BR_N  , OP1_IMZ, OP2_X     , OEN_1, OEN_1, ALU_COPY_1,WB_CSR,REN_1, MEN_0, M_X  , MT_X, CSR.S, N),
                  CSRRW  -> List(Y, BR_N  , OP1_RS1, OP2_X     , OEN_1, OEN_1, ALU_COPY_1,WB_CSR,REN_1, MEN_0, M_X  , MT_X, CSR.W, N),
                  CSRRS  -> List(Y, BR_N  , OP1_RS1, OP2_X     , OEN_1, OEN_1, ALU_COPY_1,WB_CSR,REN_1, MEN_0, M_X  , MT_X, CSR.S, N),
                  CSRRC  -> List(Y, BR_N  , OP1_RS1, OP2_X     , OEN_1, OEN_1, ALU_COPY_1,WB_CSR,REN_1, MEN_0, M_X  , MT_X, CSR.C, N),
                  CSRRCI -> List(Y, BR_N  , OP1_IMZ, OP2_X     , OEN_1, OEN_1, ALU_COPY_1,WB_CSR,REN_1, MEN_0, M_X  , MT_X, CSR.C, N),

                  ECALL  -> List(Y, BR_N  , OP1_X  , OP2_X     , OEN_0, OEN_0, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.I, N),
                  EBREAK -> List(Y, BR_N  , OP1_X  , OP2_X     , OEN_0, OEN_0, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.I, N),

                  FENCE_I-> List(Y, BR_N  , OP1_X  , OP2_X     , OEN_0, OEN_0, ALU_X   , WB_X  , REN_0, MEN_0, M_X  , MT_X, CSR.N, Y),
                  // kill pipeline and refetch instructions since the pipeline will be holding stall instructions.
                  FENCE  -> List(Y, BR_N  , OP1_X  , OP2_X     , OEN_0, OEN_0, ALU_X   , WB_X  , REN_0, MEN_1, M_X  , MT_X, CSR.N, N)
                  // we are already sequentially consistent, so no need to honor the fence instruction
                  ))
  }

   //************************************
   // Control Signals

   val Y      = true.B
   val N      = false.B

   // Register Operand Output Enable Signal
   val OEN_0   = false.B
   val OEN_1   = true.B

   // PC Select Signal
   val PC_4   = 0.U(3.W)  // PC + 4
   val PC_BR  = 1.U(3.W)  // branch_target
   val PC_J   = 2.U(3.W)  // jump_target
   val PC_JR  = 3.U(3.W)  // jump_reg_target
   val PC_EXC = 4.U(3.W)  // exception

   // Branch Type
   val BR_N   = 0.U(4.W)  // Next
   val BR_NE  = 1.U(4.W)  // Branch on NotEqual
   val BR_EQ  = 2.U(4.W)  // Branch on Equal
   val BR_GE  = 3.U(4.W)  // Branch on Greater/Equal
   val BR_GEU = 4.U(4.W)  // Branch on Greater/Equal Unsigned
   val BR_LT  = 5.U(4.W)  // Branch on Less Than
   val BR_LTU = 6.U(4.W)  // Branch on Less Than Unsigned
   val BR_J   = 7.U(4.W)  // Jump
   val BR_JR  = 8.U(4.W)  // Jump Register

   // RS1 Operand Select Signal
   val OP1_RS1   = 0.asUInt(2.W) // Register Source #1
   val OP1_PC    = 1.asUInt(2.W) // PC
   val OP1_IMZ   = 2.asUInt(2.W) // Zero-extended Immediate from RS1 field, for use by CSRI instructions
   val OP1_X     = 0.asUInt(2.W)

   // RS2 Operand Select Signal
   val OP2_RS2    = 0.asUInt(3.W) // Register Source #2
   val OP2_ITYPE  = 1.asUInt(3.W) // immediate, I-type
   val OP2_STYPE  = 2.asUInt(3.W) // immediate, S-type
   val OP2_SBTYPE = 3.asUInt(3.W) // immediate, B
   val OP2_UTYPE  = 4.asUInt(3.W) // immediate, U-type
   val OP2_UJTYPE = 5.asUInt(3.W) // immediate, J-type
   val OP2_X      = 0.asUInt(3.W)

   // Register File Write Enable Signal
   val REN_0   = false.B
   val REN_1   = true.B
   val REN_X   = false.B

   // ALU Operation Signal
   val ALU_ADD = 1.U(4.W)
   val ALU_SUB = 2.U(4.W)
   val ALU_SLL = 3.U(4.W)
   val ALU_SRL = 4.U(4.W)
   val ALU_SRA = 5.U(4.W)
   val ALU_AND = 6.U(4.W)
   val ALU_OR  = 7.U(4.W)
   val ALU_XOR = 8.U(4.W)
   val ALU_SLT = 9.U(4.W)
   val ALU_SLTU= 10.U(4.W)
   val ALU_COPY_1= 11.U(4.W)
   val ALU_COPY_2= 12.U(4.W)
   val ALU_X   = 0.U(4.W)

   // Writeback Select Signal
   val WB_ALU  = 0.U(2.W)
   val WB_MEM  = 1.U(2.W)
   val WB_PC4  = 2.U(2.W)
   val WB_CSR  = 3.U(2.W)
   val WB_X    = 0.U(2.W)

   // Memory Function Type (Read,Write,Fence) Signal
   val MWR_R   = 0.U(2.W)
   val MWR_W   = 1.U(2.W)
   val MWR_F   = 2.U(2.W)
   val MWR_X   = 0.U(2.W)

   // Memory Enable Signal
   val MEN_0   = false.B
   val MEN_1   = true.B
   val MEN_X   = false.B

   // Memory Mask Type Signal
   val MSK_B   = 0.U(3.W)
   val MSK_BU  = 1.U(3.W)
   val MSK_H   = 2.U(3.W)
   val MSK_HU  = 3.U(3.W)
   val MSK_W   = 4.U(3.W)
   val MSK_X   = 4.U(3.W)


   // Cache Flushes & Sync Primitives
   val M_N      = 0.U(3.W)
   val M_SI     = 1.U(3.W)   // synch instruction stream
   val M_SD     = 2.U(3.W)   // synch data stream
   val M_FA     = 3.U(3.W)   // flush all caches
   val M_FD     = 4.U(3.W)   // flush data cache

   // Memory Functions (read, write, fence)
   val MT_READ  = 0.U(2.W)
   val MT_WRITE = 1.U(2.W)
   val MT_FENCE = 2.U(2.W)

   val MT_X  = 0.asUInt(3.W)
   val MT_B  = 1.asUInt(3.W)
   val MT_H  = 2.asUInt(3.W)
   val MT_W  = 3.asUInt(3.W)
   val MT_D  = 4.asUInt(3.W)
   val MT_BU = 5.asUInt(3.W)
   val MT_HU = 6.asUInt(3.W)
   val MT_WU = 7.asUInt(3.W)

   val M_X   = "b0".asUInt(1.W)
   val M_XRD = "b0".asUInt(1.W) // int load
   val M_XWR = "b1".asUInt(1.W) // int store

   val DPORT = 0
   val IPORT = 1
}

object CSR
{
  // commands
  val SZ = 3.W
  val X = 0.asUInt(SZ)
  val N = 0.asUInt(SZ)
  val W = 1.asUInt(SZ)
  val S = 2.asUInt(SZ)
  val C = 3.asUInt(SZ)
  val I = 4.asUInt(SZ)
  val R = 5.asUInt(SZ)

//  val ADDRSZ = 12
//  val firstCtr = CSRs.cycle
//  val firstCtrH = CSRs.cycleh
//  val firstHPC = CSRs.hpmcounter3
//  val firstHPCH = CSRs.hpmcounter3h
//  //val firstHPE = CSRs.mhpmevent3
//  val firstMHPC = CSRs.mhpmcounter3
//  val firstMHPCH = CSRs.mhpmcounter3h
//  val firstHPM = 3
//  val nCtr = 32
//  val nHPM = nCtr - firstHPM
//  val hpmWidth = 40
}


