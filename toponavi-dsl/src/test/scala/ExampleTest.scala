import corelang.Environment
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import surfacelang.TopoEnvironment
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError
import pprint.pprintln
import syntax.TopoMapVisitor

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

    val subMapCode2 =
      """
      topo-map TestSubMap2(){
          topo-node tt1
      }
      """

    // 'station' ID 'at' expr ('at' expr)* recordAssign (requirements ('on' expr)?)?
    val vehicleCode =
      """
      transport OP1 is Elevator{
          station Lobby at TestSubMap::tt1 {location = 0.0}
          station UpperLobby at TestSubMap2::tt1 {location = 5.0}
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
          new TopoMapVisitor().visitSurfaceDefRootExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type")
      }
    }

    pprintln(rootProgram)
    
    val rootElaborated = rootProgram.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty, Map.empty))
    pprintln(rootElaborated)

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
          new TopoMapVisitor().visitSurfaceDefTopoMapExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type")
      }
    }

    val submapElaborated = submapProgram.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty, Map.empty))

    val submap2Program = catchError(subMapCode2.strip) { listener =>
      val stripedCode = subMapCode2.strip()
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefTopoMapExprContext =>
          new TopoMapVisitor().visitSurfaceDefTopoMapExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type")
      }
    }
    val submap2Elaborated = submap2Program.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty, Map.empty))


    val vehicleProgram = catchError(vehicleCode.strip) { listener =>
      val stripedCode = vehicleCode.strip()
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefTransportExprContext =>
          new TopoMapVisitor().visitSurfaceDefTransportExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type")
      }
    }

    val vehicleElaborated = vehicleProgram.elaborate(
      using TopoEnvironment(
        Environment.empty,
        Map.empty,
        Map.empty,
        Map(
          submapElaborated.name -> submapElaborated,
          submap2Elaborated.name -> submap2Elaborated
        )
      )
    )

    println("=== Submap Expr ===")
    pprint.pprintln(submapProgram)
    println("=== Submap Elaborated ===")
    pprint.pprintln(submapElaborated)

    println("=== Vehicle Expr===")
    pprint.pprintln(vehicleProgram)

    println("=== Vehicle Elaborated===")
    pprint.pprintln(vehicleElaborated)
  }
}