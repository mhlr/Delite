package ppl.dsl.optila

import java.io._
import scala.reflect.SourceContext
import scala.virtualization.lms.common._
import scala.virtualization.lms.internal.{GenericFatCodegen, GenericCodegen}
import ppl.delite.framework.{Config, ExpressionsOpt, SchedulingOpt, DeliteApplication, DeliteInteractive, DeliteInteractiveRunner}
import ppl.delite.framework.datastructures._
import ppl.delite.framework.codegen.Target
import ppl.delite.framework.codegen.scala.TargetScala
import ppl.delite.framework.codegen.cuda.TargetCuda
import ppl.delite.framework.codegen.c.TargetC
import ppl.delite.framework.codegen.opencl.TargetOpenCL
import ppl.delite.framework.codegen.delite.overrides._
import ppl.delite.framework.ops._
import ppl.dsl.optila.extern._
import ppl.dsl.optila.vector._
import ppl.dsl.optila.matrix._
import ppl.dsl.optila.capabilities._
import ppl.dsl.optila.io._
import ppl.dsl.optila.generic.GenericDefs

/**
 * These separate OptiLA applications from the Exp world.
 */

// ex. object GDARunner extends OptiLAApplicationRunner with GDA
trait OptiLAApplicationRunner extends OptiLAApplication with DeliteApplication with OptiLAExp

// ex. trait GDA extends OptiLAApplication
trait OptiLAApplication extends OptiLA with OptiLALift {
  var args: Rep[Array[String]]
  def main(): Unit
}

trait OptiLAInteractive extends OptiLAApplication with DeliteInteractive

trait OptiLAInteractiveRunner extends OptiLAApplicationRunner with DeliteInteractiveRunner

object OptiLA {
  def apply[R](b: => R) = new Scope[OptiLAInteractive, OptiLAInteractiveRunner, R](b)
}


/**
 * These are the portions of Scala imported into OptiLA's scope.
 */
trait OptiLALift extends LiftVariables with LiftEquals with LiftString with LiftBoolean /*with LiftNumeric*/ with LiftPrimitives {
  this: OptiLA =>
}

trait OptiLAScalaOpsPkg extends Base
  with Equal with IfThenElse with Variables with While with Functions
  with ImplicitOps with OrderingOps with StringOps
  with BooleanOps with PrimitiveOps with MiscOps with TupleOps
  with CastingOps with ObjectOps with IOOps
  // only included because of args. TODO: investigate passing args as a vector
  with ArrayOps with ExceptionOps

trait OptiLAScalaOpsPkgExp extends OptiLAScalaOpsPkg with DSLOpsExp
  with EqualExp with IfThenElseExp with VariablesExp with WhileExp with FunctionsExp
  with ImplicitOpsExp with OrderingOpsExp with StringOpsExp with RangeOpsExp with IOOpsExp
  with ArrayOpsExp with BooleanOpsExp with PrimitiveOpsExp with MiscOpsExp with TupleOpsExp
  with ListOpsExp with SeqOpsExp with MathOpsExp with CastingOpsExp with SetOpsExp with ObjectOpsExp
  with SynchronizedArrayBufferOpsExp with HashMapOpsExp with IterableOpsExp with ArrayBufferOpsExp with ExceptionOpsExp

trait OptiLAScalaCodeGenPkg extends ScalaGenDSLOps
  with ScalaGenEqual with ScalaGenIfThenElse with ScalaGenVariables with ScalaGenWhile with ScalaGenFunctions
  with ScalaGenImplicitOps with ScalaGenOrderingOps with ScalaGenStringOps with ScalaGenRangeOps with ScalaGenIOOps
  with ScalaGenArrayOps with ScalaGenBooleanOps with ScalaGenPrimitiveOps with ScalaGenMiscOps with ScalaGenTupleOps
  with ScalaGenListOps with ScalaGenSeqOps with ScalaGenMathOps with ScalaGenCastingOps with ScalaGenSetOps with ScalaGenObjectOps
  with ScalaGenSynchronizedArrayBufferOps with ScalaGenHashMapOps with ScalaGenIterableOps with ScalaGenArrayBufferOps with ScalaGenExceptionOps
  { val IR: OptiLAScalaOpsPkgExp  }

