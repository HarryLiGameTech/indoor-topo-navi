package corelang
import scala.util.control.TailCalls.*

type Env = Map[Int, Value]

// Utility functions
object util {
  extension [A](xs: List[TailRec[A]]) {
    def sequence: TailRec[List[A]] = {
      xs match {
        case Nil => done(Nil)
        case h :: t => for {
          hd <- h
          tl <- t.sequence
        } yield hd :: tl
      }
    }
  }
}

import util._

// Utility: shift all env indices by +1 (De Bruijn "lift") and insert arg at index 0
extension (env: Env) def extend(arg: Value): Env = {
  val shifted = env.iterator.map { case (idx, value) => (idx + 1) -> value }.toMap
  shifted + (0 -> arg)
}



// This version prevents stack-overflow
object Interpreter {

  def name: String = "Trampoline Interpreter"

  def eval(term: Term)(using env: Env): Value = {
    evalTramp(term)(using env).result
  }

  private def evalTramp(term: Term)(using env: Map[Int, Value]): TailRec[Value] = term match {

    case Term.Var(index) =>
      env(index) match {
        case Value.FixThunk(annotatedType, body, captured) =>
          // When a FixThunk is looked up, evaluate its body in the captured environment
          //  and since we cannot have self-reference when building the captured env,
          //  we now put a simple thunk at index 0 that will evaluate to the fixpoint
          tailcall(evalTramp(body)(using captured + (0 -> env(index).asInstanceOf[Value.FixThunk])))
        case value => done(value)
      }

    case Term.Lam(parameterType, body) =>
      done(Value.Closure(env, body))

    case Term.App(leftTerm, rightTerm) => for {
      leftValue <- evalTramp(leftTerm)
      rightValue <- evalTramp(rightTerm)
      result <- leftValue match {
        case Value.Closure(closureEnv, body) =>
          val newEnv = closureEnv.map { case (i, v) => (i + 1) -> v } + (0 -> rightValue)
          tailcall(evalTramp(body)(using newEnv))
        case other => throw new RuntimeException(s"Runtime type error: expected closure, got $other")
      }
    } yield result

    case Term.IntLit(n) => done(Value.IntVal(n))
    case Term.FloatLit(v) => done(Value.FloatVal(v))
    case Term.BoolLit(b) => done(Value.BoolVal(b))
    case Term.StringLit(s) => done(Value.StringVal(s))
    case Term.ListLit(tpe, elements) => for {
      evaluatedElements <- elements.map(evalTramp(_)).sequence
    } yield Value.ListVal(tpe.getOrElse(evaluatedElements.head.toTerm.infer()), evaluatedElements)

    case Term.BinOp(kind, leftTerm, rightTerm) =>
      for {
        leftValue <- evalTramp(leftTerm)
        rightValue <- evalTramp(rightTerm)
      } yield (kind, leftValue, rightValue) match {
        case (OpKind.Add, Value.FloatVal(l), Value.FloatVal(r)) => Value.FloatVal(l + r)
        case (OpKind.Sub, Value.FloatVal(l), Value.FloatVal(r)) => Value.FloatVal(l - r)
        case (OpKind.Mul, Value.FloatVal(l), Value.FloatVal(r)) => Value.FloatVal(l * r)
        case (OpKind.Eq, Value.FloatVal(l), Value.FloatVal(r)) => Value.BoolVal(l == r)
        case (OpKind.Lt, Value.FloatVal(l), Value.FloatVal(r)) => Value.BoolVal(l < r)
        case (OpKind.Gt, Value.FloatVal(l), Value.FloatVal(r)) => Value.BoolVal(l > r)
        // Integer operations
        case (OpKind.Add, Value.IntVal(l), Value.IntVal(r)) => Value.IntVal(l + r)
        case (OpKind.Sub, Value.IntVal(l), Value.IntVal(r)) => Value.IntVal(l - r)
        case (OpKind.Mul, Value.IntVal(l), Value.IntVal(r)) => Value.IntVal(l * r)
        case (OpKind.Eq, Value.IntVal(l), Value.IntVal(r)) => Value.BoolVal(l == r)
        case (OpKind.Lt, Value.IntVal(l), Value.IntVal(r)) => Value.BoolVal(l < r)
        case (OpKind.Gt, Value.IntVal(l), Value.IntVal(r)) => Value.BoolVal(l > r)
        // Other operations
        case (OpKind.Eq, Value.BoolVal(l), Value.BoolVal(r)) => Value.BoolVal(l == r)
        case (OpKind.Concat, Value.StringVal(l), Value.StringVal(r)) => Value.StringVal(l ++ r)
        case (OpKind.Concat, Value.ListVal(t1, l), Value.ListVal(t2, r)) if t1 == t2 => Value.ListVal(t1, l ++ r)
        case _ => throw new RuntimeException("Interpreter: Binary operation on incompatible types")
      }

    case Term.If(cond, thenBranch, elseBranch) => for {
      condValue <- evalTramp(cond)
      result <- condValue match {
        case Value.BoolVal(true) => tailcall(evalTramp(thenBranch))
        case Value.BoolVal(false) => tailcall(evalTramp(elseBranch))
        case _ => throw new RuntimeException("If condition must be boolean")
      }
    } yield result

    case Term.Fix(annotatedType, body) =>
      // Y-combinator approach: put a simple thunk at index 0
      // When it's looked up later, it will be evaluated in the environment where the lookup happens
      lazy val newEnv = env.map { case (i, v) => (i + 1) -> v } + (0 -> fixThunk)
      lazy val fixThunk = Value.FixThunk(annotatedType, body, env.map { case (i, v) => (i + 1) -> v })
      tailcall(evalTramp(body)(using newEnv))

    case Term.Record(fields) =>
      def evalFields(remaining: List[(String, Term)], acc: Map[String, Value]): TailRec[Value] = {
        remaining match {
          case Nil => done(Value.RecordVal(acc))
          case (name, term) :: rest => for {
            value <- evalTramp(term)
            result <- tailcall(evalFields(rest, acc + (name -> value)))
          } yield result
        }
      }
      tailcall(evalFields(fields.toList, Map.empty))

    case Term.Proj(record, field) =>
      for {
        recordValue <- evalTramp(record)
      } yield recordValue match {
        case Value.RecordVal(fields) =>
          fields.getOrElse(field, throw new RuntimeException(s"Field '$field' not found in record"))
        case _ => throw new RuntimeException("Select operation on non-record value")
      }
  }
}