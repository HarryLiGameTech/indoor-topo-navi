import corelang.{Environment, Expr, Identifier, Value}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import surfacelang.{AtomicPathExpr, ConstraintExpr, StationDef, SubTopoMapExpr, TopoEnvironment, TopoNodeRef, TransportExpr}
import syntax.TopoMapVisitor
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError

class ConstraintTest extends AnyFunSuite with Matchers {

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  def parseTopoMap(code: String): SubTopoMapExpr =
    catchError(code.strip()) { listener =>
      val lexer = new MapFileLexer(CharStreams.fromString(code.strip()))
      lexer.removeErrorListeners(); lexer.addErrorListener(listener)
      val parser = new MapFileParser(new CommonTokenStream(lexer))
      parser.removeErrorListeners(); parser.addErrorListener(listener)
      parser.surfaceDef() match {
        case ctx: MapFileParser.SurfaceDefTopoMapExprContext =>
          new TopoMapVisitor().visitSurfaceDefTopoMapExpr(ctx)
        case _ => throw new RuntimeException("Expected topo-map")
      }
    }

  def parseTransport(code: String): TransportExpr =
    catchError(code.strip()) { listener =>
      val lexer = new MapFileLexer(CharStreams.fromString(code.strip()))
      lexer.removeErrorListeners(); lexer.addErrorListener(listener)
      val parser = new MapFileParser(new CommonTokenStream(lexer))
      parser.removeErrorListeners(); parser.addErrorListener(listener)
      parser.surfaceDef() match {
        case ctx: MapFileParser.SurfaceDefTransportExprContext =>
          new TopoMapVisitor().visitSurfaceDefTransportExpr(ctx)
        case _ => throw new RuntimeException("Expected transport")
      }
    }

  // Build a TopoEnvironment with a set of named params of any Value type already bound.
  def envWithParams(bindings: (String, Value)*): TopoEnvironment =
    TopoEnvironment(
      env = bindings.foldLeft(Environment.empty[Identifier, corelang.Type, Value]) { (acc, kv) =>
        acc.addValueVar(Identifier.Symbol(kv._1), kv._2)
      },
      nodes = Map.empty,
      paths = Map.empty,
      submaps = Map.empty
    )

  // ---------------------------------------------------------------------------
  // 1. Parsing — constraint body is parsed into ConstraintExpr with correct predicates
  // ---------------------------------------------------------------------------

  test("parsing: single-predicate constraint body produces ConstraintExpr with one predicate") {
    val code =
      """
      topo-map M() {
        constraint TenantsOnly {
          require haveStaffCard
        }
        topo-node A
      }
      """
    val expr = parseTopoMap(code)
    expr.constraints should have size 1
    val c = expr.constraints.head
    c.name shouldBe "TenantsOnly"
    c.predicates should have size 1
    c.predicates.head shouldBe Expr.Var("haveStaffCard")
  }

  test("parsing: multi-predicate constraint body produces ConstraintExpr with all predicates") {
    val code =
      """
      topo-map M() {
        constraint StrictAccess {
          require haveStaffCard
          require haveManagementCard
        }
        topo-node A
      }
      """
    val expr = parseTopoMap(code)
    val c = expr.constraints.head
    c.name shouldBe "StrictAccess"
    c.predicates should have size 2
    c.predicates shouldBe List(Expr.Var("haveStaffCard"), Expr.Var("haveManagementCard"))
  }

  test("parsing: requires clause on atomic-path produces Expr.Var references") {
    val code =
      """
      topo-map M() {
        constraint TenantsOnly { require haveStaffCard }
        topo-node A
        topo-node B
        atomic-path [A -> B] {} requires TenantsOnly
      }
      """
    val expr = parseTopoMap(code)
    val path = expr.paths.head
    path.constraints shouldBe List(Expr.Var("TenantsOnly"))
  }

  test("parsing: requires <A && B> on atomic-path produces two Expr.Var references") {
    val code =
      """
      topo-map M() {
        constraint C1 { require p1 }
        constraint C2 { require p2 }
        topo-node A
        topo-node B
        atomic-path [A -> B] {} requires <C1 && C2>
      }
      """
    val expr = parseTopoMap(code)
    val path = expr.paths.head
    path.constraints shouldBe List(Expr.Var("C1"), Expr.Var("C2"))
  }

  // ---------------------------------------------------------------------------
  // 2. ConstraintExpr.elaborate — returns BoolVal reflecting predicate evaluation
  // ---------------------------------------------------------------------------