trait OptiLACudaCodeGenPkg extends CudaGenDSLOps with CudaGenImplicitOps with CudaGenOrderingOps
  with CudaGenEqual with CudaGenIfThenElse with CudaGenVariables with CudaGenWhile with CudaGenFunctions
  with CudaGenStringOps with CudaGenRangeOps with CudaGenIOOps with CudaGenArrayOps with CudaGenBooleanOps
  with CudaGenPrimitiveOps with CudaGenMiscOps
  with CudaGenListOps with CudaGenSeqOps with CudaGenMathOps with CudaGenCastingOps with CudaGenSetOps with CudaGenObjectOps
  with CudaGenSynchronizedArrayBufferOps with CudaGenHashMapOps with CudaGenIterableOps with CudaGenArrayBufferOps
  { val IR: OptiLAScalaOpsPkgExp  }

trait OptiLAOpenCLCodeGenPkg extends OpenCLGenDSLOps with OpenCLGenImplicitOps with OpenCLGenOrderingOps
  with OpenCLGenEqual with OpenCLGenIfThenElse with OpenCLGenVariables with OpenCLGenWhile with OpenCLGenFunctions
  with OpenCLGenStringOps with OpenCLGenRangeOps with OpenCLGenIOOps with OpenCLGenArrayOps with OpenCLGenBooleanOps
  with OpenCLGenPrimitiveOps with OpenCLGenMiscOps with OpenCLGenTupleOps
  with OpenCLGenListOps with OpenCLGenSeqOps with OpenCLGenMathOps with OpenCLGenCastingOps with OpenCLGenSetOps with OpenCLGenObjectOps
  with OpenCLGenSynchronizedArrayBufferOps with OpenCLGenHashMapOps with OpenCLGenIterableOps with OpenCLGenArrayBufferOps
  { val IR: OptiLAScalaOpsPkgExp  }

trait OptiLACCodeGenPkg extends CGenDSLOps with CGenImplicitOps with CGenOrderingOps
  with CGenStringOps with CGenRangeOps with CGenIOOps with CGenArrayOps with CGenBooleanOps
  with CGenPrimitiveOps with CGenMiscOps with CGenFunctions with CGenEqual with CGenIfThenElse
  with CGenVariables with CGenWhile with CGenListOps with CGenSeqOps with CGenObjectOps
  with CGenSynchronizedArrayBufferOps with CGenHashMapOps with CGenIterableOps with CGenArrayBufferOps
  { val IR: OptiLAScalaOpsPkgExp  }

/**
 * This the trait that every OptiLA application must extend.
 */
trait OptiLA extends OptiLAScalaOpsPkg with DeliteCollectionOps with DeliteArrayOps
  with GenericDefs with LanguageOps with ArithOps with CloneableOps with HasMinMaxOps
  with VectorOps with DenseVectorOps with SparseVectorOps with RangeVectorOps with DenseVectorViewOps with SparseVectorViewOps //with MatrixRowOps with MatrixColOps
  with MatrixOps with MatrixBuildableOps with DenseMatrixOps with SparseMatrixOps with SparseMatrixBuildableOps
  with LAInputReaderOps with LAOutputWriterOps {

  this: OptiLAApplication =>
}

