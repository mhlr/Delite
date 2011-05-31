package ppl.apps.profile

import ppl.dsl.optiml.{OptiMLApplication, OptiMLApplicationRunner}

/**
 * Author: Bo Wang
 * Date: May 30, 2011
 * Time: 7:38:36 PM
 *
 * Pervasive Parallelism Laboratory (PPL)
 * Stanford University
 */

object ProfileTest5Runner extends OptiMLApplicationRunner with ProfileTest5

trait ProfileTest5 extends OptiMLApplication {

  def print_usage = {
    println("Usage: ProfileTest5 <m> <n> <num_run>")
    exit(-1)
  }

  def main() = {

    if (args.length < 3) print_usage

    val m = Integer.parseInt(args(0))
    val n = Integer.parseInt(args(1))
    val num_run = Integer.parseInt(args(2))

    val a1 = Matrix.ones(m,n)
    val b1 = Matrix.ones(n,m)
    val a2 = Matrix.ones(m/2,n)
    val b2 = Matrix.ones(n,m)
    val a3 = Matrix.ones(m/2,n)
    val b3 = Matrix.ones(n,m/2)
    val a4 = Matrix.ones(m/2,n/2)
    val b4 = Matrix.ones(n/2,m)
    val a5 = Matrix.ones(m/2,n/2)
    val b5 = Matrix.ones(n/2,m/2)

    val v1 = Vector.ones(m*n)
    val v2 = Vector.ones(m*n/2)
    val v3 = Vector[Double](m,true)
    val v4 = Vector[Double](n,true)

    var i = 0
    while(i < num_run){
      val c1 = a1 * b1
      println(c1(i,i))
      val c2 = a2 * b2
      println(c2(i,i))
      val c3 = a3 * b3
      println(c3(i,i))
      val c4 = a4 * b4
      println(c4(i,i))
      val c5 = a5 * b5
      println(c5(i,i))
      val v5 = v1 map (_*5.0)
      println(v5(i))
      val v6 = v2 map (_+5.0)
      println(v6(i))
      val m7 = v3 ** v4
      println(m7(i,i))
      i += 1
    }

    profilePrintAll()
  }
}


