import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import topomap.grammar.{MapFileLexer, MapFileParser, MapFileVisitor}
import corelang.{Expr, OpKind, Type}

class TopoMapVisitorSpec extends AnyFlatSpec with Matchers {

  /**
   * Helper function to parse a string input starting from the 'expr' rule
   * and visit it to produce an AST.
   */
  def parseExpr(code: String): Expr = {
    val charStream = CharStreams.fromString(code)
    val lexer = new MapFileLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new MapFileParser(tokens)

    // We assume the input is an expression for these tests
    val tree = parser.expr()

    val visitor = new TopoMapVisitor()
    visitor.visitE(tree)
  }

  /**
   * Helper for parsing top-level definitions
   */
  def parseDef(code: String): (String, Expr) = {
    val charStream = CharStreams.fromString(code)
    val lexer = new MapFileLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new MapFileParser(tokens)

    val tree = parser.coreDef() // Assuming input is a 'def'
    val visitor = new TopoMapVisitor()

    // We know coreDef returns Any, but for 'def' input it returns (String, Expr)
    // Manually dispatch based on the specific context type
    tree match {
      case ctx: MapFileParser.FuncDefContext =>
        visitor.visitFuncDef(ctx)

      case ctx: MapFileParser.TypeDefContext =>
        // This helper expects a (String, Expr), but TypeDef returns (String, Type).
        // You might want to throw an exception or handle it differently if testing types.
        throw new IllegalArgumentException("Input code was a type definition, expected a function definition.")

      case ctx: MapFileParser.ScriptExprContext =>
        throw new IllegalArgumentException("Input code was a script expression, expected a function definition.")

      case _ =>
        throw new IllegalArgumentException(s"Unknown context type: ${tree.getClass.getName}")
    }
  }

  "MapFileVisitor" should "parse integer literals" in {
    parseExpr("42") shouldBe Expr.IntLit(42)
  }

  it should "parse float literals" in {
    parseExpr("3.14") shouldBe Expr.FloatLit(3.14)
  }

  it should "parse booleans" in {
    parseExpr("true") shouldBe Expr.BoolLit(true)
    parseExpr("false") shouldBe Expr.BoolLit(false)
  }

  it should "parse basic binary operations" in {
    parseExpr("1 + 2") shouldBe Expr.BinOp(
      OpKind.Add, Expr.IntLit(1), Expr.IntLit(2)
    )

    parseExpr("x * y") shouldBe Expr.BinOp(
      OpKind.Mul, Expr.Var("x"), Expr.Var("y")
    )

    parseExpr("val == 100") shouldBe Expr.BinOp(
      OpKind.Eq, Expr.Var("val"), Expr.IntLit(100)
    )
  }

  it should "handle operator precedence (multiplication before addition)" in {
    // 1 + 2 * 3 -> 1 + (2 * 3)
    parseExpr("1 + 2 * 3") shouldBe Expr.BinOp(
      OpKind.Add,
      Expr.IntLit(1),
      Expr.BinOp(OpKind.Mul, Expr.IntLit(2), Expr.IntLit(3))
    )
  }

  it should "handle parenthesis priority" in {
    // (1 + 2) * 3
    parseExpr("(1 + 2) * 3") shouldBe Expr.BinOp(
      OpKind.Mul,
      Expr.BinOp(OpKind.Add, Expr.IntLit(1), Expr.IntLit(2)),
      Expr.IntLit(3)
    )
  }

  it should "parse if-expressions" in {
    val code = "if x > 0 then 1 else -1"
    parseExpr(code) shouldBe Expr.If(
      Expr.BinOp(OpKind.Gt, Expr.Var("x"), Expr.IntLit(0)),
      Expr.IntLit(1),
      Expr.BinOp(OpKind.Sub, Expr.IntLit(0), Expr.IntLit(1)) // Desugared NegExpr
    )
  }

  it should "parse let expressions" in {
    val code = "let x = 10 in x + 1"
    parseExpr(code) shouldBe Expr.Let(
      "x",
      Expr.IntLit(10),
      Expr.BinOp(OpKind.Add, Expr.Var("x"), Expr.IntLit(1))
    )
  }

  it should "parse single-argument lambda expressions" in {
    // \x: Int. x
    parseExpr("\\x: Int. x") shouldBe Expr.Lam(
      "x", Type.IntType, Expr.Var("x")
    )
  }