trait OptiLACompiler extends OptiLA with OptiLAUtilities 
  // -- ops only available to the compiler (they are restricted from application use)
  with DeliteArrayCompilerOps 
  with DenseVectorCompilerOps with SparseVectorCompilerOps with SparseVectorViewCompilerOps with DenseMatrixCompilerOps with SparseMatrixCompilerOps with SparseMatrixBuildableCompilerOps
  with MathOps with RangeOps with IOOps with SeqOps with SetOps with ListOps with HashMapOps with IterableOps with ArrayBufferOps with ExceptionOps 
  // --  kernel implementations
  with LanguageImplOpsStandard
  with VectorImplOpsStandard with DenseVectorImplOpsStandard with SparseVectorImplOpsStandard with DenseVectorViewImplOpsStandard with SparseVectorViewImplOps
  with MatrixImplOpsStandard with DenseMatrixImplOpsStandard with SparseMatrixImplOps with SparseMatrixBuildableImplOps
  with LAInputReaderImplOpsStandard with LAOutputWriterImplOpsStandard
  // -- designates the choice of sparse matrix formats for SparseMatrix and SparseMatrixBuildable respectively
  with SparseMatrixCSRCompilerOps with SparseMatrixCSRImplOps with SparseMatrixCOOCompilerOps with SparseMatrixCOOImplOps with SparseVectorViewCSRImplOps {
  
  this: OptiLAApplication with OptiLAExp =>
}


/**
 * These are the corresponding IR nodes for OptiLA.
 */
trait OptiLAExp extends OptiLACompiler with OptiLAScalaOpsPkgExp with DeliteOpsExp with DeliteArrayOpsExp with VariantsOpsExp 
  with LanguageOpsExp with ArithOpsExpOpt with CloneableOpsExp
  with VectorOpsExpOpt with DenseVectorOpsExpOpt with SparseVectorOpsExp with RangeVectorOpsExp with DenseVectorViewOpsExpOpt with SparseVectorViewOpsExpOpt //with MatrixRowOpsExpOpt with MatrixColOpsExpOpt
  with MatrixOpsExpOpt with DenseMatrixOpsExpOpt with SparseMatrixOpsExp with SparseMatrixBuildableOpsExp
  with LAInputReaderOpsExp with LAOutputWriterOpsExp
  with ExceptionOpsExp
  // -- choice of sparse matrix repr
  with SparseMatrixCSROpsExp with SparseMatrixCOOOpsExp with SparseVectorViewCSROpsExp
  with ExpressionsOpt with DeliteAllOverridesExp {

  // this: OptiLAApplicationRunner => why doesn't this work?
  this: DeliteApplication with OptiLAApplication with OptiLAExp => // can't be OptiLAApplication right now because code generators depend on stuff inside DeliteApplication (via IR)

  def getCodeGenPkg(t: Target{val IR: OptiLAExp.this.type}) : GenericFatCodegen{val IR: OptiLAExp.this.type} = {
    t match {
      case _:TargetScala => new OptiLACodeGenScala{val IR: OptiLAExp.this.type = OptiLAExp.this}
      case _:TargetCuda => new OptiLACodeGenCuda{val IR: OptiLAExp.this.type = OptiLAExp.this}
      case _:TargetOpenCL => new OptiLACodeGenOpenCL{val IR: OptiLAExp.this.type = OptiLAExp.this}
      case _:TargetC => new OptiLACodeGenC{val IR: OptiLAExp.this.type = OptiLAExp.this}
      case _ => err("optila does not support this target")
    }
  }  
}

trait OptiLAUtilities {  
  def err(s: String)(implicit ctx: SourceContext) = {
    println("[optila error]: " + s)
    println("  at " + (ctx.fileName.split("/").last + ":" + ctx.line).mkString("//").mkString(";"))
    exit(1)
  }
  def warn(s: String) = println("[optila warning]: " + s)  
}


/**
 * OptiLA code generators
 */
trait OptiLACodeGenBase extends GenericFatCodegen with SchedulingOpt {

  val IR: DeliteApplication with OptiLAExp
  override def initialDefs = IR.deliteGenerator.availableDefs


  def dsmap(line: String) = line

  val specialize = Set[String]()
  val specialize2 = Set[String]()
  def genSpec(f: File, outPath: String) = {}

  def getFiles(d: File): Array[File] = {
    d.listFiles flatMap { f => if (f.isDirectory()) getFiles(f) else Array(f) }
  }
  
