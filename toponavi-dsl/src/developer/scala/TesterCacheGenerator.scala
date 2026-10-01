import api.TopoNaviService

import java.io.ObjectOutputStream
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.util.Using

object TesterCacheGenerator {
  private case class Options(
    project: Option[Path] = None,
    output: Option[Path] = None,
    params: Map[String, AnyRef] = Map.empty
  )

  private val usage = """
    |Usage: generateTesterCache --args='--project DIRECTORY [--output FILE] [--param NAME=VALUE ...]'
    |
    |Compiles a project with explicit Bool/Int parameters for the manual route testers.
    |The default output is ~/.toponavi/tester_<directory-name>.
    |Parameter values must be true, false, or an integer. No access parameters are defaulted.
    |Existing cache files are replaced only after successful compilation and serialization.
    |""".stripMargin.trim

  private def parameter(assignment: String): (String, AnyRef) = {
    assignment.split("=", 2).toList match {
      case name :: raw :: Nil if name.nonEmpty =>
        val value: AnyRef = raw match {
          case "true" => java.lang.Boolean.TRUE
          case "false" => java.lang.Boolean.FALSE
          case _ => raw.toLongOption.map(java.lang.Long.valueOf).getOrElse {
            throw new IllegalArgumentException(s"Parameter '$name' must be a Bool or Int, got '$raw'")
          }
        }
        name -> value
      case _ => throw new IllegalArgumentException(s"Expected NAME=VALUE, got '$assignment'")
    }
  }

  @tailrec
  private def parse(args: List[String], options: Options = Options()): Options = args match {
    case Nil => options
    case "--project" :: directory :: rest if options.project.isEmpty =>
      parse(rest, options.copy(project = Some(Paths.get(directory))))
    case "--output" :: file :: rest if options.output.isEmpty =>
      parse(rest, options.copy(output = Some(Paths.get(file))))
    case "--param" :: assignment :: rest =>
      val (name, value) = parameter(assignment)
      require(!options.params.contains(name), s"Repeated parameter '$name'")
      parse(rest, options.copy(params = options.params.updated(name, value)))
    case _ => throw new IllegalArgumentException(s"Invalid or repeated option: ${args.head}\n$usage")
  }

  private def loadFiles(directory: Path): java.util.Map[String, String] =
    Using.resource(Files.walk(directory)) { paths =>
      paths.iterator().asScala.filter(Files.isRegularFile(_)).map { file =>
        directory.relativize(file).toString.replace('\\', '/') -> Files.readString(file)
      }.toMap.asJava
    }

  def main(args: Array[String]): Unit = {
    if (args.toList == List("--help")) println(usage)
    else {
      val options = parse(args.toList)
      val project = options.project.getOrElse {
        throw new IllegalArgumentException(s"Missing --project\n$usage")
      }.toAbsolutePath.normalize()
      require(Files.isDirectory(project), s"Project directory not found: $project")
      val output = options.output.getOrElse {
        Paths.get(System.getProperty("user.home"), ".toponavi", s"tester_${project.getFileName}")
      }.toAbsolutePath.normalize()

      println(s"Compiling project: $project")
      val result = TopoNaviService.compile(loadFiles(project), options.params.asJava)
      Files.createDirectories(output.getParent)
      val temporary = Files.createTempFile(output.getParent, ".tester-cache-", ".tmp")
      try {
        Using.resource(new ObjectOutputStream(Files.newOutputStream(temporary)))(_.writeObject(result))
        // Older generators made caches read-only; allow replacing those files too.
        if (Files.exists(output)) output.toFile.setWritable(true)
        Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING)
      } finally {
        Files.deleteIfExists(temporary)
      }
      println(s"Cache saved: $output (${result.graphs.size} graphs)")
    }
  }
}