  test("ConstraintExpr.elaborate: all predicates true → BoolVal(true)") {
    val c = ConstraintExpr("TenantsOnly", List(Expr.Var("haveStaffCard")))
    val topoEnv = envWithParams("haveStaffCard" -> Value.BoolVal(true))
    c.elaborate(using topoEnv) shouldBe Value.BoolVal(true)
  }

  test("ConstraintExpr.elaborate: any predicate false → BoolVal(false)") {
    val c = ConstraintExpr("TenantsOnly", List(Expr.Var("haveStaffCard")))
    val topoEnv = envWithParams("haveStaffCard" -> Value.BoolVal(false))
    c.elaborate(using topoEnv) shouldBe Value.BoolVal(false)
  }

  test("ConstraintExpr.elaborate: multi-predicate, all true → BoolVal(true)") {
    val c = ConstraintExpr("StrictAccess", List(
      Expr.Var("haveStaffCard"),
      Expr.Var("haveManagementCard")
    ))
    val topoEnv = envWithParams("haveStaffCard" -> Value.BoolVal(true), "haveManagementCard" -> Value.BoolVal(true))
    c.elaborate(using topoEnv) shouldBe Value.BoolVal(true)
  }

  test("ConstraintExpr.elaborate: multi-predicate, one false → BoolVal(false)") {
    val c = ConstraintExpr("StrictAccess", List(
      Expr.Var("haveStaffCard"),
      Expr.Var("haveManagementCard")
    ))
    val topoEnv = envWithParams("haveStaffCard" -> Value.BoolVal(true), "haveManagementCard" -> Value.BoolVal(false))
    c.elaborate(using topoEnv) shouldBe Value.BoolVal(false)
  }

  test("ConstraintExpr.elaborate: expression predicate (comparison) that holds → BoolVal(true)") {
    val c = ConstraintExpr("WeightLimit", List(
      Expr.BinOp(corelang.OpKind.Lt, Expr.Var("weight"), Expr.IntLit(200))
    ))
    val topoEnv = TopoEnvironment(
      env = Environment.empty[Identifier, corelang.Type, Value]
        .addValueVar(Identifier.Symbol("weight"), Value.IntVal(150)),
      nodes = Map.empty, paths = Map.empty, submaps = Map.empty
    )
    c.elaborate(using topoEnv) shouldBe Value.BoolVal(true)
  }

  test("ConstraintExpr.elaborate: expression predicate (comparison) that fails → BoolVal(false)") {
    val c = ConstraintExpr("WeightLimit", List(
      Expr.BinOp(corelang.OpKind.Lt, Expr.Var("weight"), Expr.IntLit(200))
    ))
    val topoEnv = TopoEnvironment(
      env = Environment.empty[Identifier, corelang.Type, Value]
        .addValueVar(Identifier.Symbol("weight"), Value.IntVal(250)),
      nodes = Map.empty, paths = Map.empty, submaps = Map.empty
    )
    c.elaborate(using topoEnv) shouldBe Value.BoolVal(false)
  }

  // ---------------------------------------------------------------------------
  // 3. atomic-path filtering in SubTopoMapExpr.elaborate
  // ---------------------------------------------------------------------------

  test("atomic-path with passing constraint is included in the elaborated map") {
    val code =
      """
      topo-map M() {
        constraint TenantsOnly {
          require haveStaffCard
        }
        topo-node A
        topo-node B
        atomic-path [A -> B] {} requires TenantsOnly
      }
      """
    val expr  = parseTopoMap(code)
    val result = expr.elaborate(using envWithParams("haveStaffCard" -> Value.BoolVal(true)))
    result.paths should have size 1
  }

  test("atomic-path with failing constraint is excluded from the elaborated map") {
    val code =
      """
      topo-map M() {
        constraint TenantsOnly {
          require haveStaffCard
        }
        topo-node A
        topo-node B
        atomic-path [A -> B] {} requires TenantsOnly
      }
      """
    val expr  = parseTopoMap(code)
    val result = expr.elaborate(using envWithParams("haveStaffCard" -> Value.BoolVal(false)))
    result.paths shouldBe empty
  }