  override def emitDataStructures(path: String) {
    val s = File.separator
    val dsRoot = Config.homeDir + s+"dsls"+s+"optila"+s+"src"+s+"ppl"+s+"dsl"+s+"optila"+s+"datastruct"+s + this.toString

    val dsDir = new File(dsRoot)
    if (!dsDir.exists) return
    val outDir = new File(path)
    outDir.mkdirs()

    val files = getFiles(dsDir)    
    for (f <- files) {
      if (f.isDirectory){
        emitDataStructures(f.getPath())
      }
      else {
        if (specialize contains (f.getName.substring(0, f.getName.indexOf(".")))) {
          genSpec(f, path)
        }
        val outFile = path + s + f.getName
        val out = new BufferedWriter(new FileWriter(outFile))
        for (line <- scala.io.Source.fromFile(f).getLines) {
          out.write(dsmap(line) + "\n")
        }
        out.close()
      }
    }
  }
}

trait OptiLACodeGenScala extends OptiLACodeGenBase with OptiLAScalaCodeGenPkg with OptiLAScalaGenExternal 
  with ScalaGenDeliteOps with ScalaGenDeliteCollectionOps with ScalaGenDeliteArrayOps
  with ScalaGenLanguageOps with ScalaGenArithOps with ScalaGenVectorOps with ScalaGenDenseVectorOps with ScalaGenSparseVectorOps
  with ScalaGenDenseVectorViewOps with ScalaGenSparseVectorViewOps with ScalaGenMatrixOps with ScalaGenDenseMatrixOps with ScalaGenSparseMatrixOps with ScalaGenSparseMatrixBuildableOps  
  //with ScalaGenMatrixRowOps with ScalaGenMatrixColOps
  with ScalaGenExceptionOps
  with ScalaGenVariantsOps
  // -- choice of sparse matrix repr
  with ScalaGenSparseMatrixCSROps with ScalaGenSparseMatrixCOOOps with ScalaGenSparseVectorViewCSROps
  with DeliteScalaGenAllOverrides { //with ScalaGenMLInputReaderOps {
  
  val IR: DeliteApplication with OptiLAExp

  override val specialize = Set("DenseVector", "DenseMatrix", "SparseVector", "SparseMatrixCSR", "SparseMatrixCOO", "DenseVectorView", "SparseVectorViewCSR")

  override def genSpec(f: File, dsOut: String) {
    for (s <- List("Double","Int","Float","Long","Boolean")) {
      val outFile = dsOut + s + f.getName
      val out = new BufferedWriter(new FileWriter(outFile))
      for (line <- scala.io.Source.fromFile(f).getLines) {
        out.write(specmap(line, s) + "\n")
      }
      out.close()
    }
  }

  def specmap(line: String, t: String) : String = {
    var res = line.replaceAll("object ", "object " + t)
    res = res.replaceAll("import ", "import " + t)
    res = res.replaceAll("@specialized T: Manifest", t)
    res = res.replaceAll("T:Manifest", t)
    res = res.replaceAll("\\bT\\b", t)
    parmap(res)
  }

  override def remap(s: String) = parmap(s)
  override def remap[A](m: Manifest[A]): String = {
    var res = super.remap(m)
    res = res.replaceAllLiterally("package$", "")
    parmap(res)
  }

  def parmap(line: String): String = {
    var res = line
    for(tpe1 <- List("Int","Long","Double","Float","Boolean")) {
      for (s <- specialize) {
        res = res.replaceAll(s+"\\["+tpe1+"\\]", tpe1+s)
      }
    }
    dsmap(res)
  }

  override def dsmap(line: String) : String = {
    var res = line.replaceAll("ppl.dsl.optila.datastruct", "generated")
    res = res.replaceAll("ppl.delite.framework.datastruct", "generated")
    res = res.replaceAll("ppl.dsl.optila", "generated.scala")    
    res
  }
}

