package ppl.dsl.deliszt

import ppl.delite.framework.DeliteApplication
import ppl.delite.framework.analysis.TraversalAnalysis

import analysis.DeLisztCodeGenAnalysis

/**
 * author: Michael Wu (mikemwu@stanford.edu)
 * last modified: 04/12/2011
 *
 * Pervasive Parallelism Laboratory (PPL)
 * Stanford University
 */

 /**
 * These separate DeLiszt applications from the Exp world.
 */

// ex. object GDARunner extends DeLisztApplicationRunner with GDA
trait DeLisztApplicationRunner extends DeLisztApplication with DeliteApplication with DeLisztExp
 
trait DeLisztApplication extends DeliteApplication with DeLiszt with DeLisztLift with DeLisztLibrary {
  override lazy val analyses = List(new DeLisztCodeGenAnalysis{val IR: DeLizstApplication.this.type = DeLizstApplication.this})

  override def liftedMain(x: Rep[Array[String]]) = {
    _init(x)
    this.args = x
    val y = main()
    this.args = null
    unit(y)
  }
}

trait DeLisztLibrary {
  this: DeLisztApplication =>
}