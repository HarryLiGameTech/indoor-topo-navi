import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import corelang.{Type, Term, OpKind, TypeChecker, TypeCheckResult, TypeEnvironment, Identifier}

class TypeCheckerTest extends AnyFunSuite with Matchers {

  private val emptyEnv = TypeEnvironment.empty[Identifier, Type]

  private def ok(term: Term, expectedType: Type): Unit =
    TypeChecker.check(term, emptyEnv) shouldEqual TypeCheckResult.Ok(expectedType)

  private def okIn(term: Term, env: corelang.TypeOnlyEnv, expectedType: Type): Unit =
    TypeChecker.check(term, env) shouldEqual TypeCheckResult.Ok(expectedType)

  private def err(term: Term): Unit =
    TypeChecker.check(term, emptyEnv).isErr shouldBe true

  private def errIn(term: Term, env: corelang.TypeOnlyEnv): Unit =
    TypeChecker.check(term, env).isErr shouldBe true

  test("integer literal is IntType") {
    ok(Term.IntLit(42), Type.IntType)
  }

  test("float literal is FloatType") {
    ok(Term.FloatLit(3.14), Type.FloatType)
  }

  test("bool literal is BoolType") {
    ok(Term.BoolLit(true), Type.BoolType)
  }

  test("string literal is StringType") {
    ok(Term.StringLit("hello"), Type.StringType)
  }

  test("integer addition yields IntType") {
    ok(Term.BinOp(OpKind.Add, Term.IntLit(1), Term.IntLit(2)), Type.IntType)
  }

  test("float subtraction yields FloatType") {
    ok(Term.BinOp(OpKind.Sub, Term.FloatLit(3.0), Term.FloatLit(1.0)), Type.FloatType)
  }

  test("integer equality yields BoolType") {
    ok(Term.BinOp(OpKind.Eq, Term.IntLit(1), Term.IntLit(1)), Type.BoolType)
  }

  test("less-than on integers yields BoolType") {
    ok(Term.BinOp(OpKind.Lt, Term.IntLit(1), Term.IntLit(2)), Type.BoolType)
  }

  test("greater-than on floats yields BoolType") {
    ok(Term.BinOp(OpKind.Gt, Term.FloatLit(2.0), Term.FloatLit(1.0)), Type.BoolType)
  }

  test("bool equality yields BoolType") {
    ok(Term.BinOp(OpKind.Eq, Term.BoolLit(true), Term.BoolLit(false)), Type.BoolType)
  }

  test("string concatenation yields StringType") {
    ok(Term.BinOp(OpKind.Concat, Term.StringLit("a"), Term.StringLit("b")), Type.StringType)
  }

  test("type mismatch in binary operator is an error") {
    err(Term.BinOp(OpKind.Add, Term.IntLit(1), Term.FloatLit(2.0)))
  }

  test("applying add to booleans is an error") {
    err(Term.BinOp(OpKind.Add, Term.BoolLit(true), Term.BoolLit(false)))
  }

  test("negation of int yields IntType") {
    ok(Term.BinOp(OpKind.Neg, Term.IntLit(5), Term.IntLit(0)), Type.IntType)
  }

  test("negation of float yields FloatType") {
    ok(Term.BinOp(OpKind.Neg, Term.FloatLit(1.0), Term.FloatLit(0.0)), Type.FloatType)
  }

  test("negation of bool is an error") {
    err(Term.BinOp(OpKind.Neg, Term.BoolLit(true), Term.BoolLit(false)))
  }

  test("list concatenation with matching element types yields ListType") {
    val listTerm = Term.BinOp(
      OpKind.Concat,
      Term.ListLit(Some(Type.IntType), List(Term.IntLit(1))),
      Term.ListLit(Some(Type.IntType), List(Term.IntLit(2)))
    )
    ok(listTerm, Type.ListType(Type.IntType))
  }

  test("list concatenation with mismatched element types is an error") {
    val listTerm = Term.BinOp(
      OpKind.Concat,
      Term.ListLit(Some(Type.IntType), List(Term.IntLit(1))),
      Term.ListLit(Some(Type.FloatType), List(Term.FloatLit(2.0)))
    )
    err(listTerm)
  }

