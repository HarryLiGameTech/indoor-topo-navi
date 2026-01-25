import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError

class ExampleTest extends AnyFunSuite with should.Matchers{
  test("example test"){
    val rootCode =
      """
      root TestBuilding{
          def a = 1
      }
      """
      
    val subMapCode =
      """
      topo-map TestSubMap{
          topo-node tt1
          topo-node tt2
          atomic-path [tt1 <-> tt2] {cose = 114}
      }
      """

    val rootProgram = catchError(rootCode.strip) { listener =>
      val stripedCode = rootCode.strip()
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)
      syntax.TopoMapVisitor().visitSurfaceDefRootExpr(parser.rootExpr())
    }

    println(rootProgram)
  }
}