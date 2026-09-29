import api.TopoNaviService
import enums.VisitingMode
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.{Callable, CyclicBarrier, Executors, TimeUnit}
import scala.jdk.CollectionConverters.*

class CompilationIsolationTest extends AnyFunSuite with Matchers {
  private def project(index: Int, withParams: Boolean): Map[String, String] = {
    val cost = if (withParams) "value" else index.toString
    Map(
      "configuration" -> s"building-includes { submap Floor$index using Base$index }",
      s"Base$index" -> s"""topo-map Base$index() {
        |  let params = {permResident = $index}
        |  topo-node A
        |  topo-node B
        |  atomic-path [A -> B] {cost = $cost}
        |}""".stripMargin
    ) ++ Option.when(withParams)("root" -> "root Building(value: Int) {}")
  }

  test("concurrent compilation, validation, and navigation requests keep their project state isolated") {
    val workers = 12
    val executor = Executors.newFixedThreadPool(workers)
    val barrier = new CyclicBarrier(workers)
    try {
      val requests = (0 until workers).map { worker =>
        executor.submit(new Callable[Unit] {
          override def call(): Unit = {
            for (round <- 0 until 8) {
              val index = round * workers + worker + 1
              val withParams = worker % 2 == 0
              val files = project(index, withParams).asJava
              val params = Map[String, AnyRef]("value" -> java.lang.Integer.valueOf(index)).asJava
              barrier.await(10, TimeUnit.SECONDS)
              if (worker % 6 < 2) {
                val result = if (withParams) TopoNaviService.compile(files, params)
                  else TopoNaviService.compile(files)
                result.graphs.keySet shouldBe Set(s"Base$index", s"Floor$index")
                result.graphSequence shouldBe List(s"Floor$index")
                result.graphs.values.foreach { graph =>
                  graph.adjacencyList.head.costs(VisitingMode.Normal) shouldBe index.toDouble
                }
              } else if (worker % 6 < 4) {
                val result = if (withParams) TopoNaviService.validateCode(files, params)
                  else TopoNaviService.validateCode(files)
                result shouldBe "Compilation Successful"
              } else {
                val result = if (withParams) {
                  TopoNaviService.findPath(files, params, s"Floor$index::A", s"Floor$index::B", "MinimizeTime")
                } else {
                  TopoNaviService.findPath(files, s"Floor$index::A", s"Floor$index::B", "MinimizeTime")
                }
                result should include(s"$index" + "s (~")
                result should include(s"Floor$index")
              }
            }
          }
        })
      }
      requests.foreach(_.get(30, TimeUnit.SECONDS))
    } finally {
      executor.shutdownNow()
      executor.awaitTermination(10, TimeUnit.SECONDS)
    }
  }

  test("a failed validation does not affect later compilation") {
    val invalid = project(1, withParams = false).updated(
      "Base1", "topo-map Base1() {\nlet params = {}\ntopo-node A\ntopo-node A\n}"
    )
    intercept[RuntimeException] { TopoNaviService.validateCode(invalid.asJava) }
    val result = TopoNaviService.compile(project(2, withParams = false).asJava)
    result.graphs.keySet shouldBe Set("Base2", "Floor2")
    result.graphs("Floor2").adjacencyList.head.costs(VisitingMode.Normal) shouldBe 2.0
  }
}
