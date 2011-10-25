package ppl.dsl.optila.vector

import ppl.dsl.optila.{Vector, DenseVector, RangeVector}
import ppl.dsl.optila.{OptiLAExp, OptiLA}
import ppl.delite.framework.DeliteApplication
import ppl.delite.framework.datastruct.scala.DeliteCollection
import ppl.delite.framework.ops.{DeliteOpsExp, DeliteCollectionOpsExp}
import scala.virtualization.lms.common.{EffectExp, BaseExp, Base, ScalaGenBase}
import scala.virtualization.lms.util.OverloadHack
import java.io.PrintWriter

trait RangeVectorOps extends Base with OverloadHack { this: OptiLA =>

  implicit def repToRangeVecOps(x: Rep[RangeVector]) = new RangeVecOpsCls(x)
  implicit def varToRangeVecOps(x: Var[RangeVector]) = new RangeVecOpsCls(readVar(x))
  implicit def rangeToInterface(lhs: Rep[RangeVector]) = new VInterface(new RangeVecOpsCls(lhs))
  
  implicit def rangeVectorBuilder = new VectorBuilder[Int,RangeVector] {
    def alloc(length: Rep[Int], isRow: Rep[Boolean]) = (0::length)
    def toIntf(x: Rep[RangeVector]): Interface[Vector[Int]] = rangeToInterface(x)
  }  
  
  class RangeVecOpsCls(val elem: Rep[RangeVector]) extends VecOpsCls[Int] {
    // type VA = RangeVector
    // def toOps(x: Rep[RangeVector]) = repToRangeVecOps(x)
    // def toIntf(x: Rep[RangeVector]) = rangeToInterface(x)
    // def builder: VectorBuilder[Int,RangeVector] = rangeVectorBuilder
    def mA = manifest[Int]
    //def mVA = manifest[RangeVector]
    
    type V[X] = DenseVector[X]       
    type Self = RangeVector 
    def wrap(x: Rep[RangeVector]) = rangeToInterface(x)
    def toOps[B:Manifest](x: Rep[DenseVector[B]]) = repToDenseVecOps(x)
    def toIntf[B:Manifest](x: Rep[DenseVector[B]]): Interface[Vector[B]] = denseToInterface(x)
    def builder[B:Manifest]: VectorBuilder[B,V[B]] = denseVectorBuilder[B]    
    def mV[B:Manifest] = manifest[DenseVector[B]] 
          
    // VectorOps
    def length = rangevector_length(elem)
    def isRow = rangevector_isrow(elem)
    def apply(n: Rep[Int]) = rangevector_apply(elem,n)
    def sort(implicit o: Ordering[Int]) = elem.cloneL    
    
    // generic
    type VPLUSR = DenseVector[Int]
    val mVPLUSR = manifest[VPLUSR]
    val vplusBuilder = denseVectorBuilder[Int]
    def vplusToIntf(x: Rep[VPLUSR]) = denseToInterface(x)
    
    type VMINUSR = DenseVector[Int]
    val mVMINUSR = manifest[VMINUSR]
    val vminusBuilder = denseVectorBuilder[Int]
    def vminusToIntf(x: Rep[VMINUSR]) = denseToInterface(x)    
    
    type VTIMESR = DenseVector[Int]
    val mVTIMESR = manifest[VTIMESR]
    val vtimesBuilder = denseVectorBuilder[Int]
    def vtimesToIntf(x: Rep[VTIMESR]) = denseToInterface(x)        
        
    def t = throw new UnsupportedOperationException("RangeVectors cannot be transposed") // TODO    
    def mt() = throw new UnsupportedOperationException("RangeVectors cannot be updated")    
    def update(n: Rep[Int], y: Rep[Int]): Rep[Unit] = throw new UnsupportedOperationException("RangeVectors cannot be updated")
    def copyFrom(pos: Rep[Int], y: Rep[DenseVector[Int]]) = throw new UnsupportedOperationException("RangeVectors cannot be updated")
    def insert(pos: Rep[Int], y: Rep[Int]) = throw new UnsupportedOperationException("RangeVectors cannot be updated")
    def insertAll(pos: Rep[Int], y: Rep[DenseVector[Int]]) = throw new UnsupportedOperationException("RangeVectors cannot be updated")
    def removeAll(pos: Rep[Int], len: Rep[Int]) = throw new UnsupportedOperationException("RangeVectors cannot be updated")
    def trim() = throw new UnsupportedOperationException("RangeVectors cannot be updated")
    def clear() = throw new UnsupportedOperationException("RangeVectors cannot be updated")        
  } 
  
  def rangevector_length(x: Rep[RangeVector]): Rep[Int]
  def rangevector_isrow(x: Rep[RangeVector]): Rep[Boolean]
  def rangevector_apply(x: Rep[RangeVector], n: Rep[Int]): Rep[Int]
  // def rangevector_times_matrix(x: Rep[RangeVector], y: Rep[Matrix[Int]]): Rep[DenseVector[Int]]
  // def rangevector_flatmap[B:Manifest](x: Rep[RangeVector], f: Rep[A] => Rep[DenseVector[B]]): Rep[DenseVector[B]]
}

trait RangeVectorOpsExp extends RangeVectorOps with DeliteCollectionOpsExp { this: OptiLAExp =>
  
  def rangevector_length(x: Rep[RangeVector]) = x match {
    case Def(VectorObjectRange(start,end,stride,r)) => (end-start + stride - 1) / stride
    case Def(v@Reflect(VectorObjectRange(start,end,stride,r), u, es)) /*if context.contains(v)*/ => (end-start + stride - 1) / stride
  }
  
  def rangevector_isrow(x: Exp[RangeVector]) = x match {
    case Def(VectorObjectRange(s,e,d,r)) => r
    case Def(v@Reflect(VectorObjectRange(s,e,d,r), u, es)) /*if context.contains(v)*/ => r
  }
  
  def rangevector_apply(x: Exp[RangeVector], n: Exp[Int]) = rangevector_optimize_apply(x,n).get
  
  // and this one also helps in the example:
  def rangevector_optimize_apply[A:Manifest](x: Exp[DeliteCollection[A]], n: Exp[Int]): Option[Exp[A]] = x match {
    case Def(VectorObjectRange(s,e,d,r)) => Some((s + n*d).asInstanceOf[Exp[A]])
    case Def(v@Reflect(VectorObjectRange(s,e,d,r), u, es)) /*if context.contains(v)*/ => Some((s + n*d).asInstanceOf[Exp[A]])
    case _ => None
  }
  
  override def dc_apply[A:Manifest](x: Exp[DeliteCollection[A]], n: Exp[Int]) = {
    rangevector_optimize_apply(x,n) getOrElse super.dc_apply(x,n)
  }
  
  
}
  
