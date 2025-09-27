import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError

class ExampleTest extends AnyFunSuite with should.Matchers{
  test("example test"){
    val code =
      """
      TopoMap Test{
          =342RF
      }
      """

    val program = catchError(code.strip) { listener =>
      val stripedCode = code.strip()
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)
      MapFileVisitor().visitProgram(parser.program())
    }

    println(program)
  }
}