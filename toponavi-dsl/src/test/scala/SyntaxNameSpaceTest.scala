import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import surfacelang.SyntaxNameSpace
import corelang.{Environment, Expr, Identifier, Interpreter, Term, Type, Value}

/**
 * Phantom implementation of SyntaxNameSpace for testing purposes.
 * This allows us to test the trait's methods without implementing the full concrete class.
 */
class SyntaxNameSpacePhantom(
  override val types: List[(String, Type)],
  override val defns: List[(String, Expr)]
) extends SyntaxNameSpace

class SyntaxNameSpaceTest extends AnyFunSuite with should.Matchers {

  test("lookup should return defined expression") {
    val typeDefs = List(
      ("MyInt", Type.IntType),
      ("MyBool", Type.BoolType)
    )
    val exprDefs = List(
      ("x", Expr.IntLit(42)),
      ("y", Expr.StringLit("hello"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("x") should equal(Expr.IntLit(42))
    nameSpace.lookup("y") should equal(Expr.StringLit("hello"))
  }

  test("lookup should throw exception for undefined term") {
    val typeDefs = List()
    val exprDefs = List(
      ("x", Expr.IntLit(42))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    an[RuntimeException] should be thrownBy {
      nameSpace.lookup("undefined")
    }
  }

  test("synthesisEnv should create proper type environment") {
    val typeDefs = List(
      ("MyInt", Type.IntType),
      ("MyFloat", Type.FloatType)
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    // Verify that types are added to the environment
    env.getType(Identifier.Symbol("MyInt")) should equal(Some(Type.IntType))
    env.getType(Identifier.Symbol("MyFloat")) should equal(Some(Type.FloatType))
  }

  test("synthesisEnv should evaluate expressions and add them to value environment") {
    val typeDefs = List(
      ("IntType", Type.IntType),
      ("BoolType", Type.BoolType)
    )
    val exprDefs = List(
      ("intVal", Expr.IntLit(100)),
      ("boolVal", Expr.BoolLit(true)),
      ("stringVal", Expr.StringLit("test"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    // Verify that values are added to the environment and have correct types
    env.getValue(Identifier.Symbol("intVal")) should equal(Some(Value.IntVal(100)))
    env.getValue(Identifier.Symbol("boolVal")) should equal(Some(Value.BoolVal(true)))
    env.getValue(Identifier.Symbol("stringVal")) should equal(Some(Value.StringVal("test")))
  }

  test("synthesisEnv should handle list expressions") {
    val typeDefs = List(
      ("IntListType", Type.ListType(Type.IntType))
    )
    val exprDefs = List(
      ("myList", Expr.ListLit(Some(Type.IntType), List(
        Expr.IntLit(1),
        Expr.IntLit(2),
        Expr.IntLit(3)
      )))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("myList")) should equal(
      Some(Value.ListVal(Type.IntType, List(
        Value.IntVal(1),
        Value.IntVal(2),
        Value.IntVal(3)
      )))
    )
  }

  test("synthesisEnv should handle record expressions") {
    val typeDefs = List(
      ("RecordType", Type.RecordType(Map("x" -> Type.IntType, "y" -> Type.StringType)))
    )
    val exprDefs = List(
      ("myRecord", Expr.Record(Map(
        "x" -> Expr.IntLit(42),
        "y" -> Expr.StringLit("world")
      )))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("myRecord")) should equal(
      Some(Value.RecordVal(Map(
        "x" -> Value.IntVal(42),
        "y" -> Value.StringVal("world")
      )))
    )
  }

  test("synthesisEnv should handle empty definitions") {
    val typeDefs = List()
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.types should be(empty)
    env.values should be(empty)
  }

  test("synthesisEnv should handle multiple types and expressions") {
    val typeDefs = List(
      ("Type1", Type.IntType),
      ("Type2", Type.StringType),
      ("Type3", Type.BoolType),
      ("Type4", Type.FloatType)
    )
    val exprDefs = List(
      ("expr1", Expr.IntLit(1)),
      ("expr2", Expr.StringLit("two")),
      ("expr3", Expr.BoolLit(false))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    // Verify all types are in environment
    typeDefs.foreach { case (name, tpe) =>
      env.getType(Identifier.Symbol(name)) should equal(Some(tpe))
    }
    
    // Verify all values are in environment
    env.getValue(Identifier.Symbol("expr1")) should equal(Some(Value.IntVal(1)))
    env.getValue(Identifier.Symbol("expr2")) should equal(Some(Value.StringVal("two")))
    env.getValue(Identifier.Symbol("expr3")) should equal(Some(Value.BoolVal(false)))
  }

  test("lookup should work with complex expressions") {
    val typeDefs = List(
      ("ListType", Type.ListType(Type.IntType))
    )
    val complexExpr = Expr.BinOp(corelang.OpKind.Add, Expr.IntLit(10), Expr.IntLit(20))
    val exprDefs = List(
      ("addition", complexExpr)
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("addition") should equal(complexExpr)
  }

  test("lookup error message should contain the undefined term name") {
    val typeDefs = List()
    val exprDefs = List(
      ("defined", Expr.IntLit(1))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    val exception = intercept[RuntimeException] {
      nameSpace.lookup("notDefined")
    }
    exception.getMessage should include("notDefined")
  }

  test("synthesisEnv should handle enum types and values") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green", "Blue")))
    )
    val exprDefs = List(
      ("myColor", Expr.EnumLit("Color", "Red"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("Color")) should equal(Some(Type.EnumType("Color", Set("Red", "Green", "Blue"))))
    env.getValue(Identifier.Symbol("myColor")) should equal(Some(Value.EnumVal(Type.EnumType("Color", Set("Red", "Green", "Blue")), "Red")))
  }

  test("synthesisEnv should handle nested list types") {
    val typeDefs = List(
      ("NestedList", Type.ListType(Type.ListType(Type.IntType)))
    )
    val exprDefs = List(
      ("nestedList", Expr.ListLit(Some(Type.ListType(Type.IntType)), List(
        Expr.ListLit(Some(Type.IntType), List(Expr.IntLit(1), Expr.IntLit(2))),
        Expr.ListLit(Some(Type.IntType), List(Expr.IntLit(3), Expr.IntLit(4)))
      )))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("nestedList")) should not be(None)
  }

  test("synthesisEnv should handle function types") {
    val typeDefs = List(
      ("IntToInt", Type.Arrow(Type.IntType, Type.IntType)),
      ("IntToBool", Type.Arrow(Type.IntType, Type.BoolType))
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("IntToInt")) should equal(Some(Type.Arrow(Type.IntType, Type.IntType)))
    env.getType(Identifier.Symbol("IntToBool")) should equal(Some(Type.Arrow(Type.IntType, Type.BoolType)))
  }

  test("synthesisEnv should handle float values") {
    val typeDefs = List(
      ("FloatType", Type.FloatType)
    )
    val exprDefs = List(
      ("pi", Expr.FloatLit(3.14159)),
      ("e", Expr.FloatLit(2.71828))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("pi")) should equal(Some(Value.FloatVal(3.14159)))
    env.getValue(Identifier.Symbol("e")) should equal(Some(Value.FloatVal(2.71828)))
  }

  test("lookup should differentiate between different expression types") {
    val typeDefs = List()
    val exprDefs = List(
      ("int", Expr.IntLit(42)),
      ("bool", Expr.BoolLit(true)),
      ("string", Expr.StringLit("test")),
      ("float", Expr.FloatLit(3.14))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("int") should equal(Expr.IntLit(42))
    nameSpace.lookup("bool") should equal(Expr.BoolLit(true))
    nameSpace.lookup("string") should equal(Expr.StringLit("test"))
    nameSpace.lookup("float") should equal(Expr.FloatLit(3.14))
  }

  test("synthesisEnv should handle complex record types with nested fields") {
    val typeDefs = List(
      ("ComplexRecord", Type.RecordType(Map(
        "id" -> Type.IntType,
        "name" -> Type.StringType,
        "active" -> Type.BoolType,
        "scores" -> Type.ListType(Type.FloatType)
      )))
    )
    val exprDefs = List(
      ("record", Expr.Record(Map(
        "id" -> Expr.IntLit(1),
        "name" -> Expr.StringLit("Alice"),
        "active" -> Expr.BoolLit(true),
        "scores" -> Expr.ListLit(Some(Type.FloatType), List(
          Expr.FloatLit(95.5),
          Expr.FloatLit(87.3)
        ))
      )))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("record")) should not be(None)
  }

  test("lookup should handle names with special characters") {
    val typeDefs = List()
    val exprDefs = List(
      ("_private", Expr.IntLit(10)),
      ("camelCase", Expr.StringLit("test")),
      ("snake_case", Expr.BoolLit(false))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("_private") should equal(Expr.IntLit(10))
    nameSpace.lookup("camelCase") should equal(Expr.StringLit("test"))
    nameSpace.lookup("snake_case") should equal(Expr.BoolLit(false))
  }

  test("synthesisEnv should handle binary operations") {
    val typeDefs = List()
    val exprDefs = List(
      ("add", Expr.BinOp(corelang.OpKind.Add, Expr.IntLit(5), Expr.IntLit(3))),
      ("sub", Expr.BinOp(corelang.OpKind.Sub, Expr.IntLit(10), Expr.IntLit(4))),
      ("mul", Expr.BinOp(corelang.OpKind.Mul, Expr.IntLit(6), Expr.IntLit(7))),
      ("eq", Expr.BinOp(corelang.OpKind.Eq, Expr.IntLit(5), Expr.IntLit(5)))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    // Verify the expressions are evaluated
    env.getValue(Identifier.Symbol("add")) should not be(None)
    env.getValue(Identifier.Symbol("sub")) should not be(None)
    env.getValue(Identifier.Symbol("mul")) should not be(None)
    env.getValue(Identifier.Symbol("eq")) should not be(None)
  }

  test("synthesisEnv should handle conditional expressions") {
    val typeDefs = List()
    val exprDefs = List(
      ("conditional", Expr.If(
        Expr.BoolLit(true),
        Expr.IntLit(100),
        Expr.IntLit(0)
      ))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("conditional")) should equal(Some(Value.IntVal(100)))
  }

  test("synthesisEnv should handle empty lists") {
    val typeDefs = List()
    val exprDefs = List(
      ("emptyList", Expr.ListLit(Some(Type.IntType), List()))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("emptyList")) should equal(
      Some(Value.ListVal(Type.IntType, List()))
    )
  }

  test("synthesisEnv should handle empty records") {
    val typeDefs = List()
    val exprDefs = List(
      ("emptyRecord", Expr.Record(Map()))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("emptyRecord")) should equal(
      Some(Value.RecordVal(Map()))
    )
  }

  test("lookup should handle case-sensitive names") {
    val typeDefs = List()
    val exprDefs = List(
      ("Value", Expr.IntLit(1)),
      ("value", Expr.IntLit(2))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("Value") should equal(Expr.IntLit(1))
    nameSpace.lookup("value") should equal(Expr.IntLit(2))
  }

  test("synthesisEnv should preserve all type definitions") {
    val typeDefs = List(
      ("Int", Type.IntType),
      ("Float", Type.FloatType),
      ("Bool", Type.BoolType),
      ("String", Type.StringType),
      ("IntList", Type.ListType(Type.IntType)),
      ("IntArrow", Type.Arrow(Type.IntType, Type.IntType))
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    typeDefs.foreach { case (name, tpe) =>
      env.getType(Identifier.Symbol(name)) should equal(Some(tpe))
    }
  }

  test("synthesisEnv should preserve all expression definitions") {
    val typeDefs = List()
    val exprDefs = List(
      ("a", Expr.IntLit(1)),
      ("b", Expr.IntLit(2)),
      ("c", Expr.IntLit(3)),
      ("d", Expr.IntLit(4)),
      ("e", Expr.IntLit(5))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    exprDefs.foreach { case (name, expr) =>
      env.getValue(Identifier.Symbol(name)) should not be(None)
    }
  }

  test("lookup should fail for whitespace-only names") {
    val typeDefs = List()
    val exprDefs = List(
      ("x", Expr.IntLit(1))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    an[RuntimeException] should be thrownBy {
      nameSpace.lookup("   ")
    }
  }

  test("lookup should fail for empty string") {
    val typeDefs = List()
    val exprDefs = List(
      ("x", Expr.IntLit(1))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    an[RuntimeException] should be thrownBy {
      nameSpace.lookup("")
    }
  }

  test("synthesisEnv should handle large numbers") {
    val typeDefs = List()
    val exprDefs = List(
      ("largeInt", Expr.IntLit(9223372036854775807L)),
      ("negativeInt", Expr.IntLit(-9223372036854775808L))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("largeInt")) should equal(Some(Value.IntVal(9223372036854775807L)))
    env.getValue(Identifier.Symbol("negativeInt")) should equal(Some(Value.IntVal(-9223372036854775808L)))
  }

  test("synthesisEnv should handle special float values") {
    val typeDefs = List()
    val exprDefs = List(
      ("zero", Expr.FloatLit(0.0)),
      ("negative", Expr.FloatLit(-3.14))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("zero")) should equal(Some(Value.FloatVal(0.0)))
    env.getValue(Identifier.Symbol("negative")) should equal(Some(Value.FloatVal(-3.14)))
  }

  test("synthesisEnv should handle strings with special characters") {
    val typeDefs = List()
    val exprDefs = List(
      ("str1", Expr.StringLit("hello\nworld")),
      ("str2", Expr.StringLit("tab\there")),
      ("str3", Expr.StringLit("quote'test"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("str1")) should equal(Some(Value.StringVal("hello\nworld")))
    env.getValue(Identifier.Symbol("str2")) should equal(Some(Value.StringVal("tab\there")))
    env.getValue(Identifier.Symbol("str3")) should equal(Some(Value.StringVal("quote'test")))
  }

  test("synthesisEnv should handle concatenation operations") {
    val typeDefs = List()
    val exprDefs = List(
      ("concat", Expr.BinOp(corelang.OpKind.Concat, 
        Expr.StringLit("hello"), 
        Expr.StringLit("world")))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("concat")) should not be(None)
  }

  test("synthesisEnv should handle comparison operations") {
    val typeDefs = List()
    val exprDefs = List(
      ("lt", Expr.BinOp(corelang.OpKind.Lt, Expr.IntLit(5), Expr.IntLit(10))),
      ("gt", Expr.BinOp(corelang.OpKind.Gt, Expr.IntLit(10), Expr.IntLit(5)))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("lt")) should not be(None)
    env.getValue(Identifier.Symbol("gt")) should not be(None)
  }

  test("lookup multiple times should return same result") {
    val typeDefs = List()
    val exprDefs = List(
      ("x", Expr.IntLit(42))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("x") should equal(nameSpace.lookup("x"))
    nameSpace.lookup("x") should equal(nameSpace.lookup("x"))
  }

  test("synthesisEnv should be idempotent") {
    val typeDefs = List(
      ("Type1", Type.IntType)
    )
    val exprDefs = List(
      ("expr1", Expr.IntLit(42))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    val env1 = nameSpace.synthesisEnv
    val env2 = nameSpace.synthesisEnv
    
    env1.types should equal(env2.types)
    env1.values should equal(env2.values)
  }

  test("synthesisEnv should handle mixed type and expression definitions") {
    val typeDefs = List(
      ("MyInt", Type.IntType),
      ("MyString", Type.StringType),
      ("MyList", Type.ListType(Type.IntType))
    )
    val exprDefs = List(
      ("num", Expr.IntLit(100)),
      ("text", Expr.StringLit("mixed")),
      ("list", Expr.ListLit(Some(Type.IntType), List(Expr.IntLit(1))))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    // Verify types are present
    env.getType(Identifier.Symbol("MyInt")) should equal(Some(Type.IntType))
    env.getType(Identifier.Symbol("MyString")) should equal(Some(Type.StringType))
    env.getType(Identifier.Symbol("MyList")) should equal(Some(Type.ListType(Type.IntType)))
    
    // Verify values are present
    env.getValue(Identifier.Symbol("num")) should equal(Some(Value.IntVal(100)))
    env.getValue(Identifier.Symbol("text")) should equal(Some(Value.StringVal("mixed")))
    env.getValue(Identifier.Symbol("list")) should not be(None)
  }

  // ============ COMPREHENSIVE ENUM TESTS ============

  test("lookup should work with enum literals") {
    val typeDefs = List()
    val exprDefs = List(
      ("color", Expr.EnumLit("Color", "Red")),
      ("status", Expr.EnumLit("Status", "Active"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("color") should equal(Expr.EnumLit("Color", "Red"))
    nameSpace.lookup("status") should equal(Expr.EnumLit("Status", "Active"))
  }

  test("synthesisEnv should evaluate enum literal values") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green", "Blue")))
    )
    val exprDefs = List(
      ("red", Expr.EnumLit("Color", "Red")),
      ("green", Expr.EnumLit("Color", "Green")),
      ("blue", Expr.EnumLit("Color", "Blue"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("red")) should equal(Some(Value.EnumVal(Type.EnumType("Color", Set("Red", "Green", "Blue")), "Red")))
    env.getValue(Identifier.Symbol("green")) should equal(Some(Value.EnumVal(Type.EnumType("Color", Set("Red", "Green", "Blue")), "Green")))
    env.getValue(Identifier.Symbol("blue")) should equal(Some(Value.EnumVal(Type.EnumType("Color", Set("Red", "Green", "Blue")), "Blue")))
  }

  test("synthesisEnv should handle enum types with multiple variants") {
    val typeDefs = List(
      ("Status", Type.EnumType("Status", Set("Pending", "Active", "Inactive", "Completed")))
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("Status")) should equal(
      Some(Type.EnumType("Status", Set("Pending", "Active", "Inactive", "Completed")))
    )
  }

  test("synthesisEnv should handle multiple enum type definitions") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green", "Blue"))),
      ("Size", Type.EnumType("Size", Set("Small", "Medium", "Large"))),
      ("Priority", Type.EnumType("Priority", Set("Low", "High", "Critical")))
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("Color")) should not be(None)
    env.getType(Identifier.Symbol("Size")) should not be(None)
    env.getType(Identifier.Symbol("Priority")) should not be(None)
  }

  test("synthesisEnv should handle enum values with different types") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green", "Blue"))),
      ("Size", Type.EnumType("Size", Set("S", "M", "L")))
    )
    val exprDefs = List(
      ("myColor", Expr.EnumLit("Color", "Red")),
      ("mySize", Expr.EnumLit("Size", "M"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("myColor")) should equal(Some(Value.EnumVal(Type.EnumType("Color", Set("Red", "Green", "Blue")), "Red")))
    env.getValue(Identifier.Symbol("mySize")) should equal(Some(Value.EnumVal(Type.EnumType("Size", Set("S", "M", "L")), "M")))
  }

  test("synthesisEnv should handle enum types with single variant") {
    val typeDefs = List(
      ("Unit", Type.EnumType("Unit", Set("Unit")))
    )
    val exprDefs = List(
      ("unit", Expr.EnumLit("Unit", "Unit"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("Unit")) should equal(Some(Type.EnumType("Unit", Set("Unit"))))
    env.getValue(Identifier.Symbol("unit")) should equal(Some(Value.EnumVal(Type.EnumType("Unit", Set("Unit")), "Unit")))
  }

  test("synthesisEnv should handle enum types with many variants") {
    val variants = Set("V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9", "V10")
    val typeDefs = List(
      ("ManyEnum", Type.EnumType("ManyEnum", variants))
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("ManyEnum")) should equal(Some(Type.EnumType("ManyEnum", variants)))
  }

  test("synthesisEnv should evaluate all enum variant values") {
    val typeDefs = List(
      ("Level", Type.EnumType("Level", Set("Low", "Medium", "High")))
    )
    val exprDefs = List(
      ("low", Expr.EnumLit("Level", "Low")),
      ("medium", Expr.EnumLit("Level", "Medium")),
      ("high", Expr.EnumLit("Level", "High"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("low")) should equal(Some(Value.EnumVal(Type.EnumType("Level", Set("Low", "Medium", "High")), "Low")))
    env.getValue(Identifier.Symbol("medium")) should equal(Some(Value.EnumVal(Type.EnumType("Level", Set("Low", "Medium", "High")), "Medium")))
    env.getValue(Identifier.Symbol("high")) should equal(Some(Value.EnumVal(Type.EnumType("Level", Set("Low", "Medium", "High")), "High")))
  }

  test("synthesisEnv should handle enum values in records") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green", "Blue"))),
      ("Item", Type.RecordType(Map(
        "name" -> Type.StringType,
        "color" -> Type.EnumType("Color", Set("Red", "Green", "Blue"))
      )))
    )
    val exprDefs = List(
      ("item", Expr.Record(Map(
        "name" -> Expr.StringLit("Ball"),
        "color" -> Expr.EnumLit("Color", "Red")
      )))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("item")) should not be(None)
  }

  test("synthesisEnv should handle enum values in lists") {
    val typeDefs = List(
      ("Status", Type.EnumType("Status", Set("Active", "Inactive")))
    )
    val exprDefs = List(
      ("statuses", Expr.ListLit(
        Some(Type.EnumType("Status", Set("Active", "Inactive"))),
        List(
          Expr.EnumLit("Status", "Active"),
          Expr.EnumLit("Status", "Inactive"),
          Expr.EnumLit("Status", "Active")
        )
      ))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("statuses")) should not be(None)
  }

  test("synthesisEnv should handle match expressions with enums") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green", "Blue")))
    )
    val exprDefs = List(
      ("colorMatch", Expr.Match(
        Expr.EnumLit("Color", "Red"),
        List(
          ("Red", Expr.StringLit("red")),
          ("Green", Expr.StringLit("green")),
          ("Blue", Expr.StringLit("blue"))
        )
      ))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("colorMatch")) should not be(None)
  }

  test("lookup should differentiate between enum types and values") {
    val typeDefs = List(
      ("Color", Type.EnumType("Color", Set("Red", "Green")))
    )
    val exprDefs = List(
      ("red", Expr.EnumLit("Color", "Red"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    // Type lookup
    val env = nameSpace.synthesisEnv
    env.getType(Identifier.Symbol("Color")) should not be(None)
    
    // Value lookup
    nameSpace.lookup("red") should equal(Expr.EnumLit("Color", "Red"))
  }

  test("synthesisEnv should handle enum names with underscores") {
    val typeDefs = List(
      ("Color_Type", Type.EnumType("Color_Type", Set("Red_Orange", "Green_Yellow", "Blue_Purple")))
    )
    val exprDefs = List(
      ("color_val", Expr.EnumLit("Color_Type", "Red_Orange"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("Color_Type")) should not be(None)
    env.getValue(Identifier.Symbol("color_val")) should equal(Some(Value.EnumVal(Type.EnumType("Color_Type", Set("Red_Orange", "Green_Yellow", "Blue_Purple")), "Red_Orange")))
  }

  test("synthesisEnv should handle enum names with mixed case") {
    val typeDefs = List(
      ("HTTPStatus", Type.EnumType("HTTPStatus", Set("OK", "NotFound", "ServerError")))
    )
    val exprDefs = List(
      ("status", Expr.EnumLit("HTTPStatus", "NotFound"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("status")) should equal(Some(Value.EnumVal(Type.EnumType("HTTPStatus", Set("OK", "NotFound", "ServerError")), "NotFound")))
  }

  test("synthesisEnv should handle boolean-like enum types") {
    val typeDefs = List(
      ("Boolean", Type.EnumType("Boolean", Set("True", "False")))
    )
    val exprDefs = List(
      ("isValid", Expr.EnumLit("Boolean", "True")),
      ("isInvalid", Expr.EnumLit("Boolean", "False"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("isValid")) should equal(Some(Value.EnumVal(Type.EnumType("Boolean", Set("True", "False")), "True")))
    env.getValue(Identifier.Symbol("isInvalid")) should equal(Some(Value.EnumVal(Type.EnumType("Boolean", Set("True", "False")), "False")))
  }

  test("synthesisEnv should handle option-like enum types") {
    val typeDefs = List(
      ("Maybe", Type.EnumType("Maybe", Set("None", "Some")))
    )
    val exprDefs = List(
      ("nothing", Expr.EnumLit("Maybe", "None")),
      ("something", Expr.EnumLit("Maybe", "Some"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("nothing")) should equal(Some(Value.EnumVal(Type.EnumType("Maybe", Set("None", "Some")), "None")))
    env.getValue(Identifier.Symbol("something")) should equal(Some(Value.EnumVal(Type.EnumType("Maybe", Set("None", "Some")), "Some")))
  }

  test("synthesisEnv should preserve enum type information") {
    val typeDefs = List(
      ("TrafficLight", Type.EnumType("TrafficLight", Set("Red", "Yellow", "Green")))
    )
    val exprDefs = List(
      ("light1", Expr.EnumLit("TrafficLight", "Red")),
      ("light2", Expr.EnumLit("TrafficLight", "Green"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    val light1Value = env.getValue(Identifier.Symbol("light1"))
    val light2Value = env.getValue(Identifier.Symbol("light2"))
    
    light1Value should equal(Some(Value.EnumVal(Type.EnumType("TrafficLight", Set("Red", "Yellow", "Green")), "Red")))
    light2Value should equal(Some(Value.EnumVal(Type.EnumType("TrafficLight", Set("Red", "Yellow", "Green")), "Green")))
  }

  test("synthesisEnv should handle enum values mixed with other types") {
    val typeDefs = List(
      ("Status", Type.EnumType("Status", Set("Active", "Inactive"))),
      ("Count", Type.IntType)
    )
    val exprDefs = List(
      ("status", Expr.EnumLit("Status", "Active")),
      ("count", Expr.IntLit(42)),
      ("name", Expr.StringLit("test"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("status")) should equal(Some(Value.EnumVal(Type.EnumType("Status", Set("Active", "Inactive")), "Active")))
    env.getValue(Identifier.Symbol("count")) should equal(Some(Value.IntVal(42)))
    env.getValue(Identifier.Symbol("name")) should equal(Some(Value.StringVal("test")))
  }

  test("lookup should return correct enum literal expression") {
    val typeDefs = List()
    val exprDefs = List(
      ("priority1", Expr.EnumLit("Priority", "High")),
      ("priority2", Expr.EnumLit("Priority", "Low"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("priority1") should equal(Expr.EnumLit("Priority", "High"))
    nameSpace.lookup("priority2") should equal(Expr.EnumLit("Priority", "Low"))
  }

  test("synthesisEnv should handle empty variant sets (edge case)") {
    val typeDefs = List(
      ("Empty", Type.EnumType("Empty", Set()))
    )
    val exprDefs = List()
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getType(Identifier.Symbol("Empty")) should equal(Some(Type.EnumType("Empty", Set())))
  }

  test("synthesisEnv should handle enum types with numeric-like variant names") {
    val typeDefs = List(
      ("ErrorCode", Type.EnumType("ErrorCode", Set("Error404", "Error500", "Error403")))
    )
    val exprDefs = List(
      ("notFound", Expr.EnumLit("ErrorCode", "Error404")),
      ("serverError", Expr.EnumLit("ErrorCode", "Error500"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    env.getValue(Identifier.Symbol("notFound")) should equal(Some(Value.EnumVal(Type.EnumType("ErrorCode", Set("Error404", "Error500", "Error403")), "Error404")))
    env.getValue(Identifier.Symbol("serverError")) should equal(Some(Value.EnumVal(Type.EnumType("ErrorCode", Set("Error404", "Error500", "Error403")), "Error500")))
  }

  test("synthesisEnv should maintain enum type consistency across multiple definitions") {
    val typeDefs = List(
      ("Mode", Type.EnumType("Mode", Set("Read", "Write", "Execute")))
    )
    val exprDefs = List(
      ("mode1", Expr.EnumLit("Mode", "Read")),
      ("mode2", Expr.EnumLit("Mode", "Write")),
      ("mode3", Expr.EnumLit("Mode", "Execute"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    val env = nameSpace.synthesisEnv
    
    // All should be of the same enum type
    env.getValue(Identifier.Symbol("mode1")).get should equal(Value.EnumVal(Type.EnumType("Mode", Set("Read", "Write", "Execute")), "Read"))
    env.getValue(Identifier.Symbol("mode2")).get should equal(Value.EnumVal(Type.EnumType("Mode", Set("Read", "Write", "Execute")), "Write"))
    env.getValue(Identifier.Symbol("mode3")).get should equal(Value.EnumVal(Type.EnumType("Mode", Set("Read", "Write", "Execute")), "Execute"))
  }

  test("lookup multiple times on enum expressions should return same result") {
    val typeDefs = List()
    val exprDefs = List(
      ("state", Expr.EnumLit("State", "Running"))
    )
    
    val nameSpace = new SyntaxNameSpacePhantom(typeDefs, exprDefs)
    
    nameSpace.lookup("state") should equal(nameSpace.lookup("state"))
    nameSpace.lookup("state") should equal(nameSpace.lookup("state"))
  }
}
