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

package ca.uwaterloo.cs.bigdata2017w.assignment5

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
import org.apache.spark.sql.SparkSession


class Conf2(args: Seq[String]) extends ScallopConf(args) {
  mainOptions = Seq(input, date)
  val input = opt[String](descr = "input path", required = true)
  val date = opt[String](descr = "date", required = true)
  // val text = opt[Boolean](descr = "text format", required = false, default = Some(true))
  verify()
}

object Q2 {
  val log = Logger.getLogger(getClass().getName())

  def main(argv: Array[String]) {
    val args = new Conf2(argv)

    log.info("Input: " + args.input())
    log.info("Date: " + args.date())
    // log.info("Text format: " + args.text())

    val conf = new SparkConf().setAppName("Q2")
    val sc = new SparkContext(conf)

    var fileName = ""
    val date = args.date()
    // if (args.text()) {
      fileName = "/lineitem.tbl"
    // } else {
      // fileName = "/lineitem/part-r-00000-06ffba52-de7d-4aa9-a540-0b8fa4a96d6e.snappy.parquet"
    // }

    val lineItemFile = sc.textFile(args.input() + fileName)
      .filter(line => {
          line.split("\\|")(10).contains(date)
        })
      .map(line => {
        (line.split("\\|")(0), 0)
        })
    // if (args.text()) {
      fileName = "/orders.tbl"
    // } else {
      // fileName = "/lineitem/part-r-00000-06ffba52-de7d-4aa9-a540-0b8fa4a96d6e.snappy.parquet"
    // }

    val orderFile = sc.textFile(args.input() + fileName)
      .map(line => {
        val temp = line.split("\\|")
        (temp(0), temp(6))
        })

    val output = orderFile.cogroup(lineItemFile)
      .filter(line => {
          !line._2._2.isEmpty
        })
      .sortByKey(true)
      .take(20)
    output
      .foreach(line => {
        println("(" + line._2._1.iterator.next() + "," + line._1 + ")")
      })
  }
}

