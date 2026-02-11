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
    val rootCode =
      """
      root TestBuilding(p1: String){
        let a: Int = 1
//        {
//          let a: Int = 1;
//        }

        def magicFunc(): Int = {
            a + 114514
        }

      }
      """
      
    val subMapCode = // Unbound a
      """
      topo-map TestSubMap(){
          let a: Int = 114
          let b: Int = a + 400

//          def magicFunc(): Int = {
//            a + 114514
//          }

          topo-node tt1 {number = a}
          topo-node tt2
          atomic-path [tt1 <-> tt2] {cost = a+5}
          let params: {area: Int} = {area = 114514}
      }
      """

    val subMapCode2 =
      """
      topo-map TestSubMap2(){
          topo-node tt1
          let params: {area: Int} = {area = 114514}
      }
      """

    // 'station' ID 'at' expr ('at' expr)* recordAssign (requirements ('on' expr)?)?
    val vehicleCode =
      """
      transport OP1 is Elevator{
          let bb: String = "aaa"
          let params: {velocity: Float, accl: Float} = {velocity = 2.5, accl = 0.8}
          station Lobby at TestSubMap::tt1 {location = 0.0}
          station UpperLobby at TestSubMap2::tt1 {location = 5.0}
      }
      """

    val stripedCode = rootCode.strip()

    val rootProgram = catchError(stripedCode) { listener =>
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
    
    // TODO: Shitty one!
//    val rootElaborated = rootProgram.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty, Map.empty))
//    pprintln(rootElaborated)

    val stripedCode1 = subMapCode.strip()

    val submapProgram = catchError(stripedCode1) { listener =>
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode1))
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

    val stripedCode2 = subMapCode2.strip()
    val submap2Program = catchError(stripedCode2) { listener =>
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode2))
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

    val stripedCode3 = vehicleCode.strip()
    val vehicleProgram = catchError(stripedCode3) { listener =>
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode3))
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