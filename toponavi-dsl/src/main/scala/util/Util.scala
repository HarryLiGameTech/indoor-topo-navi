package util

import org.antlr.v4.runtime.{BaseErrorListener, ParserRuleContext, RecognitionException, Recognizer}
import pprint.pprintln

case class SourceSpan(start: Int, end: Int) {
  def contains(other: SourceSpan): Boolean = {
    start <= other.start && end >= other.end
  }
  def overlaps(other: SourceSpan): Boolean = {
    (start < other.end && end > other.start) || (other.start < end && other.end > start)
  }
  def length: Int = end - start
  def merge(other: SourceSpan): Option[SourceSpan] = {
    if overlaps(other) || contains(other) || other.contains(this) then
      Some(SourceSpan(math.min(start, other.start), math.max(end, other.end)))
    else None
  }
  def toSourcePositions(code: String): (SourcePosition, SourcePosition) = {
    (SourcePosition.from(code, start), SourcePosition.from(code, end))
  }
}
object SourceSpan {
  def unknown: SourceSpan = SourceSpan(-1, -1)
  def fromPositions(code: String, start: SourcePosition, end: SourcePosition): SourceSpan = {
    val startPos = start.toCharPos(code)
    val endPos = end.toCharPos(code)
    SourceSpan(startPos, endPos)
  }
}
trait OptionalSpanned[T] {
  def withSpan(span: SourceSpan): T
}
case class SourcePosition(line: Int, column: Int) {
  def isEqual(other: SourcePosition): Boolean = {
    line == other.line && column == other.column
  }
  def moveBy(columns: Int): SourcePosition = {
    copy(column = column + columns)
  }
  def toCharPos(code: String): Int = {
    val lines = code.split("\n")
    var charPoint = 0
    for (i <- 0 until line - 1) {
      charPoint += lines(i).length + 1 // Add 1 for the newline character
    }
    charPoint + column
  }
}
object SourcePosition {
  def from(code: String, bytePos: Int): SourcePosition = {
    val lines = code.split('\n').foldLeft((0, 0, 0)) {
      case ((line, _, pos), currentLine) => {
        if pos + currentLine.length + 1 > bytePos then (line, bytePos - pos, pos)
        else (line + 1, 0, pos + currentLine.length + 1)
      }
    }
    SourcePosition(lines._1, lines._2)
  }
  def fromLines(code: String): List[SourcePosition] = {
    code.split('\n').zipWithIndex.flatMap { case (line, lineIndex) =>
      line.zipWithIndex.map { case (_, colIndex) => SourcePosition(lineIndex, colIndex) }
    }.toList
  }
}

trait CpError extends Exception

trait SpannedError extends CpError {
  def message: String

  def infoSpans: Map[SourceSpan, String]
}

case class UnknownError(cause: Throwable, span: SourceSpan) extends SpannedError {
  override def message: String = cause.getMessage

  override def infoSpans: Map[SourceSpan, String] = Map(span -> cause.toString)

  override def getStackTrace: Array[StackTraceElement] = cause.getStackTrace
}

case class SingleSpanError(
  kind: ErrorKind,
  info: String,
  span: SourceSpan,
) extends SpannedError {
  override def message: String = kind.message

  override def infoSpans: Map[SourceSpan, String] = Map(span -> info)
}

case class CoreError(kind: ErrorKind, info: String) extends CpError {
  def withSpan(span: SourceSpan): SpannedError = {
    val spannedError = SingleSpanError(kind, info, span)
    spannedError.setStackTrace(this.getStackTrace)
    spannedError
  }

  override def toString: String = s"Error: ${kind.message} - $info"
}

trait ErrorKind {
  def message: String
}

enum CoreErrorKind(override val message: String) extends ErrorKind {
  case Unreachable extends CoreErrorKind("Undefined behavior")
  case UnsupportedFeature extends CoreErrorKind("Unsupported feature")


  def raise(info: => String): Nothing = throw CoreError(this, info)

  def raise(span: SourceSpan)(info: => String): Nothing = throw CoreError(this, info).withSpan(span)
}

case class PanicError(message: String) extends Exception

enum SyntaxError extends Exception with SpannedError {
  override def message: String = "Syntax error"
  def info: String
  def span: SourceSpan
  override lazy val infoSpans: Map[SourceSpan, String] = Map(this.span -> info)
  override def toString: String = s"SyntaxError: $info at ${this.span}"
  case InvalidInput(override val info: String, override val span: SourceSpan)
  case InvalidOperator(override val info: String, override val span: SourceSpan)
  case InvalidSymbol(override val info: String, override val span: SourceSpan)
  case InvalidDeclaration(override val info: String, override val span: SourceSpan)
  case MissingReturnType(override val info: String, override val span: SourceSpan)
  case ParsingError(override val info: String, override val span: SourceSpan, cause: RecognitionException)
  case SpineParsingError(override val info: String, override val span: SourceSpan)
}
extension (context: ParserRuleContext) {
  def span: SourceSpan = SourceSpan(context.start.getStartIndex, context.stop.getStopIndex)
  def raiseError(errCons: (String, SourceSpan) => SyntaxError)(info: => String): Nothing = {
    val span = SourceSpan(context.start.getStartIndex, context.stop.getStopIndex)
    throw errCons(info, span)
  }
}
class ErrorListener(code: String) extends BaseErrorListener {
  override def syntaxError(
    recognizer: Recognizer[_, _],
    offendingSymbol: Any,
    line: Int, charPositionInLine: Int,
    message: String, exception: RecognitionException
  ): Unit = {
    val charPos = SourcePosition(line, charPositionInLine).toCharPos(code)
    val span = SourceSpan(charPos, charPos)
    throw SyntaxError.ParsingError("Syntax error encountered", span, exception)
  }
}

def printSourceWithHighlight(source: String, span: SourceSpan, info: String): Unit = {
  // Split source by lines for display
//  val lines = source.split("\n").zipWithIndex
  val lines = source.split("\r?\n").zipWithIndex // Windows-safe option

  // Calculate the line and character positions of the error
  val (startLine, endLine) = {
    val (start, end) = (span.start, span.end)
    val startLine = source.substring(0, start).count(_ == '\n')
    val endLine = source.substring(0, end).count(_ == '\n')
    println(f"Error at lines $startLine to $endLine:\n")
    (startLine, endLine)
  }
  // Print each line, and highlight the range containing the error
  lines.foreach {
    case (line, idx) if idx >= startLine && idx <= endLine => {
      println(f"$idx%4d: ${line.stripLineEnd}")
      // Highlight the error within the line
      if (idx == startLine) {
        val highlightStart = span.start - source.substring(0, span.start).lastIndexOf('\n') - 1
        val highlightEnd = if (startLine == endLine) span.end - span.start + highlightStart else line.length
        println(" " * (highlightStart + 6) + "^" * (highlightEnd - highlightStart + 1) + " " + info)
      }
    }
    case (line, idx) => println(f"$idx%4d: $line")
  }
}

def catchError[R](source: String)(action: ErrorListener => R): R = {
  val errorListener = ErrorListener(source)
  try action(errorListener) catch {
    case error: SpannedError => {
      error.infoSpans.headOption match {
        case Some(span, info) => {
          println(s"Error: ${error.message}")
          printSourceWithHighlight(source, span, info)
        }
        case None => println(s"Error: ${error.message}")
      }
      // Rethrow the original Error after showing it
      throw error
    }
  }
}
