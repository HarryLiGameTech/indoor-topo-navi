import corelang.{Environment, Expr, Identifier, Interpreter, OpKind, Term, Type, TypeChecker, TypeCheckResult, Value}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream, Token}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import syntax.TopoMapVisitor
import topomap.grammar.{MapFileLexer, MapFileParser}

class DivisionTest extends AnyFunSuite with Matchers {
  private def parse(code: String): Expr = {
    val parser = new MapFileParser(new CommonTokenStream(new MapFileLexer(CharStreams.fromString(code))))
    val expression = new TopoMapVisitor().visitExpr(parser.expr())
    parser.getNumberOfSyntaxErrors shouldBe 0
    parser.getCurrentToken.getType shouldBe Token.EOF
    expression
  }

  private def term(code: String): Term =
    parse(code).toTerm(Environment.empty[Identifier, Type, Value])

  test("division parses as a binary operator") {
    parse("3 / 2") shouldBe Expr.BinOp(OpKind.Div, Expr.IntLit(3), Expr.IntLit(2))
    OpKind.Div.toString shouldBe "/"
  }

  test("division is left-associative") {
    parse("12 / 3 / 2") shouldBe Expr.BinOp(
      OpKind.Div,
      Expr.BinOp(OpKind.Div, Expr.IntLit(12), Expr.IntLit(3)),
      Expr.IntLit(2)
    )
  }

  Seq(
    "3 / 2" -> 1.5,
    "3 / 2.0" -> 1.5,
    "3.0 / 2" -> 1.5,
    "3.0 / 2.0" -> 1.5,
    "4 / 2" -> 2.0,
    "-3 / 2" -> -1.5,
    "3 / -2" -> -1.5,
    "-3 / -2" -> 1.5,
    "0 / 2" -> 0.0,
    "0.0 / 2.0" -> 0.0,
    "12 / 3 / 2" -> 2.0,
    "12 / (3 / 2)" -> 8.0,
    "2 * 3 / 4" -> 1.5,
    "12 / 3.0 * 2.0" -> 8.0,
    "2.0 + 6 / 3" -> 4.0,
    "(2.0 + 6.0) / 4" -> 2.0
  ).foreach { case (expression, expected) =>
    test(s"division type inference, checking, and evaluation agree: $expression") {
      val subject = term(expression)
      subject.infer() shouldBe Type.FloatType
      TypeChecker.check(subject) shouldBe TypeCheckResult.Ok(Type.FloatType)
      Interpreter.eval(subject) shouldBe Value.FloatVal(expected)
    }
  }

  for (numerator <- Seq("0", "1", "1.0"); denominator <- Seq("0", "0.0", "-0.0", "2 - 2")) {
    test(s"division rejects zero denominator: $numerator / ($denominator)") {
      val subject = term(s"$numerator / ($denominator)")
      TypeChecker.check(subject) shouldBe TypeCheckResult.Ok(Type.FloatType)
      val error = intercept[ArithmeticException] {
        Interpreter.eval(subject)
      }
      error.getMessage shouldBe "Division by zero"
    }
  }

  Seq(Double.NaN, Double.PositiveInfinity, Double.NegativeInfinity).foreach { invalid =>
    for (onLeft <- Seq(true, false)) {
      test(s"division rejects non-finite operand $invalid on ${if (onLeft) "left" else "right"}") {
        val left = if (onLeft) Term.FloatLit(invalid) else Term.IntLit(1)
        val right = if (onLeft) Term.IntLit(1) else Term.FloatLit(invalid)
        val error = intercept[ArithmeticException] {
          Interpreter.eval(Term.BinOp(OpKind.Div, left, right))
        }
        error.getMessage shouldBe "Division requires finite operands"
      }
    }
  }

  test("division rejects floating-point overflow") {
    val error = intercept[ArithmeticException] {
      Interpreter.eval(Term.BinOp(OpKind.Div, Term.FloatLit(Double.MaxValue), Term.FloatLit(0.5)))
    }
    error.getMessage shouldBe "Division result must be finite"
  }

  test("division converts integer operands before evaluation") {
    Interpreter.eval(Term.BinOp(OpKind.Div, Term.IntLit(Long.MinValue), Term.IntLit(-1))) shouldBe
      Value.FloatVal(9223372036854775808.0)
    Interpreter.eval(Term.BinOp(OpKind.Div, Term.IntLit(9007199254740993L), Term.IntLit(1))) shouldBe
      Value.FloatVal(9007199254740992.0)
  }

  Seq("true", "\"3\"", "[3]", "{value = 3}").foreach { invalid =>
    for (onLeft <- Seq(true, false)) {
      val expression = if (onLeft) s"$invalid / 2" else s"2 / $invalid"
      test(s"division rejects nonnumeric operands: $expression") {
        val subject = term(expression)
        TypeChecker.check(subject).isErr shouldBe true
        intercept[RuntimeException] { subject.infer() }
          .getMessage should include("Illegal type for binary operator")
        intercept[RuntimeException] { Interpreter.eval(subject) }
          .getMessage should include("incompatible types")
      }
    }
  }
}
