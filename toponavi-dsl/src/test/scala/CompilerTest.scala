import api.TopoNaviService
import compiler.TopoScriptCompiler
import corelang.Value
import data.NavigationOutputPath
import enums.RoutePlanningPreferences.MinimizeTime
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should

import java.io.{File, PrintWriter}
import java.nio.file.Files
import pprint.pprintln

class CompilerTest extends AnyFunSuite with should.Matchers {

//  test("Compiler should parse and elaborate example project") {
//    // 1. Create a temporary directory for the test project
//    val tempDir = Files.createTempDirectory("toponavi_test_project").toFile
//    tempDir.deleteOnExit()
//
//    try {
//      // 2. Create configuration.tcfg
//      val configFile = new File(tempDir, "configuration.tcfg")
//      val configCode =
//        """
//          |building-includes{
//          |    submap Floor1
//          |    submap Floor2
//          |    submap Floor3 using Zone1
//          |    submap Floor4 using Zone1
//          |    vehicle Elevator1
//          |    vehicle OPS
//          |}
//          |""".stripMargin
//      writeToFile(configFile, configCode)
//
//      // 3. Create map file (Floor1.tmap)
//      val mapFile = new File(tempDir, "Floor1.tmap")
//      val mapCode =
//        """
//          |topo-map Floor1() {
//          |    let params: {permResident: Int} = {permResident = 2}
//          |    topo-node node1
//          |    topo-node node2
//          |    atomic-path [node1 <-> node2] { cost = 10.0 }
//          |}
//          |""".stripMargin
//      writeToFile(mapFile, mapCode)
//
//      val mapFile2 = new File(tempDir, "Floor2.tmap")
//      val mapCode2 =
//        """
//          |topo-map Floor2() {
//          |    let params: {permResident: Int} = {permResident = 10}
//          |    topo-node node1
//          |    topo-node node2
//          |    atomic-path [node1 <-> node2] { cost = 7.0 }
//          |}
//          |""".stripMargin
//      writeToFile(mapFile2, mapCode2)
//
//      val mapFile3 = new File(tempDir, "Zone1.tmap")
//      val mapCode3 =
//        """
//          |topo-map Zone1() {
//          |    let params: {permResident: Int} = {permResident = 10}
//          |    topo-node hall
//          |    topo-node room
//          |    atomic-path [hall <-> room] { cost = 7.0 }
//          |}
//          |""".stripMargin
//      writeToFile(mapFile3, mapCode3)
//
//      // 4. Create transport file (Elevator1.ttr)
//      val transFile = new File(tempDir, "Elevator1.ttr")
//      val vehicleCode =
//        """
//          |transport Elevator1 is Elevator {
//          |    let params: {velocity: Float, accl: Float, carAmount: Int, duty: Int} = {velocity = 1.0, accl = 0.8, carAmount = 2, duty = 1350}
//          |    station s1 at Floor1::node1 { location = 0.0, departureRate = 0.7 }
//          |    station s2 at Floor2::node1 { location = 5.0, departureRate = 0.1 }
//          |    station s3 at Floor3::hall { location = 10.0, departureRate = 0.1 }
//          |    station s4 at Floor4::hall { location = 15.0, departureRate = 0.1 }
//          |}
//          |""".stripMargin
//      writeToFile(transFile, vehicleCode)
//
//      // 5. Run the compiler
//      val compiler = new TopoScriptCompiler()
//
//      // We expect this to run without throwing exceptions
//      val result = compiler.compile(tempDir.getAbsolutePath)
//
//      // Since the current implementation returns a placeholder, we just check it's not null
//      // In the future, we would assert on result.graphs and result.transportGraph
//      assert(result != null)
//      println("Compilation result: ")
//      pprintln(result)
//      pprintln(result.graphs("Floor3").nodes)
//      println("Compilation successful!")
//
//    } finally {
//      // Cleanup happens automatically via deleteOnExit usually, but good to be explicit for recursive delete if needed
//      // For simple files, standard deleteOnExit on dir might fail if not empty in Java < ?
//      // But this is just a test. using IO
//      tempDir.listFiles().foreach(_.delete())
//      tempDir.delete()
//    }
//  }
//
//  private def writeToFile(file: File, content: String): Unit = {
//    val writer = new PrintWriter(file)
//    try {
//      writer.write(content)
//    } finally {
//      writer.close()
//    }
//  }

//  test("Compiler should parse and elaborate real high-rise project"){
//    // Try to locate the examples directory relative to the module or project root
//    val possiblePaths = List(
//      new File("../examples/swfc"),
//      new File("examples/swfc"),
//      new File("H:/Academic/UNNC/FoSE/Y4/FYP/indoor-topo-navi/examples/swfc")
//    )
//
//    val examplesDir = possiblePaths.find(_.exists()).getOrElse(new File("../examples/swfc"))
//
//    if (examplesDir.exists() && examplesDir.isDirectory) {
//      println(s"Compiling real project at: ${examplesDir.getAbsolutePath}")
//
//      val compiler = new TopoScriptCompiler()
//      val result = compiler.compile(examplesDir.getAbsolutePath, Map(
//        "haveStaffCard" -> Value.BoolVal(true),
//        "haveManagementCard" -> Value.BoolVal(true),
//        "haveRoomKey" -> Value.BoolVal(true),
//        "id" -> Value.IntVal(1919810),
//        "aggregatedWeight" -> Value.IntVal(10))
//      )
//
//      val plan: NavigationOutputPath = TopoNaviService.findRoutePlan(result, "UpperLobby::SS3_4_hall", "UpperLobby::retail_conn1_in", MinimizeTime);
//
//      assert(result != null)
//      println("Real project compilation result: ")
//      pprintln(result)
//      println("Real project compilation successful!")
//
//      val stairCases = result.transportGraph.nodes
//        .map(_.ownerLine)
//        .distinct
//        .collect { case sc: data.StairCase => sc }
//
//      if (stairCases.isEmpty) {
//        fail("No StairCase found in the transport graph. Check that a Stairs transport is defined and compiled correctly.")
//      } else {
//        println("StairCase objects found in transport graph:")
//        stairCases.foreach(pprint.pprintln(_))
//      }
//
//      pprint.pprintln(plan)
//    } else {
//      println(s"Skipping real project test: 'examples' directory not found. Checked: ${possiblePaths.map(_.getAbsolutePath).mkString(", ")}")
//    }
//  }


