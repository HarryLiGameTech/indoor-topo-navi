import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import corelang.{Type, Term, Value, Interpreter, OpKind}

class InterpreterTest extends AnyFunSuite with Matchers {
  
  def testInterpreter(
    description: String,
    term: Term,
    expected: Value,
  ): Unit = test(description) {
    val result = Interpreter.eval(term)
    result shouldEqual expected
  }

  // Test integer addition
  testInterpreter("evaluating addition",
    Term.BinOp(OpKind.Add, Term.IntLit(10), Term.IntLit(32)),
    Value.IntVal(42)
  )

  // Test integer subtraction
  testInterpreter("evaluating subtraction",
    Term.BinOp(OpKind.Sub, Term.IntLit(50), Term.IntLit(8)),
    Value.IntVal(42)
  )

  // Test integer multiplication
  testInterpreter("evaluating multiplication",
    Term.BinOp(OpKind.Mul, Term.IntLit(6), Term.IntLit(7)),
    Value.IntVal(42)
  )

  // Test comparison equal
  testInterpreter("evaluating equality comparison (true)",
    Term.BinOp(OpKind.Eq, Term.IntLit(42), Term.IntLit(42)),
    Value.BoolVal(true)
  )

  testInterpreter("evaluating equality comparison (false)",
    Term.BinOp(OpKind.Eq, Term.IntLit(42), Term.IntLit(43)),
    Value.BoolVal(false)
  )

  // Test comparison less than
  testInterpreter("evaluating less than (true)",
    Term.BinOp(OpKind.Lt, Term.IntLit(10), Term.IntLit(20)),
    Value.BoolVal(true)
  )

  testInterpreter("evaluating less than (false)",
    Term.BinOp(OpKind.Lt, Term.IntLit(20), Term.IntLit(10)),
    Value.BoolVal(false)
  )

  // Test comparison greater than
  testInterpreter("evaluating greater than (true)",
    Term.BinOp(OpKind.Gt, Term.IntLit(20), Term.IntLit(10)),
    Value.BoolVal(true)
  )

  testInterpreter("evaluating greater than (false)",
    Term.BinOp(OpKind.Gt, Term.IntLit(10), Term.IntLit(20)),
    Value.BoolVal(false)
  )

  // Test boolean equality
  testInterpreter("evaluating boolean equality (true)",
    Term.BinOp(OpKind.Eq, Term.BoolLit(true), Term.BoolLit(true)),
    Value.BoolVal(true)
  )

  testInterpreter("evaluating boolean equality (false)",
    Term.BinOp(OpKind.Eq, Term.BoolLit(true), Term.BoolLit(false)),
    Value.BoolVal(false)
  )

  // Test if expression - then branch
  testInterpreter("evaluating if expression (then branch)",
    Term.If(Term.BoolLit(true), Term.IntLit(42), Term.IntLit(0)),
    Value.IntVal(42)
  )

  // Test if expression - else branch
  testInterpreter("evaluating if expression (else branch)",
    Term.If(Term.BoolLit(false), Term.IntLit(0), Term.IntLit(42)),
    Value.IntVal(42)
  )

  // Test if with condition evaluation
  testInterpreter("evaluating if with computed condition",
    Term.If(
      Term.BinOp(OpKind.Lt, Term.IntLit(5), Term.IntLit(10)),
      Term.IntLit(100),
      Term.IntLit(200)
    ),
    Value.IntVal(100)
  )

  // Test lambda and application - identity function
  testInterpreter("evaluating identity function", {
    // λx:Int. x applied to 42
    val identityFn = Term.Lam(Type.IntType, Term.Var(0))
    Term.App(identityFn, Term.IntLit(42))
  },
    Value.IntVal(42)
  )

  // Test lambda with computation in body
  testInterpreter("evaluating lambda with computation", {
    // (λx:Int. x + 10) 32
    val addTenFn = Term.Lam(
      Type.IntType,
      Term.BinOp(OpKind.Add, Term.Var(0), Term.IntLit(10))
    )
    Term.App(addTenFn, Term.IntLit(32))
  },
    Value.IntVal(42)
  )

  // Test nested lambdas (curried function)
  testInterpreter("evaluating curried function", {
    // (λx:Int. λy:Int. x + y) 30 12
    val addFn = Term.Lam(
      Type.IntType,
      Term.Lam(
        Type.IntType,
        Term.BinOp(OpKind.Add, Term.Var(1), Term.Var(0))
      )
    )
    val partial = Term.App(addFn, Term.IntLit(30))
    Term.App(partial, Term.IntLit(12))
  },
    Value.IntVal(42)
  )

  // Test closure captures environment
  testInterpreter("evaluating closure with captured variable", {
    // (λx:Int. (λy:Int. x * y) 7) 6
    val fn = Term.Lam(
      Type.IntType,
      Term.App(
        Term.Lam(
          Type.IntType,
          Term.BinOp(OpKind.Mul, Term.Var(1), Term.Var(0))
        ),
        Term.IntLit(7)
      )
    )
    Term.App(fn, Term.IntLit(6))
  },
    Value.IntVal(42)
  )

  // Test fixpoint - factorial function
  testInterpreter("evaluating factorial using fixpoint", {
    // fix f:(Int->Int). λn:Int. if (n == 0) then 1 else n * f(n-1)
    // Applied to 5
    val factorialBody = Term.Lam(
      Type.IntType,
      Term.If(
        Term.BinOp(OpKind.Eq, Term.Var(0), Term.IntLit(0)),
        Term.IntLit(1),
        Term.BinOp(
          OpKind.Mul,
          Term.Var(0),
          Term.App(
            Term.Var(1), // reference to the fixpoint
            Term.BinOp(OpKind.Sub, Term.Var(0), Term.IntLit(1))
          )
        )
      )
    )
    val factorial = Term.Fix(Type.Arrow(Type.IntType, Type.IntType), factorialBody)
    Term.App(factorial, Term.IntLit(5))
  },
    Value.IntVal(120) // 5! = 120
  )