  test("well-typed if expression returns branch type") {
    ok(
      Term.If(Term.BoolLit(true), Term.IntLit(1), Term.IntLit(2)),
      Type.IntType
    )
  }

  test("if expression with non-bool condition is an error") {
    err(Term.If(Term.IntLit(1), Term.IntLit(1), Term.IntLit(2)))
  }

  test("if expression with mismatched branch types is an error") {
    err(Term.If(Term.BoolLit(true), Term.IntLit(1), Term.FloatLit(2.0)))
  }

  test("lambda over int body returns Arrow type") {
    ok(
      Term.Lam(Type.IntType, Term.Var(0)),
      Type.Arrow(Type.IntType, Type.IntType)
    )
  }

  test("lambda returning constant string") {
    ok(
      Term.Lam(Type.IntType, Term.StringLit("hi")),
      Type.Arrow(Type.IntType, Type.StringType)
    )
  }

  test("well-typed application returns return type") {
    val fn = Term.Lam(Type.IntType, Term.BinOp(OpKind.Add, Term.Var(0), Term.IntLit(10)))
    ok(Term.App(fn, Term.IntLit(5)), Type.IntType)
  }

  test("application with wrong argument type is an error") {
    val fn = Term.Lam(Type.IntType, Term.Var(0))
    err(Term.App(fn, Term.BoolLit(true)))
  }

  test("applying a non-function is an error") {
    err(Term.App(Term.IntLit(1), Term.IntLit(2)))
  }

  test("curried function application") {
    val addFn = Term.Lam(
      Type.IntType,
      Term.Lam(Type.IntType, Term.BinOp(OpKind.Add, Term.Var(1), Term.Var(0)))
    )
    val partial = Term.App(addFn, Term.IntLit(3))
    ok(partial, Type.Arrow(Type.IntType, Type.IntType))
    ok(Term.App(partial, Term.IntLit(4)), Type.IntType)
  }

  test("well-typed fixpoint") {
    val factBody = Term.Lam(
      Type.IntType,
      Term.If(
        Term.BinOp(OpKind.Eq, Term.Var(0), Term.IntLit(0)),
        Term.IntLit(1),
        Term.BinOp(
          OpKind.Mul,
          Term.Var(0),
          Term.App(Term.Var(1), Term.BinOp(OpKind.Sub, Term.Var(0), Term.IntLit(1)))
        )
      )
    )
    ok(
      Term.Fix(Type.Arrow(Type.IntType, Type.IntType), factBody),
      Type.Arrow(Type.IntType, Type.IntType)
    )
  }

  test("fixpoint with wrong body type is an error") {
    val badBody = Term.Lam(Type.IntType, Term.BoolLit(false))
    err(Term.Fix(Type.Arrow(Type.IntType, Type.IntType), badBody))
  }

  test("well-typed record literal") {
    ok(
      Term.Record(Map("x" -> Term.IntLit(1), "y" -> Term.BoolLit(true))),
      Type.RecordType(Map("x" -> Type.IntType, "y" -> Type.BoolType))
    )
  }

  test("record with ill-typed field is an error") {
    err(Term.Record(Map(
      "x" -> Term.BinOp(OpKind.Add, Term.IntLit(1), Term.BoolLit(false))
    )))
  }

  test("projection from record returns field type") {
    ok(
      Term.Proj(Term.Record(Map("x" -> Term.IntLit(42))), "x"),
      Type.IntType
    )
  }

  test("projection of missing field is an error") {
    err(Term.Proj(Term.Record(Map("x" -> Term.IntLit(42))), "y"))
  }

  test("projection from non-record is an error") {
    err(Term.Proj(Term.IntLit(42), "x"))
  }

  test("annotated empty list is well-typed") {
    ok(Term.ListLit(Some(Type.IntType), Nil), Type.ListType(Type.IntType))
  }

  test("unannotated empty list is an error") {
    err(Term.ListLit(None, Nil))
  }

  test("homogeneous list is well-typed") {
    ok(
      Term.ListLit(None, List(Term.IntLit(1), Term.IntLit(2), Term.IntLit(3))),
      Type.ListType(Type.IntType)
    )
  }

  test("heterogeneous list is an error") {
    err(Term.ListLit(None, List(Term.IntLit(1), Term.BoolLit(true))))
  }

