import api.TopoNaviService
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should

import java.io.{IOException, ObjectOutputStream}
import java.nio.file.{Files, Path, Paths}
import java.nio.file.attribute.PosixFilePermissions
import scala.jdk.CollectionConverters.*


class TesterCacheGenerator extends AnyFunSuite with should.Matchers {

  // Load all files from a directory into a Map[String, String], mirroring
  // TopoController.loadExampleFiles() used by CompilationCacheService
  private def loadFiles(dir: Path): java.util.Map[String, String] = {
    val files = scala.collection.mutable.Map[String, String]()
    Files.walk(dir)
      .filter(Files.isRegularFile(_))
      .forEach { file =>
        val relativePath = dir.relativize(file).toString.replace('\\', '/')
        files(relativePath) = Files.readString(file)
      }
    files.asJava
  }

  test("Compile swfc and save result to ~/.toponavi/tester_swfc") {
    val possiblePaths = List(
      Paths.get("../examples/swfc"),
      Paths.get("examples/swfc"),
      Paths.get("H:/Academic/UNNC/FoSE/Y4/FYP/indoor-topo-navi/examples/swfc")
    )

    val examplesDir = possiblePaths.find(p => Files.isDirectory(p))
      .getOrElse(fail(s"examples/swfc not found. Checked: ${possiblePaths.map(_.toAbsolutePath).mkString(", ")}"))

    println(s"Loading files from: ${examplesDir.toAbsolutePath}")

    // Mirror CompilationCacheService: compile from Map[String, String]
    val files = loadFiles(examplesDir)
    val result = TopoNaviService.compile(files)
    println(s"Compilation successful! Graphs: ${result.graphs.size}")

    // Save to ~/.toponavi/tester_swfc — same ObjectOutputStream pattern as CompilationCacheService.save
    val saveDir  = Paths.get(System.getProperty("user.home"), ".toponavi")
    val savePath = saveDir.resolve("tester_swfc")

    Files.createDirectories(saveDir)
    savePath.toFile.setWritable(true) // unlock if re-running

    try {
      val oos = new ObjectOutputStream(Files.newOutputStream(savePath))
      try     { oos.writeObject(result); println(s"Cache SAVED: ${savePath.toAbsolutePath}") }
      finally { oos.close() }
    } catch {
      case e: IOException =>
        System.err.println(s"Cache write failed: ${e.getMessage}")
        throw new RuntimeException("Failed to cache compilation result", e)
    }

    // Set read-only to prevent casual deletion/overwrite
    val posixSupported = try { Files.getPosixFilePermissions(savePath); true }
                         catch { case _: UnsupportedOperationException => false }
    if (posixSupported)
      Files.setPosixFilePermissions(savePath, PosixFilePermissions.fromString("r--r--r--"))
    else
      savePath.toFile.setReadOnly()

    println(s"Permissions set: ${savePath.toAbsolutePath} is now read-only")
    assert(savePath.toFile.exists())
    assert(!savePath.toFile.canWrite)
  }

  test("Compile trent and save result to ~/.toponavi/tester_trent") {
    val possiblePaths = List(
      Paths.get("../examples/trent"),
      Paths.get("examples/trent"),
      Paths.get("H:/Academic/UNNC/FoSE/Y4/FYP/indoor-topo-navi/examples/trent")
    )

    val examplesDir = possiblePaths.find(p => Files.isDirectory(p))
      .getOrElse(fail(s"examples/trent not found. Checked: ${possiblePaths.map(_.toAbsolutePath).mkString(", ")}"))

    println(s"Loading files from: ${examplesDir.toAbsolutePath}")

    // Mirror CompilationCacheService: compile from Map[String, String]
    val files = loadFiles(examplesDir)
    val result = TopoNaviService.compile(files)
    println(s"Compilation successful! Graphs: ${result.graphs.size}")

    // Save to ~/.toponavi/tester_trent — same ObjectOutputStream pattern as CompilationCacheService.save
    val saveDir = Paths.get(System.getProperty("user.home"), ".toponavi")
    val savePath = saveDir.resolve("tester_trent")

    Files.createDirectories(saveDir)
    savePath.toFile.setWritable(true) // unlock if re-running

    try {
      val oos = new ObjectOutputStream(Files.newOutputStream(savePath))
      try {
        oos.writeObject(result); println(s"Cache SAVED: ${savePath.toAbsolutePath}")
      }
      finally {
        oos.close()
      }
    } catch {
      case e: IOException =>
        System.err.println(s"Cache write failed: ${e.getMessage}")
        throw new RuntimeException("Failed to cache compilation result", e)
    }

    // Set read-only to prevent casual deletion/overwrite
    val posixSupported = try {
      Files.getPosixFilePermissions(savePath); true
    }
    catch {
      case _: UnsupportedOperationException => false
    }
    if (posixSupported)
      Files.setPosixFilePermissions(savePath, PosixFilePermissions.fromString("r--r--r--"))
    else
      savePath.toFile.setReadOnly()

    println(s"Permissions set: ${savePath.toAbsolutePath} is now read-only")
    assert(savePath.toFile.exists())
    assert(!savePath.toFile.canWrite)
  }
}
