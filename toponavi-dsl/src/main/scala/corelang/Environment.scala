package corelang

trait Identified[T]

trait Typed[Id, T <: Identified[Id]] extends Identified[Id] {
  def infer(using env: ImmutEnvironment[Id, T, Typed[Id, T]]): T
}

trait ImmutTypeEnvironment[Id, +T <: Identified[Id]] {
  def types: Map[Id, T]

  def ty(id: Id): T = types(id)

  def getType(id: Id): Option[T] = types.get(id)

  def merge[T1 >: T <: Identified[Id]](other: ImmutTypeEnvironment[Id, T1]): ImmutTypeEnvironment[Id, T1]
}

trait TypeEnvironment[Id, T <: Identified[Id]] extends ImmutTypeEnvironment[Id, T] { self =>

  type Self <: TypeEnvironment[Id, T]

  def addTypeVar[T1 <: T](id: Id, ty: T1): Self

  def merge(other: Self): Self

  def withTypeVar[T1 <: T, R](id: Id, ty: T1)(f: Self => R): R = f(addTypeVar(id, ty))

  def withTypeVars[T1 <: T, R](vars: Map[Id, T1])(f: Self => R): R = {
    f(vars.foldLeft(this.asInstanceOf[Self]) { case (env, (id, ty)) => env.addTypeVar(id, ty).asInstanceOf[Self] })
  }

  def withTypeVars[T1 <: T, R](vars: (Id, T1)*)(f: Self => R): R = {
    withTypeVars(vars.toMap)(f)
  }
}

case class TypeOnlyEnvironment[Id, T <: Identified[Id]](
  override val types: Map[Id, T],
) extends TypeEnvironment[Id, T] {

  type Self = TypeOnlyEnvironment[Id, T]

  override def addTypeVar[T1 <: T](id: Id, ty: T1): TypeOnlyEnvironment[Id, T] = {
    copy(types = types + (id -> ty))
  }

  override def merge(other: TypeOnlyEnvironment[Id, T]): TypeOnlyEnvironment[Id, T] = {
    copy(types = this.types ++ other.types)
  }

  override def merge[T1 >: T <: Identified[Id]](other: ImmutTypeEnvironment[Id, T1]): ImmutTypeEnvironment[Id, T1] = {
    other match {
      case env: TypeOnlyEnvironment[Id, T1] => TypeOnlyEnvironment(this.types ++ env.types)
      case _ => TypeOnlyEnvironment(this.types ++ other.types)
    }
  }

  // Support :: operator for De Bruijn indices (assuming Id is compatible with Int)
  def ::[T1 <: T](ty: T1)(using ev: Id =:= Identifier): TypeOnlyEnvironment[Id, T] = {
    val shiftedTypes = types.map { case (id, t) =>
      id match {
        case Identifier.Index(i) => (Identifier.Index(i + 1).asInstanceOf[Id], t)
        case other => (other, t)
      }
    }
    copy(types = shiftedTypes + (Identifier.Index(0).asInstanceOf[Id] -> ty))
  }
}

object TypeEnvironment {
  def empty[Id, T <: Identified[Id]]: TypeOnlyEnvironment[Id, T] = TypeOnlyEnvironment(Map.empty)
}

trait ImmutEnvironment[Id, +T <: Identified[Id], +V <: Identified[Id]] extends ImmutTypeEnvironment[Id, T] {
  def values: Map[Id, V]

  def value(id: Id): V = values(id)

  def getValue(id: Id): Option[V] = values.get(id)

  def merge[T1 >: T <: Identified[Id], V1 >: V <: Identified[Id]](
    other: ImmutEnvironment[Id, T1, V1]
  ): ImmutEnvironment[Id, T1, V1]
}