  test("list annotation mismatch is an error") {
    err(Term.ListLit(Some(Type.FloatType), List(Term.IntLit(1))))
  }

  test("proposition type is PropositionType") {
    ok(
      Term.Proposition(Set(Term.BinOp(OpKind.Lt, Term.IntLit(0), Term.IntLit(10)))),
      Type.PropositionType
    )
  }

  test("proposition with ill-typed predicate is an error") {
    err(Term.Proposition(Set(Term.BinOp(OpKind.Add, Term.IntLit(1), Term.BoolLit(false)))))
  }

  test("enum literal with valid variant is well-typed") {
    val direction: Type.EnumType = Type.EnumType("Direction", Set("North", "South", "East", "West"))
    ok(Term.EnumLit(direction, "North"), direction)
  }

  test("enum literal with unknown variant is an error") {
    val direction: Type.EnumType = Type.EnumType("Direction", Set("North", "South"))
    err(Term.EnumLit(direction, "Up"))
  }

  test("match expression on enum returns branch type") {
    val color: Type.EnumType = Type.EnumType("Color", Set("Red", "Green"))
    val scrutinee = Term.EnumLit(color, "Red")
    val cases = List(
      "Red"   -> Term.IntLit(1),
      "Green" -> Term.IntLit(2),
    )
    ok(Term.Match(scrutinee, cases), Type.IntType)
  }

  test("match with unknown variant in case is an error") {
    val color: Type.EnumType = Type.EnumType("Color", Set("Red", "Green"))
    val scrutinee = Term.EnumLit(color, "Red")
    val cases = List("Blue" -> Term.IntLit(0))
    err(Term.Match(scrutinee, cases))
  }

  test("match with inconsistent branch types is an error") {
    val color: Type.EnumType = Type.EnumType("Color", Set("Red", "Green"))
    val scrutinee = Term.EnumLit(color, "Red")
    val cases = List(
      "Red"   -> Term.IntLit(1),
      "Green" -> Term.BoolLit(false),
    )
    err(Term.Match(scrutinee, cases))
  }

  test("match with duplicate cases is an error") {
    val color: Type.EnumType = Type.EnumType("Color", Set("Red", "Green"))
    val scrutinee = Term.EnumLit(color, "Red")
    val cases = List(
      "Red" -> Term.IntLit(1),
      "Red" -> Term.IntLit(2),
    )
    err(Term.Match(scrutinee, cases))
  }

  test("match on non-enum type is an error") {
    err(Term.Match(Term.IntLit(1), List("x" -> Term.IntLit(0))))
  }

  test("empty match is an error") {
    val color: Type.EnumType = Type.EnumType("Color", Set("Red"))
    err(Term.Match(Term.EnumLit(color, "Red"), Nil))
  }

  test("bound variable in lambda body is well-typed") {
    ok(Term.Lam(Type.BoolType, Term.Var(0)), Type.Arrow(Type.BoolType, Type.BoolType))
  }

  test("unbound variable index is an error") {
    err(Term.Var(99))
  }

  test("symbol present in environment resolves correctly") {
    val env = TypeEnvironment.empty[Identifier, Type]
      .addTypeVar(Identifier.Symbol("pi"), Type.FloatType)
    okIn(Term.Sym("pi"), env, Type.FloatType)
  }

  test("symbol absent from environment is an error") {
    errIn(Term.Sym("missing"), emptyEnv)
  }

  test("nested record projection") {
    val inner = Term.Record(Map("z" -> Term.IntLit(7)))
    val outer = Term.Record(Map("inner" -> inner))
    ok(
      Term.Proj(Term.Proj(outer, "inner"), "z"),
      Type.IntType
    )
  }

  test("complex well-typed program: curried add with if") {
    val addOrZero = Term.Lam(
      Type.IntType,
      Term.Lam(
        Type.BoolType,
        Term.If(
          Term.Var(0),
          Term.BinOp(OpKind.Add, Term.Var(1), Term.IntLit(10)),
          Term.IntLit(0)
        )
      )
    )
    ok(
      addOrZero,
      Type.Arrow(Type.IntType, Type.Arrow(Type.BoolType, Type.IntType))
    )
  }
}