  // Test fixpoint - sum from 1 to n
  testInterpreter("evaluating sum using fixpoint", {
    // fix f:(Int->Int). λn:Int. if (n == 0) then 0 else n + f(n-1)
    // Applied to 10
    val sumBody = Term.Lam(
      Type.IntType,
      Term.If(
        Term.BinOp(OpKind.Eq, Term.Var(0), Term.IntLit(0)),
        Term.IntLit(0),
        Term.BinOp(
          OpKind.Add,
          Term.Var(0),
          Term.App(
            Term.Var(1),
            Term.BinOp(OpKind.Sub, Term.Var(0), Term.IntLit(1))
          )
        )
      )
    )
    val sum = Term.Fix(Type.Arrow(Type.IntType, Type.IntType), sumBody)
    Term.App(sum, Term.IntLit(10))
  },
    Value.IntVal(55) // 1+2+3+...+10 = 55
  )

  // Test fixpoint - fibonacci function
  testInterpreter("evaluating fibonacci using fixpoint", {
    // fix f:(Int->Int). λn:Int. if (n < 2) then n else f(n-1) + f(n-2)
    // Applied to 10
    val fibonacciBody = Term.Lam(
      Type.IntType,
      Term.If(
        Term.BinOp(OpKind.Lt, Term.Var(0), Term.IntLit(2)),
        Term.Var(0),
        Term.BinOp(
          OpKind.Add,
          Term.App(
            Term.Var(1), // reference to the fixpoint
            Term.BinOp(OpKind.Sub, Term.Var(0), Term.IntLit(1))
          ),
          Term.App(
            Term.Var(1), // reference to the fixpoint
            Term.BinOp(OpKind.Sub, Term.Var(0), Term.IntLit(2))
          )
        )
      )
    )
    val fibonacci = Term.Fix(Type.Arrow(Type.IntType, Type.IntType), fibonacciBody)
    Term.App(fibonacci, Term.IntLit(10))
  },
    Value.IntVal(55) // fib(10) = 55
  )

  // Test complex expression
  testInterpreter("evaluating complex nested expression", {
    // ((λx:Int. x * 2) 10) + ((λy:Int. y - 8) 20)
    val leftPart = Term.App(
      Term.Lam(Type.IntType, Term.BinOp(OpKind.Mul, Term.Var(0), Term.IntLit(2))),
      Term.IntLit(10)
    )
    val rightPart = Term.App(
      Term.Lam(Type.IntType, Term.BinOp(OpKind.Sub, Term.Var(0), Term.IntLit(8))),
      Term.IntLit(20)
    )
    Term.BinOp(OpKind.Add, leftPart, rightPart)
  },
    Value.IntVal(32) // (10*2) + (20-8) = 20 + 12 = 32
  )

  // Test record functionality
  testInterpreter("evaluating simple record literal",
    Term.Record(Map("x" -> Term.IntLit(42), "y" -> Term.IntLit(100))),
    Value.RecordVal(Map("x" -> Value.IntVal(42), "y" -> Value.IntVal(100)))
  )

  testInterpreter("evaluating record field selection",
    Term.Proj(
      Term.Record(Map("x" -> Term.IntLit(42), "y" -> Term.IntLit(100))),
      "x"
    ),
    Value.IntVal(42)
  )

  testInterpreter("evaluating record with computed fields",
    Term.Record(Map(
      "sum" -> Term.BinOp(OpKind.Add, Term.IntLit(1), Term.IntLit(2)),
      "product" -> Term.BinOp(OpKind.Mul, Term.IntLit(3), Term.IntLit(4))
    )),
    Value.RecordVal(Map("sum" -> Value.IntVal(3), "product" -> Value.IntVal(12)))
  )

  testInterpreter("evaluating record with boolean fields",
    Term.Record(Map(
      "flag" -> Term.BoolLit(true),
      "comparison" -> Term.BinOp(OpKind.Lt, Term.IntLit(5), Term.IntLit(10))
    )),
    Value.RecordVal(Map("flag" -> Value.BoolVal(true), "comparison" -> Value.BoolVal(true)))
  )

  testInterpreter("evaluating computation with record field selection",
    Term.BinOp(
      OpKind.Add,
      Term.Proj(Term.Record(Map("x" -> Term.IntLit(10), "y" -> Term.IntLit(20))), "x"),
      Term.Proj(Term.Record(Map("x" -> Term.IntLit(10), "y" -> Term.IntLit(20))), "y")
    ),
    Value.IntVal(30)
  )

  testInterpreter("evaluating nested record",
    Term.Record(Map(
      "point" -> Term.Record(Map("x" -> Term.IntLit(5), "y" -> Term.IntLit(10))),
      "value" -> Term.IntLit(42)
    )),
    Value.RecordVal(Map(
      "point" -> Value.RecordVal(Map("x" -> Value.IntVal(5), "y" -> Value.IntVal(10))),
      "value" -> Value.IntVal(42)
    ))
  )

  testInterpreter("evaluating nested record field selection",
    Term.Proj(
      Term.Proj(
        Term.Record(Map(
          "point" -> Term.Record(Map("x" -> Term.IntLit(5), "y" -> Term.IntLit(10))),
          "value" -> Term.IntLit(42)
        )),
        "point"
      ),
      "x"
    ),
    Value.IntVal(5)
  )
}