  test("Compiler should parse and elaborate real low-rise project") {
    // Try to locate the examples directory relative to the module or project root
    val possiblePaths = List(
      new File("../examples/trent"),
      new File("examples/trent"),
      new File("H:/Academic/UNNC/FoSE/Y4/FYP/indoor-topo-navi/examples/trent")
    )

    val examplesDir = possiblePaths.find(_.exists()).getOrElse(new File("../examples/trent"))

    if (examplesDir.exists() && examplesDir.isDirectory) {
      println(s"Compiling real project at: ${examplesDir.getAbsolutePath}")

      val compiler = new TopoScriptCompiler()
      val result = compiler.compile(examplesDir.getAbsolutePath, Map(
        "haveStaffCard" -> Value.BoolVal(true),
        "timeOfDay" -> Value.IntVal(12000) // 10:00
      ))

//      val plan: NavigationOutputPath = TopoNaviService.findRoutePlan(result, "Floor1::arabica_out_T", "Floor3::riverview_podium", MinimizeTime);
      
      
      assert(result != null)
//      println("Real project compilation result: ")
//      pprintln(result)
//      println("Real project compilation successful!")

      val stairCases = result.transportGraph.nodes
        .map(_.ownerLine)
        .distinct
        .collect { case sc: data.StairCase => sc }

      if (stairCases.isEmpty) {
        fail("No StairCase found in the transport graph. Check that a Stairs transport is defined and compiled correctly.")
      } else {
//        println("StairCase objects found in transport graph:")
//        stairCases.foreach(pprint.pprintln(_))
      }

//      pprint.pprintln(plan.routeEdges)
      // List all Floor1 nodes with their estimated coords
      result.graphs.get("Floor1") match {
        case Some(floor1Graph) =>
          println(s"\n=== Floor1 estimated coords (${floor1Graph.nodes.size} nodes) ===")
          val (assigned, unassigned) = floor1Graph.nodes.partition(_.estimatedCoord.isDefined)
          assigned
            .sortBy(n => (n.estimatedCoord.get.x, n.estimatedCoord.get.y))
            .foreach { n =>
              val c = n.estimatedCoord.get
              println(f"  ${n.identifier}%-40s -> (${c.x}%4d, ${c.y}%4d)")
            }
          if (unassigned.nonEmpty) {
            println(s"  --- ${unassigned.size} node(s) with no estimated coord ---")
            unassigned.foreach(n => println(s"  ${n.identifier}"))
          }
        case None =>
          println("Floor1 graph not found in compilation result")
      }
    } else {
      println(s"Skipping real project test: 'examples' directory not found. Checked: ${possiblePaths.map(_.getAbsolutePath).mkString(", ")}")
    }
  }
}
