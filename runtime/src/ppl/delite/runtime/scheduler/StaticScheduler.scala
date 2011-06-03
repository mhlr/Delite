package ppl.delite.runtime.scheduler

import ppl.delite.runtime.graph.DeliteTaskGraph
import ppl.delite.runtime.graph.ops._
import java.util.ArrayDeque
import ppl.delite.runtime.cost._

/**
 * Author: Kevin J. Brown
 * Date: Dec 3, 2010
 * Time: 11:49:23 PM
 * 
 * Pervasive Parallelism Laboratory (PPL)
 * Stanford University
 */

/**
 * The base class of all static / walk-time schedulers
 * Defines the public interface for the rest of the Delite Runtime
 */

trait StaticScheduler extends ParallelUtilizationCostModel {
  def schedule(graph: DeliteTaskGraph)

  protected def scheduleFlat(graph: DeliteTaskGraph)

  protected def scheduleOne(op: DeliteOP, graph: DeliteTaskGraph, schedule: PartialSchedule)

	protected def scheduleSequential(graph: DeliteTaskGraph) 	

  protected def enqueueRoots(graph: DeliteTaskGraph, opQueue: ArrayDeque[DeliteOP]) {
    for (op <- graph.ops) {
      if (!op.isSchedulable) {//if not already in opQueue (protects against same consumer appearing in list multiple times)
        op.processSchedulable
        if (op.isSchedulable) opQueue.add(op)
      }
    }
  }

  protected def split(op: DeliteOP, graph: DeliteTaskGraph, schedule: PartialSchedule, resourceList: Seq[Int]) {
    val helper = OpHelper.expand(op, resourceList.length, graph)
    helper.isSchedulable = true
    scheduleOne(helper, graph, schedule)

    for (i <- resourceList) {
      val chunk = OpHelper.split(op, i, resourceList.length, graph.kernelPath)
      schedule(i).add(chunk)
      chunk.scheduledResource = i
      chunk.isSchedulable = true
      chunk.isScheduled = true
    }
  }

	protected def addSequential(op: DeliteOP, graph: DeliteTaskGraph, schedule: PartialSchedule, resource: Int) {
		op match {
			case c: OP_Condition => {
				scheduleSequential(c.predicateGraph)
				scheduleSequential(c.thenGraph)
				scheduleSequential(c.elseGraph)				
				splitNotEmpty(c, graph, schedule, List(c.predicateGraph.schedule, c.thenGraph.schedule, c.elseGraph.schedule), Seq(0))			
			}
			case w: OP_While => {
				scheduleSequential(w.predicateGraph)
				scheduleSequential(w.bodyGraph)
				splitNotEmpty(w, graph, schedule, List(w.predicateGraph.schedule, w.bodyGraph.schedule), Seq(0))			
			}
			case op if op.isDataParallel => split(op, graph, schedule, Seq(0))
			case op => scheduleOn(op, graph, schedule, resource)			
		}		
	}
	
  protected def addNested(op: OP_Nested, graph: DeliteTaskGraph, schedule: PartialSchedule, resourceList: Seq[Int]) {
    op match {
      case c: OP_Condition => {
        scheduleFlat(c.predicateGraph)
        scheduleFlat(c.thenGraph)
        scheduleFlat(c.elseGraph)
        splitNotEmpty(c, graph, schedule, List(c.predicateGraph.schedule, c.thenGraph.schedule, c.elseGraph.schedule), resourceList)
      }
      case w: OP_While => {
				if (shouldParallelize(w, Map[String,Int]())){
					println("scheduling while loop in parallel")
					scheduleFlat(w.predicateGraph)	        
        	scheduleFlat(w.bodyGraph)
				}
				else {					
					println("scheduling while loop sequentially")
					scheduleSequential(w.predicateGraph)
					scheduleSequential(w.bodyGraph)
			  }        
				splitNotEmpty(w, graph, schedule, List(w.predicateGraph.schedule, w.bodyGraph.schedule), resourceList)			
      }
      case v: OP_Variant => {
        scheduleFlat(v.variantGraph)
        splitNotEmpty(v, graph, schedule, List(v.variantGraph.schedule), resourceList)
      }
      case err => error("Control OP type not recognized: " + err.getClass.getSimpleName)
    }
  }

	protected def scheduleOn(op: DeliteOP, graph: DeliteTaskGraph, schedule: PartialSchedule, resource: Int) {	
		schedule(resource).add(op)
	  op.scheduledResource = resource
	  op.isScheduled = true
	}

  protected def splitNotEmpty(op: OP_Nested, graph: DeliteTaskGraph, outerSchedule: PartialSchedule, innerSchedules: List[PartialSchedule], resourceList: Seq[Int]) {
    val filteredList = resourceList.filter(i => innerSchedules.map(_(i).isEmpty) contains false)
    val chunkList = if (filteredList.isEmpty) Seq(resourceList(0)) else filteredList
    val chunks = op.makeChunks(chunkList, graph)

    val chunksIter = chunks.iterator
    for (i <- chunkList) {
      val chunk = chunksIter.next
      outerSchedule(i).add(chunk)
      chunk.scheduledResource = i
      chunk.isSchedulable = true
      chunk.isScheduled = true
    }
  }

  protected def ensureScheduled(graph: DeliteTaskGraph) {
    for (op <- graph.ops) {
      if (!op.isScheduled)
        error("Graph dependencies are unsatisfiable")
    }
  }

}