trait OptiLACodeGenCuda extends OptiLACodeGenBase with OptiLACudaCodeGenPkg with OptiLACudaGenExternal with CudaGenDeliteOps
  with CudaGenArithOps with CudaGenVectorOps with CudaGenDenseVectorOps with CudaGenDenseVectorViewOps with CudaGenMatrixOps with CudaGenDenseMatrixOps with CudaGenDataStruct
  with CudaGenVariantsOps with CudaGenDeliteCollectionOps
  with DeliteCudaGenAllOverrides { //with CudaGenMLInputReaderOps  //TODO:DeliteCodeGenOverrideScala needed?
  val IR: DeliteApplication with OptiLAExp
  import IR._


  // Maps the scala type to cuda type
  override def remap[A](m: Manifest[A]) : String = {
    m.toString match {
      case "ppl.dsl.optila.DenseVector[Int]" => "DenseVector<int>"
      case "ppl.dsl.optila.DenseVector[Long]" => "DenseVector<long>"
      case "ppl.dsl.optila.DenseVector[Float]" => "DenseVector<float>"
      case "ppl.dsl.optila.DenseVector[Double]" => "DenseVector<double>"
      case "ppl.dsl.optila.DenseVector[Boolean]" => "DenseVector<bool>"
      case "ppl.dsl.optila.RangeVector" => "RangeVector"
      case "ppl.dsl.optila.DenseMatrix[Int]" => "DenseMatrix<int>"
      case "ppl.dsl.optila.DenseMatrix[Long]" => "DenseMatrix<long>"
      case "ppl.dsl.optila.DenseMatrix[Float]" => "DenseMatrix<float>"
      case "ppl.dsl.optila.DenseMatrix[Double]" => "DenseMatrix<double>"
      case "ppl.dsl.optila.DenseMatrix[Boolean]" => "DenseMatrix<bool>"
      case "ppl.dsl.optila.DenseVectorView[Int]" => "DenseVectorView<int>"
      case "ppl.dsl.optila.DenseVectorView[Long]" => "DenseVectorView<long>"
      case "ppl.dsl.optila.DenseVectorView[Float]" => "DenseVectorView<float>"
      case "ppl.dsl.optila.DenseVectorView[Double]" => "DenseVectorView<double>"
      case "ppl.dsl.optila.DenseVectorView[Boolean]" => "DenseVectorView<bool>"
      //case "ppl.dsl.optila.MatrixRow[Int]" => "DenseVectorView<int>"
      //case "ppl.dsl.optila.MatrixRow[Long]" => "DenseVectorView<long>"
      //case "ppl.dsl.optila.MatrixRow[Float]" => "DenseVectorView<float>"
      //case "ppl.dsl.optila.MatrixRow[Double]" => "DenseVectorView<double>"
      //case "ppl.dsl.optila.MatrixRow[Boolean]" => "DenseVectorView<bool>"
      case "Array[Int]" => "DeliteArray<int>"
      case "Array[Long]" => "DeliteArray<long>"
      case "Array[Float]" => "DeliteArray<float>"
      case "Array[Double]" => "DeliteArray<double>"
      case "Array[Boolean]" => "DeliteArray<bool>"
      case _ => super.remap(m)
    }
  }

  override def isObjectType[T](m: Manifest[T]) : Boolean = m.toString match {
    case "ppl.dsl.optila.DenseVector[Int]" => true
    case "ppl.dsl.optila.DenseVector[Long]" => true
    case "ppl.dsl.optila.DenseVector[Float]" => true
    case "ppl.dsl.optila.DenseVector[Double]" => true
    case "ppl.dsl.optila.DenseVector[Boolean]" => true
    case "ppl.dsl.optila.RangeVector" => true
    case "ppl.dsl.optila.DenseMatrix[Int]" => true
    case "ppl.dsl.optila.DenseMatrix[Long]" => true
    case "ppl.dsl.optila.DenseMatrix[Float]" => true
    case "ppl.dsl.optila.DenseMatrix[Double]" => true
    case "ppl.dsl.optila.DenseMatrix[Boolean]" => true
    case "Array[Double]" | "Array[Long]" | "Array[Float]" | "Array[Double]" | "Array[Boolean]" => true
    case _ => super.isObjectType(m)
  }

  override def copyInputHtoD(sym: Sym[Any]) : String = remap(sym.tp) match {
    case "DenseVector<int>" | "DenseVector<long>" | "DenseVector<float>" | "DenseVector<double>" | "DenseVector<bool>" => densevectorCopyInputHtoD(sym)
    case "RangeVector" => rangevectorCopyInputHtoD(sym)
    case "DenseMatrix<int>" | "DenseMatrix<long>" | "DenseMatrix<float>" | "DenseMatrix<double>" | "DenseMatrix<bool>" => densematrixCopyInputHtoD(sym)
    case "DeliteArray<int>" | "DeliteArray<long>" | "DeliteArray<float>" | "DeliteArray<double>" | "DeliteArray<bool>" => delitearrayCopyInputHtoD(sym)
    case _ => super.copyInputHtoD(sym)
  }

  override def copyOutputDtoH(sym: Sym[Any]) : String = remap(sym.tp) match {
    case "DenseVector<int>" | "DenseVector<long>" | "DenseVector<float>" | "DenseVector<double>" | "DenseVector<bool>" => densevectorCopyOutputDtoH(sym)
    case "RangeVector" => "assert(false); //RangeVector cannot be an output of a GPU kernel!\n"
    case "DenseMatrix<int>" | "DenseMatrix<long>" | "DenseMatrix<float>" | "DenseMatrix<double>" | "DenseMatrix<bool>" => densematrixCopyOutputDtoH(sym)
    case "DeliteArray<int>" | "DeliteArray<long>" | "DeliteArray<float>" | "DeliteArray<double>" | "DeliteArray<bool>" => delitearrayCopyOutputDtoH(sym)
    case _ => super.copyOutputDtoH(sym)
  }

  override def copyMutableInputDtoH(sym: Sym[Any]) : String = remap(sym.tp) match {
    case "DenseVector<int>" | "DenseVector<long>" | "DenseVector<float>" | "DenseVector<double>" | "DenseVector<bool>" => densevectorCopyMutableInputDtoH(sym)
    case "RangeVector" => "assert(false); //RangeVector cannot be mutated within a GPU kernel!\n"
    case "DenseMatrix<int>" | "DenseMatrix<long>" | "DenseMatrix<float>" | "DenseMatrix<double>" | "DenseMatrix<bool>" => densematrixCopyMutableInputDtoH(sym)
    case "DeliteArray<int>" | "DeliteArray<long>" | "DeliteArray<float>" | "DeliteArray<double>" | "DeliteArray<bool>" => delitearrayCopyMutableInputDtoH(sym)
    case _ => super.copyMutableInputDtoH(sym)
  }

  override def getDSLHeaders: String = {
    val out = new StringBuilder
    out.append("#include <float.h>\n")
    out.append("#include \"DenseVector.h\"\n")
    out.append("#include \"RangeVector.h\"\n")
    out.append("#include \"DeliteArray.h\"\n")
    out.append("#include \"DenseMatrix.h\"\n")
    out.append("#include \"library.h\"\n") // external library
    out.toString
  }

}

