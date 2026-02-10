import compiler.TopoScriptCompiler
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import java.io.{File, PrintWriter}
import java.nio.file.Files

class CompilerTest extends AnyFunSuite with should.Matchers {

  test("Compiler should parse and elaborate example project") {
    // 1. Create a temporary directory for the test project
    val tempDir = Files.createTempDirectory("toponavi_test_project").toFile
    tempDir.deleteOnExit()

    try {
      // 2. Create configuration.tcfg
      val configFile = new File(tempDir, "configuration.tcfg")
      val configCode =
        """
          |building-includes{
          |    submap Floor1
          |    vehicle Elevator1
          |}
          |""".stripMargin
      writeToFile(configFile, configCode)

      // 3. Create map file (Floor1.tmap)
      val mapFile = new File(tempDir, "Floor1.tmap")
      val mapCode =
        """
          |topo-map Floor1() {
          |    topo-node node1 { x = 0.0, y = 0.0 }
          |    topo-node node2 { x = 10.0, y = 0.0 }
          |    atomic-path [node1 <-> node2] { cost = 10.0 }
          |}
          |""".stripMargin
      writeToFile(mapFile, mapCode)

      // 4. Create transport file (Elevator1.ttr)
      val transFile = new File(tempDir, "Elevator1.ttr")
      val vehicleCode =
        """
          |transport Elevator1 is Elevator {
          |    {let params = {velocity = 2.5, accl = 0.8};}
          |    station s1 at Floor1::node1 { location = 0.0 }
          |}
          |""".stripMargin
      writeToFile(transFile, vehicleCode)

      // 5. Run the compiler
      val compiler = new TopoScriptCompiler()

      // We expect this to run without throwing exceptions
      val result = compiler.compile(tempDir.getAbsolutePath)

      // Since the current implementation returns a placeholder, we just check it's not null
      // In the future, we would assert on result.graphs and result.transportGraph
      assert(result != null)
      println("Compilation successful!")

    } finally {
      // Cleanup happens automatically via deleteOnExit usually, but good to be explicit for recursive delete if needed
      // For simple files, standard deleteOnExit on dir might fail if not empty in Java < ?
      // But this is just a test. using IO
      tempDir.listFiles().foreach(_.delete())
      tempDir.delete()
    }
  }

  private def writeToFile(file: File, content: String): Unit = {
    val writer = new PrintWriter(file)
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }
}
