import corelang.Environment
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import surfacelang.TopoEnvironment
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError
import pprint.pprintln

class ExampleTest extends AnyFunSuite with should.Matchers{
  test("example test"){
    // TODO: LetStmt now has to be with another braces-pair, try to remove this limit
    val rootCode =
      """
      root TestBuilding(p1: String){
        {
          let a: Int = 1;
          let b: String = "Tester";
        }

        def magicFunc(): Int = {
          a + 114514
        }
      }
      """
      
    val subMapCode =
      """
      topo-map TestSubMap(){
          {
              let a: Int = 114;
          }
          topo-node tt1 {number = a}
          topo-node tt2
          atomic-path [tt1 <-> tt2] {cost = a+5}
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

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefRootExprContext =>
          new syntax.TopoMapVisitor().visitSurfaceDefRootExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type")
      }
    }

    println(rootProgram)

    val submapProgram = catchError(subMapCode.strip) { listener =>
      val stripedCode = subMapCode.strip()
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefTopoMapExprContext =>
          new syntax.TopoMapVisitor().visitSurfaceDefTopoMapExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type")
      }
    }

    val submapElaborated = submapProgram.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty))

    println("=== Submap Expr ===")
    pprint.pprintln(submapProgram)
    println("=== Submap Elaborated ===")
    pprint.pprintln(submapElaborated)

  }
}