import corelang.{Environment, Identifier, Interpreter, Term, Type, TypeChecker, TypeCheckResult, Value}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream, Token}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import syntax.TopoMapVisitor
import topomap.grammar.{MapFileLexer, MapFileParser}

class FixpointTest extends AnyFunSuite with Matchers {
  private def term(code: String): Term = {
    val parser = new MapFileParser(new CommonTokenStream(new MapFileLexer(CharStreams.fromString(code))))
    val expression = new TopoMapVisitor().visitExpr(parser.expr())
    parser.getNumberOfSyntaxErrors shouldBe 0
    parser.getCurrentToken.getType shouldBe Token.EOF
    expression.toTerm(Environment.empty[Identifier, Type, Value])
  }

  private val factorialBody = "\\n: Int. if n == 0 then 1 else n * factorial(n - 1)"

  Seq(
    s"(fix factorial: Int -> Int. $factorialBody)(5)",
    s"let rec factorial: Int -> Int = $factorialBody in factorial(5)"
  ).foreach { code =>
    test(s"recursive factorial type checking and evaluation agree: $code") {
      val subject = term(code)
      TypeChecker.check(subject) shouldBe TypeCheckResult.Ok(Type.IntType)
      Interpreter.eval(subject) shouldBe Value.IntVal(120)
    }
  }

  test("recursive bindings preserve captured outer variables") {
    val subject = term(
      "(\\base: Int. let rec sum: Int -> Int = \\n: Int. if n == 0 then base else n + sum(n - 1) in sum(3))(10)"
    )

    TypeChecker.check(subject) shouldBe TypeCheckResult.Ok(Type.IntType)
    Interpreter.eval(subject) shouldBe Value.IntVal(16)
  }

  test("fixpoint bodies can have a non-function annotated type") {
    val subject = term("fix value: Int. 42")
    TypeChecker.check(subject) shouldBe TypeCheckResult.Ok(Type.IntType)
    Interpreter.eval(subject) shouldBe Value.IntVal(42)
  }

  Seq(
    "fix value: Int. \\n: Int. n",
    "fix function: Int -> Int. \\f: Int -> Int. f"
  ).foreach { code =>
    test(s"fixpoint rejects a transformer in place of its annotated result: $code") {
      TypeChecker.check(term(code)).isErr shouldBe true
    }
  }
}