case class Environment[Id, T <: Identified[Id], V <: Identified[Id]](
  types: Map[Id, T] = Map.empty,
  values: Map[Id, V] = Map.empty,
) extends TypeEnvironment[Id, T] with ImmutEnvironment[Id, T, V] {

  type Self = Environment[Id, T, V]

  override def value(id: Id): V = values(id)

  override def getValue(id: Id): Option[V] = values.get(id)

  def get(id: Id): Option[(V, T)] = for {
    value <- getValue(id)
    ty <- getType(id)
  } yield (value, ty)

  def apply(id: Id): (V, T) = (value(id), ty(id))

  def asImmut: ImmutEnvironment[Id, T, V] = this

  def typeEnv: TypeEnvironment[Id, T] = TypeOnlyEnvironment(types)

  def mapTypes[T1 <: Identified[Id]](f: (Id, T) => T1): Environment[Id, T1, V] = {
    copy(types = types.view.map { (id, ty) => id -> f(id, ty) }.toMap)
  }

  def mapValues[V1 <: Identified[Id]](f: (Id, V) => V1): Environment[Id, T, V1] = {
    copy(values = values.view.map { (id, term) => id -> f(id, term) }.toMap)
  }

  override def addTypeVar[T1 <: T](id: Id, ty: T1): Environment[Id, T, V] = {
    copy(types = types + (id -> ty))
  }

  def addValueVar[V1 <: V](id: Id, term: V1): Environment[Id, T, V] = {
    copy(values = values + (id -> term))
  }

  override def withTypeVar[T1 <: T, R](id: Id, ty: T1)(f: Environment[Id, T, V] => R): R = f(addTypeVar(id, ty))

  override def withTypeVars[T1 <: T, R](vars: Map[Id, T1])(f: Environment[Id, T, V] => R): R = {
    f(copy(types = types ++ vars))
  }

  override def withTypeVars[T1 <: T, R](vars: (Id, T1)*)(f: Environment[Id, T, V] => R): R = {
    f(copy(types = types ++ vars.toMap))
  }

  def withValueVar[V1 <: V, R](id: Id, term: V1)(f: Environment[Id, T, V] => R): R = {
    f(addValueVar(id, term))
  }

  def withValueVars[V1 <: V, R](vars: Map[Id, V1])(f: Environment[Id, T, V] => R): R = {
    f(copy(values = values ++ vars))
  }

  def withValueVars[V1 <: V, R](vars: (Id, V1)*)(f: Environment[Id, T, V] => R): R = {
    f(copy(values = values ++ vars.toMap))
  }

  override def merge(other: Environment[Id, T, V]): Environment[Id, T, V] = {
    Environment(
      types = this.types ++ other.types,
      values = this.values ++ other.values,
    )
  }

  override def merge[T1 >: T <: Identified[Id]](other: ImmutTypeEnvironment[Id, T1]): ImmutTypeEnvironment[Id, T1] = {
    other match {
      case env: TypeOnlyEnvironment[Id, T1] => TypeOnlyEnvironment(this.types ++ env.types)
      case env: Environment[Id, T1, _] => Environment(this.types ++ env.types, this.values)
      case _ => TypeOnlyEnvironment(this.types ++ other.types)
    }
  }

  override def merge[T1 >: T <: Identified[Id], V1 >: V <: Identified[Id]](
    other: ImmutEnvironment[Id, T1, V1]
  ): ImmutEnvironment[Id, T1, V1] = {
    other match {
      case env: Environment[Id, T1, V1] => 
        Environment(this.types ++ env.types, this.values ++ env.values)
      case _ => 
        Environment(this.types ++ other.types, this.values ++ other.values)
    }
  }

  // Support :: operator for De Bruijn indices (assuming Id is compatible with Identifier)
  def ::[V1 <: V](value: V1)(using ev: Id =:= Identifier): Environment[Id, T, V] = {
    val shiftedValues = values.map { case (id, v) =>
      id match {
        case Identifier.Index(i) => (Identifier.Index(i + 1).asInstanceOf[Id], v)
        case other => (other, v)
      }
    }
    copy(values = shiftedValues + (Identifier.Index(0).asInstanceOf[Id] -> value))
  }
}

object Environment {

  def empty[Id, T <: Identified[Id], V <: Identified[Id]]: Environment[Id, T, V] = Environment(
    types = Map.empty[Id, T],
    values = Map.empty[Id, V],
  )

  def builder[Id, T <: Identified[Id], V <: Identified[Id]]: Builder[Id, T, V] = Builder()

  class Builder[Id, T <: Identified[Id], V <: Identified[Id]] {
    private var env: Environment[Id, T, V] = Environment.empty

    def typeVar(id: Id, ty: T): Builder[Id, T, V] = {
      env = env.addTypeVar(id, ty)
      this
    }

    def valueVar(id: Id, term: V): Builder[Id, T, V] = {
      env = env.addValueVar(id, term)
      this
    }

    def build(): Environment[Id, T, V] = env

    def buildWith(f: Builder[Id, T, V] => Unit): Environment[Id, T, V] = {
      f(this)
      this.build()
    }
  }
}

