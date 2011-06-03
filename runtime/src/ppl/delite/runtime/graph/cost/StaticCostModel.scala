package ppl.delite.runtime.cost

import ppl.delite.runtime.graph.ops._
import ppl.delite.runtime.Config
import scala.collection.mutable.ListBuffer

trait AbstractCostModel {	
	def shouldParallelize(op: OP_While, sizeDict: Map[String, Int]): Boolean
	def shouldParallelize(op: OP_MultiLoop, sizeDict: Map[String, Int]): Boolean
}

trait ParallelUtilizationCostModel extends AbstractCostModel {
	// heuristics
	val whileThreshold = Config.whileCostThreshold
	val multiloopThreshold = Config.loopCostThreshold
	val unknownLoopSize = 0 // could be large, could be median of known sizes, ...
	
	def getParallelUtilization(ops: Set[DeliteOP], sizeDict: Map[String, Int]): (Int, Int, Int) = {
		var totalOps: Int = 0 
		var parallelOps: Int = 0
		var parallelSize: Int = 0 
		
		for (n <- ops) {
			n match {
				case s: OP_Single => totalOps += 1
				case l: OP_MultiLoop => totalOps += 1
				 												parallelOps += 1
																parallelSize += getLoopSize(l, sizeDict)
				case _ => 
			}
		}
		(totalOps, parallelOps, parallelSize)
	}
	
	def getLoopSize(o: OP_MultiLoop, sizeDict: Map[String, Int]): Int = {
		if (o.sizeIsConst) { o.size.toInt }
		else (sizeDict getOrElse (o.size, unknownLoopSize))
	}
	
	def shouldParallelize(w: OP_While, sizeDict: Map[String, Int]) = {
		val (totalOps, parallelOps, parallelSize) = getParallelUtilization(w.bodyGraph.ops, sizeDict)
		(parallelSize/totalOps) > whileThreshold
	}
	
	def shouldParallelize(l: OP_MultiLoop, sizeDict: Map[String, Int]) = {
		getLoopSize(l, sizeDict) > multiloopThreshold
	}
}