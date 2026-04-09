import corelang.{Environment, Identifier, Value}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import surfacelang.TopoEnvironment
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError
import pprint.pprintln
import syntax.TopoMapVisitor

class ExampleTest extends AnyFunSuite with should.Matchers{

  // Build a TopoEnvironment with a set of named params of any Value type already bound.
  // Usage: envWithParams("flag" -> Value.BoolVal(true), "name" -> Value.StringVal("x"), "n" -> Value.IntVal(3))
  def envWithParams(bindings: (String, Value)*): TopoEnvironment = {
    TopoEnvironment(
      env = bindings.foldLeft(Environment.empty[Identifier, corelang.Type, Value]) { (acc, kv) =>
        acc.addValueVar(Identifier.Symbol(kv._1), kv._2)
      },
      nodes = Map.empty,
      paths = Map.empty,
      submaps = Map.empty
    )
  }

  test("example test"){
    val rootCode =
      """
      root TestBuilding(p1: String, p2: Int, p3: Bool){
        def a: Int = 1

        def magicFunc(): Int = {
            a + 114514
        }

        constraint TestConstraint {
            require p1 == "satisfy"
        }

        constraint TestConstraint2 {
            require p2 == 42
        }

      }
      """
      
    val subMapCode = // Unbound symbol: a
      """
      topo-map TestSubMap(){
          // let a: Int = 114
          let b: Int = a + 400

//          def magicFunc(): Int = {
//            a + 114514
//          }

          topo-node tt1 {number = a}
          topo-node tt2
          topo-node tt3
          atomic-path [tt1 -> tt2] {cost = a+5} requires TestConstraint
          atomic-path [tt2 -> tt1] {cost = 10} requires TestConstraint2
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
    
    val rootElaborated = rootProgram.elaborate(using envWithParams(
      "p1" -> Value.StringVal("not-satisfy"),
      "p2" -> Value.IntVal(42),
      "p3" -> Value.BoolVal(true)
    ))
    pprintln(rootElaborated)

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

    val submapElaborated = submapProgram.elaborate(using TopoEnvironment(rootElaborated.context, Map.empty, Map.empty, Map.empty))

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


/*
RootValue(
  name = "TestBuilding",
  params = List(("p1", StringType)),
  context = Environment(
    types = Map(),
    values = Map(
      Symbol(name = "a") -> IntVal(n = 1L),
      Symbol(name = "magicFunc") -> IntVal(n = 114515L)
    )
  )
)
*/