trait OptiLACodeGenOpenCL extends OptiLACodeGenBase with OptiLAOpenCLCodeGenPkg with OptiLAOpenCLGenExternal /*with OpenCLGenLanguageOps*/ with OpenCLGenArithOps with OpenCLGenDeliteOps with OpenCLGenVectorOps with OpenCLGenMatrixOps with OpenCLGenDenseMatrixOps with OpenCLGenDataStruct// with OpenCLGenDenseVectorViewOps
  /*with OpenCLGenVariantsOps*/ with DeliteOpenCLGenAllOverrides with OpenCLGenDeliteCollectionOps // with DeliteCodeGenOverrideOpenCL // with OpenCLGenMLInputReaderOps  //TODO:DeliteCodeGenOverrideScala needed?
{
  val IR: DeliteApplication with OptiLAExp
  import IR._

  override def isObjectType[T](m: Manifest[T]) : Boolean = m.toString match {
    case "ppl.dsl.optila.Matrix[Int]" => true
    case "ppl.dsl.optila.Matrix[Long]" => true
    case "ppl.dsl.optila.Matrix[Float]" => true
    case "ppl.dsl.optila.Matrix[Double]" => true
    case "ppl.dsl.optila.Matrix[Boolean]" => true
    case "ppl.dsl.optila.Vector[Int]" => true
    case "ppl.dsl.optila.Vector[Long]" => true
    case "ppl.dsl.optila.Vector[Float]" => true
    case "ppl.dsl.optila.Vector[Double]" => true
    case "ppl.dsl.optila.Vector[Boolean]" => true
    case "ppl.dsl.optila.RangeVector" => true
    case _ => super.isObjectType(m)
  }

  override def remap[A](m: Manifest[A]) : String = m.toString match {
    case "ppl.dsl.optila.Matrix[Int]" => "IntMatrix"
    case "ppl.dsl.optila.Matrix[Long]" => "LongMatrix"
    case "ppl.dsl.optila.Matrix[Float]" => "FloatMatrix"
    case "ppl.dsl.optila.Matrix[Double]" => "DoubleMatrix"
    case "ppl.dsl.optila.Matrix[Boolean]" => "BooleanMatrix"
    case "ppl.dsl.optila.Vector[Int]" => "IntVector"
    case "ppl.dsl.optila.Vector[Long]" => "LongVector"
    case "ppl.dsl.optila.Vector[Float]" => "FloatVector"
    case "ppl.dsl.optila.Vector[Double]" => "DoubleVector"
    case "ppl.dsl.optila.Vector[Boolean]" => "BooleanVector"
    case "ppl.dsl.optila.RangeVector" => "RangeVector"
    case _ => super.remap(m)
  }

  override def copyInputHtoD(sym: Sym[Any]) : String = remap(sym.tp) match {
    case "IntMatrix" | "LongMatrix" | "FloatMatrix" | "DoubleMatrix" | "BooleanMatrix" => matrixCopyInputHtoD(sym)
    case "IntVector" | "LongVector" | "FloatVector" | "DoubleVector" | "BooleanVector" => vectorCopyInputHtoD(sym)
    case "RangeVector" => rangeVectorCopyInputHtoD(sym)
    case _ => super.copyInputHtoD(sym)
  }

  override def copyOutputDtoH(sym: Sym[Any]) : String = remap(sym.tp) match {
    case "IntMatrix" | "LongMatrix" | "FloatMatrix" | "DoubleMatrix" | "BooleanMatrix" => matrixCopyOutputDtoH(sym)
    case "IntVector" | "LongVector" | "FloatVector" | "DoubleVector" | "BooleanVector" => vectorCopyOutputDtoH(sym)
    //TODO: Not going to implement below?
    //case "IntLabels" | "LongLabels" | "FloatLabels" | "DoubleLabels" | "BooleanLabels" => labelsCopyOutputDtoH(sym)
    //case "RangeVector" => rangeVectorCopyOutputDtoH(sym)
    //case "IndexVector" => indexVectorCopyOutputDtoH(sym)
    //case "FloatFloatTrainingSet" => trainingSetCopyOutputDtoH(sym)
    case _ => super.copyOutputDtoH(sym)
  }

  override def copyMutableInputDtoH(sym: Sym[Any]) : String = remap(sym.tp) match {
    case "IntMatrix" | "LongMatrix" | "FloatMatrix" | "DoubleMatrix" | "BooleanMatrix" => matrixCopyMutableInputDtoH(sym)
    case "IntVector" | "LongVector" | "FloatVector" | "DoubleVector" | "BooleanVector" => vectorCopyMutableInputDtoH(sym)
    case "RangeVector" => rangeVectorCopyMutableInputHtoD(sym)
    case _ => super.copyMutableInputDtoH(sym)
  }

  /*
  override def disAssembleObject[A](sym: Sym[Any]) : String = remap(sym.tp) match {
    case "IntMatrix" | "LongMatrix" | "FloatMatrix" | "DoubleMatrix" | "BooleanMatrix" =>
      "int %s_numRows, int %s_numCols, __global %s *%s_data".format(quote(sym),quote(sym),remap(sym.tp.typeArguments(0)),quote(sym))
    case "IntVector" | "LongVector" | "FloatVector" | "DoubleVector" | "BooleanVector" =>
      "char %s_isRow, int %s_length, __global %s *%s_data".format(quote(sym),quote(sym),remap(sym.tp.typeArguments(0)),quote(sym))
    case _ => super.disAssembleObject(sym)
  }

  override def reAssembleObject[A](sym: Sym[Any]) : String = remap(sym.tp) match {
    case "IntMatrix" | "LongMatrix" | "FloatMatrix" | "DoubleMatrix" | "BooleanMatrix" =>
      "\t%s %s; %s.numRows = %s_numRows; %s.numCols = %s_numCols; %s.data = %s_data".format(remap(sym.tp),quote(sym),quote(sym),quote(sym),quote(sym),quote(sym),quote(sym),quote(sym))
    case "IntVector" | "LongVector" | "FloatVector" | "DoubleVector" | "BooleanVector" =>
      "\t%s %s; %s.isRow = %s_isRow; %s.length = %s_length; %s.data = %s_data;".format(remap(sym.tp),quote(sym),quote(sym),quote(sym),quote(sym),quote(sym),quote(sym),quote(sym))
    case _ => super.disAssembleObject(sym)
  }
  */

  override def unpackObject[A](sym: Sym[Any]) : Map[String,Manifest[_]] = remap(sym.tp) match {
    case "IntMatrix" | "LongMatrix" | "FloatMatrix" | "DoubleMatrix" | "BooleanMatrix" =>
      val dataArrayType = sym.tp.typeArguments(0)
      Map("numRows"->Manifest.Int, "numCols"->Manifest.Int, "data"->dataArrayType.arrayManifest)
    case "IntVector" | "LongVector" | "FloatVector" | "DoubleVector" | "BooleanVector" =>
      val dataArrayType = sym.tp.typeArguments(0)
      Map("isRow"->Manifest.Boolean, "length"->Manifest.Int, "data"->dataArrayType.arrayManifest)
    case "RangeVector" =>
      Map("isRow"->Manifest.Boolean, "start"->Manifest.Int, "stride"->Manifest.Int, "end"->Manifest.Int)
    case _ => super.unpackObject(sym)
  }

  override def getDSLHeaders: String = {
    val out = new StringBuilder
    out.append("#include <float.h>\n")
    out.append("#include \"VectorImpl.h\"\n")
    out.append("#include \"DenseMatrix.h\"\n")
    out.append("#include \"RangeVectorImpl.h\"\n")
    out.append("#include \"library.h\"\n") // external library
    out.toString
  }
}

