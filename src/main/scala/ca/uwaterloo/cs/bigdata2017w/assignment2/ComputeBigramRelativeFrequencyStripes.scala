/**
  * Bespin: reference implementations of "big data" algorithms
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package ca.uwaterloo.cs.bigdata2017w.assignment2

import io.bespin.scala.util.Tokenizer

import org.apache.log4j._
import collection.mutable.HashMap
import scala.collection.JavaConverters._
import org.apache.hadoop.fs._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.rogach.scallop._
import org.apache.spark.Partitioner
import org.apache.spark.HashPartitioner
import org.apache.spark.util.{CollectionsUtils, Utils}


class Conf1(args: Seq[String]) extends ScallopConf(args) with Tokenizer {
  mainOptions = Seq(input, output, reducers)
  val input = opt[String](descr = "input path", required = true)
  val output = opt[String](descr = "output path", required = true)
  val reducers = opt[Int](descr = "number of reducers", required = false, default = Some(1))
  
  verify()
}

// class MyPartitioner2(partitions: Int) extends Partitioner {
//   require(partitions >= 0)
//   def numPartitions: Int = partitions
//   def getPartition(key: Any): Int = key match {
//     case null => 0
//     case (key1,key2) => (key1.hashCode & Integer.MAX_VALUE) % numPartitions
//   }
// }


object ComputeBigramRelativeFrequencyStripes extends Tokenizer {
  val log = Logger.getLogger(getClass().getName())

  def main(argv: Array[String]) {
    val args = new Conf1(argv)

    log.info("Input: " + args.input())
    log.info("Output: " + args.output())
    log.info("Number of reducers: " + args.reducers())

    val conf = new SparkConf().setAppName("Compute Bigram Relative Frequency Stripes")
    val sc = new SparkContext(conf)

    val outputDir = new Path(args.output())
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)

    val textFile = sc.textFile(args.input())
    var marginal = 0.0
    var sum = 0.0

    val counts = textFile
      .flatMap(line => {
        val tokens = tokenize(line)
        // println (tokens)
        if (tokens.length > 1) {
        	val pairs = tokens.sliding(2).map(p => {(p.head, Map(p.tail -> 1.0))}) 
        	// val single = tokens.init.map(p => (p, "*"))
          // println (pairs)
        	pairs
        }
        else {
         	List()
     	}
      })
      .reduceByKey((key, value) => {
        // println (key)
        // println (value)
        val temp = value.map{
          case (key1, value1) => key1 -> (key.getOrElse(key1, 0.0) + value1) //getorelse?
        }
        key ++ temp
        })
      .sortByKey()
      // .repartitionAndSortWithinPartitions(new MyPartitioner2(args.reducers()))
      .map( bigram => {
        sum = 0
      	val total = bigram._2.map {
          case (key,value) => {
            sum = sum + value
          }
        }
        val term = bigram._2.map {
          case (key,value) => {
            // key -> (bigram._2.get(key).get / sum)
            key.head + "=" + (bigram._2.get(key).get / sum)
          }
        }
        (bigram._1 + " {" + term.mkString(", ") + "}")
        // (bigram._1,term)      	
      })
      // .map( bigram => {
      //   var temp = ""
      //   val format = bigram._2.map {
      //     case (key,value) => {
      //       temp = temp + key.head + "=" + value + ", "
      //     }
      //   }
      //   (bigram._1 + " {" + temp.dropRight(2) + "}")
      //   })
      .saveAsTextFile(args.output())
  }
}