  test("only paths whose constraints pass are kept; others are excluded") {
    val code =
      """
      topo-map M() {
        constraint TenantsOnly {
          require haveStaffCard
        }
        topo-node A
        topo-node B
        topo-node C
        atomic-path [A -> B] {} requires TenantsOnly
        atomic-path [A <-> C] {}
      }
      """
    val expr = parseTopoMap(code)

    // haveStaffCard = false: restricted path dropped, unrestricted path kept (bidirectional → 2 edges)
    val resultFail = expr.elaborate(using envWithParams("haveStaffCard" -> Value.BoolVal(false)))
    resultFail.paths should have size 1
    resultFail.paths.head.from.name shouldBe "A"
    resultFail.paths.head.to.name   shouldBe "C"

    // haveStaffCard = true: both paths kept
    val resultPass = expr.elaborate(using envWithParams("haveStaffCard" -> Value.BoolVal(true)))
    resultPass.paths should have size 2
  }

  test("path with requires <A && B> is excluded when any referenced constraint fails") {
    val code =
      """
      topo-map M() {
        constraint C1 { require p1 }
        constraint C2 { require p2 }
        topo-node A
        topo-node B
        atomic-path [A -> B] {} requires <C1 && C2>
      }
      """
    val expr = parseTopoMap(code)

    // Both pass
    expr.elaborate(using envWithParams("p1" -> Value.BoolVal(true), "p2" -> Value.BoolVal(true))).paths should have size 1
    // C1 fails
    expr.elaborate(using envWithParams("p1" -> Value.BoolVal(false), "p2" -> Value.BoolVal(true))).paths shouldBe empty
    // C2 fails
    expr.elaborate(using envWithParams("p1" -> Value.BoolVal(true), "p2" -> Value.BoolVal(false))).paths shouldBe empty
    // Both fail
    expr.elaborate(using envWithParams("p1" -> Value.BoolVal(false), "p2" -> Value.BoolVal(false))).paths shouldBe empty
  }

  // ---------------------------------------------------------------------------
  // 4. Station filtering in TransportExpr.elaborate
  // ---------------------------------------------------------------------------

  test("station with passing constraint is included in the elaborated transport") {
    val submapCode =
      """
      topo-map TestMap() {
        topo-node hall
        let params: {area: Int} = {area = 0}
      }
      """
    val transportCode =
      """
      transport OP1 is Elevator {
        let params: {velocity: Float, accl: Float} = {velocity = 2.5, accl = 0.8}
        constraint StaffOnly { require haveStaffCard }
        station Floor1 at TestMap::hall {relativeLocation = 0.0} requires StaffOnly
      }
      """
    val submap   = parseTopoMap(submapCode).elaborate(using envWithParams())
    val transport = parseTransport(transportCode)
    val topoEnv  = envWithParams("haveStaffCard" -> Value.BoolVal(true)).copy(submaps = Map("TestMap" -> submap))
    transport.elaborate(using topoEnv).stations should have size 1
  }

  test("station with failing constraint is excluded from the elaborated transport") {
    val submapCode =
      """
      topo-map TestMap() {
        topo-node hall
        let params: {area: Int} = {area = 0}
      }
      """
    val transportCode =
      """
      transport OP1 is Elevator {
        let params: {velocity: Float, accl: Float} = {velocity = 2.5, accl = 0.8}
        constraint StaffOnly { require haveStaffCard }
        station Floor1 at TestMap::hall {relativeLocation = 0.0} requires StaffOnly
      }
      """
    val submap   = parseTopoMap(submapCode).elaborate(using envWithParams())
    val transport = parseTransport(transportCode)
    val topoEnv  = envWithParams("haveStaffCard" -> Value.BoolVal(false)).copy(submaps = Map("TestMap" -> submap))
    transport.elaborate(using topoEnv).stations shouldBe empty
  }

  test("unrestricted station is always kept regardless of other param values") {
    val submapCode =
      """
      topo-map TestMap() {
        topo-node hall
        let params: {area: Int} = {area = 0}
      }
      """
    val transportCode =
      """
      transport OP1 is Elevator {
        let params: {velocity: Float, accl: Float} = {velocity = 2.5, accl = 0.8}
        constraint StaffOnly { require haveStaffCard }
        station Floor1 at TestMap::hall {relativeLocation = 0.0} requires StaffOnly
        station Lobby  at TestMap::hall {relativeLocation = 5.0}
      }
      """
    val submap   = parseTopoMap(submapCode).elaborate(using envWithParams())
    val transport = parseTransport(transportCode)

    val topoEnvFail = envWithParams("haveStaffCard" -> Value.BoolVal(false)).copy(submaps = Map("TestMap" -> submap))
    val result = transport.elaborate(using topoEnvFail)
    // Restricted station dropped, unrestricted Lobby kept
    result.stations should have size 1
    result.stations.head._1.nodeName shouldBe "hall"
  }
}