trait OptiLACodeGenC extends OptiLACodeGenBase with OptiLACCodeGenPkg with CGenDeliteOps 
  with CGenArithOps with CGenVectorOps with CGenDenseVectorOps with CGenMatrixOps with CGenDenseMatrixOps //with CGenMatrixRowOps
  with CGenVariantsOps with DeliteCGenAllOverrides
{
  val IR: DeliteApplication with OptiLAExp
  import IR._

  override def remap[A](m: Manifest[A]) : String = m.toString match {
    case "ppl.dsl.optila.Matrix[Int]" => "Matrix<int>"
    case "ppl.dsl.optila.Matrix[Long]" => "Matrix<long>"
    case "ppl.dsl.optila.Matrix[Float]" => "Matrix<float>"
    case "ppl.dsl.optila.Matrix[Double]" => "Matrix<double>"
    case "ppl.dsl.optila.Matrix[Boolean]" => "Matrix<bool>"
    case "ppl.dsl.optila.Vector[Int]" => "Vector<int>"
    case "ppl.dsl.optila.Vector[Long]" => "Vector<long>"
    case "ppl.dsl.optila.Vector[Float]" => "Vector<float>"
    case "ppl.dsl.optila.Vector[Double]" => "Vector<double>"
    case "ppl.dsl.optila.Vector[Boolean]" => "Vector<bool>"
    case _ => super.remap(m)
  }
}