  it should "parse function application (ML style)" in {
    // f x
    parseExpr("f x") shouldBe Expr.App(Expr.Var("f"), Expr.Var("x"))
  }

  it should "parse function application (C style with multiple args)" in {
    // f(a, b) -> App(App(f, a), b)
    val code = "f(a, b)"
    parseExpr(code) shouldBe Expr.App(
      Expr.App(Expr.Var("f"), Expr.Var("a")),
      Expr.Var("b")
    )
  }

  it should "parse record literals" in {
    val code = "{ x = 1, y = 2, t = \"string\" }"
    parseExpr(code) shouldBe Expr.Record(Map(
      "x" -> Expr.IntLit(1),
      "y" -> Expr.IntLit(2),
      "t" -> Expr.StringLit("string")
    ))
  }

  it should "parse record with bin-op" in {
    val code = "{sum = 1+2, prod = 3*4}"
    parseExpr(code) shouldBe Expr.Record(Map(
      "sum" -> Expr.BinOp(OpKind.Add, Expr.IntLit(1), Expr.IntLit(2)),
      "prod" -> Expr.BinOp(OpKind.Mul, Expr.IntLit(3), Expr.IntLit(4))
    ))
  }

  it should "parse record with bool values" in {
    val code = "{flag = true, negFlag = false}"
    parseExpr(code) shouldBe Expr.Record(Map(
      "flag" -> Expr.BoolLit(true),
      "negFlag" -> Expr.BoolLit(false)
    ))
  }

  it should "parse nested record" in {
    val code = "{point = {x = 1, y = 2}, label = \"A\"}"
    parseExpr(code) shouldBe Expr.Record(Map(
      "point" -> Expr.Record(Map(
        "x" -> Expr.IntLit(1),
        "y" -> Expr.IntLit(2)
      )),
      "label" -> Expr.StringLit("A")
    ))
  }

  it should "parse projections" in {
    parseExpr("point.x") shouldBe Expr.Proj(Expr.Var("point"), "x")
  }

  it should "convert block statements into nested Let expressions" in {
    val code =
      """{
        |  let x = 1;
        |  let y = 2;
        |  x + y
        |}""".stripMargin

    // Expect: Let("x", 1, Let("y", 2, x + y))
    parseExpr(code) shouldBe Expr.Let(
      "x", Expr.IntLit(1),
      Expr.Let(
        "y", Expr.IntLit(2),
        Expr.BinOp(OpKind.Add, Expr.Var("x"), Expr.Var("y"))
      )
    )
  }

  it should "handle top-level function definitions with currying" in {
    // def add(a: Int, b: Int): Int = a + b
    val code = "def add(a: Int, b: Int): Int = a + b"

    val (name, body) = parseDef(code)

    name shouldBe "add"

    // Expect: Lam("a", Int, Lam("b", Int, BinOp(+, a, b)))
    body shouldBe Expr.Lam(
      "a", Type.IntType,
      Expr.Lam(
        "b", Type.IntType,
        Expr.BinOp(OpKind.Add, Expr.Var("a"), Expr.Var("b"))
      )
    )
  }

  it should "parse complex types" in {
    // We can't test parseType directly easily unless we expose it,
    // but we can test it via a lambda or definition.
    val code = "\\f: Int -> Bool. f 1"

    parseExpr(code) shouldBe Expr.Lam(
      "f",
      Type.Arrow(Type.IntType, Type.BoolType),
      Expr.App(Expr.Var("f"), Expr.IntLit(1))
    )
  }

  it should "parse fixpoint expressions" in {
    // fix f: Int -> Int. \x: Int. if x == 0 then 1 else x * f(x - 1)
    val code = "fix f: Int -> Int. \\x: Int. if x == 0 then 1 else x * f(x - 1)"

    val result = parseExpr(code)

    // Expected AST structure
    result shouldBe Expr.Fix(
      "f",
      Type.Arrow(Type.IntType, Type.IntType),
      Expr.Lam(
        "x", Type.IntType,
        Expr.If(
          Expr.BinOp(OpKind.Eq, Expr.Var("x"), Expr.IntLit(0)),
          Expr.IntLit(1),
          Expr.BinOp(OpKind.Mul,
            Expr.Var("x"),
            Expr.App(
              Expr.Var("f"),
              Expr.BinOp(OpKind.Sub, Expr.Var("x"), Expr.IntLit(1))
            )
          )
        )
      )
    )
  }
}