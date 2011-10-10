package epfl.mdarrays.staged

import java.io._
import scala.virtualization.lms.common._
import scala.virtualization.lms.internal.{GenericFatCodegen, GenericCodegen}
import ppl.delite.framework.codegen.{Utils, Target}
import ppl.delite.framework.codegen.scala.TargetScala
import ppl.delite.framework.codegen.delite.overrides.{DeliteCudaGenAllOverrides, DeliteCGenAllOverrides, DeliteScalaGenAllOverrides, DeliteAllOverridesExp}
import ppl.delite.framework.ops._
import ppl.delite.framework.codegen.delite.DeliteCodeGenPkg
import ppl.delite.framework.{DeliteApplication, Config}

// ex. object GDARunner extends OptiMLApplicationRunner with GDA
trait StagedSACApplicationRunner extends StagedSACApplication with DeliteApplication with StagedSACExp

// ex. trait GDA extends OptiMLApplication
trait StagedSACApplication extends StagedSAC {
  var args: Rep[Array[String]]
  def main(): Unit
}

trait StagedSAC extends MDArrayBase with MiscOps {
	this: StagedSACApplication =>
}

trait StagedSACExp extends StagedSAC with MDArrayBaseExp with MiscOpsExp
 	with DeliteOpsExp with DeliteAllOverridesExp {
	
	this: DeliteApplication with StagedSACApplication with StagedSACExp => 

  // we want a single typer for both the sequential and parallel code
  lazy val typer = new MDArrayTypingBubbleUp { val IR: StagedSACExp.this.type = StagedSACExp.this }

  // generators created by getCodeGenPkg will use the 'current' scope of the deliteGenerator as global scope
  override val deliteGenerator = new DeliteCodeGenPkg { val IR : StagedSACExp.this.type = StagedSACExp.this;
                                                        override def performTyping[A: Manifest, B: Manifest](x: Exp[A], y: Exp[B]): Unit = typer.doTyping(y, false)
                                                        val generators = StagedSACExp.this.generators }

  def getCodeGenPkg(t: Target{val IR: StagedSACExp.this.type}) : GenericFatCodegen{val IR: StagedSACExp.this.type} = {
    t match {
      case _:TargetScala => new StagedSACCodegenScala {
															val IR: StagedSACExp.this.type = StagedSACExp.this
															val TY = typer
														}
      case _ => throw new RuntimeException("staged-sac does not support this target")
    }
  }	
}

trait StagedSACCodegenBase extends GenericFatCodegen with Utils {
  val IR: DeliteApplication with StagedSACExp

  override def initialDefs = IR.deliteGenerator.availableDefs

  override def emitDataStructures(path: String) {
    val s = File.separator
    val dsRoot = Config.homeDir + s+"dsls"+s+"sac-staged"+s+"src"+s+"epfl"+s+"mdarrays"+s+"datastruct"+s + this.toString
    copyDataStructures(dsRoot, path, dsmap)
  }

  def dsmap(s: String) = s.replaceAll("epfl.mdarrays", "generated.scala")
}

trait StagedSACCodegenScala extends StagedSACCodegenBase with ScalaGenMDArray
  with ScalaGenDeliteOps with ScalaGenDeliteCollectionOps with DeliteScalaGenAllOverrides with ScalaGenMiscOps {
	
  val IR: TY.IR.type with DeliteApplication with StagedSACExp
	
  override def remap[A](m: Manifest[A]): String = {
    dsmap(super.remap(m))
  }
}

