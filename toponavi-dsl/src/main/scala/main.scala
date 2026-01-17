import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import topomap.grammar.{MapFileLexer, MapFileParser}
import util.catchError

@main
def main(): Unit = {
  val code =
    """
    TopoMap Test{}
    """

  val program = catchError(code.strip) { listener =>
    val stripedCode = code.strip()
    val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
    lexer.removeErrorListeners()
    lexer.addErrorListener(listener)
    val parser = MapFileParser(CommonTokenStream(lexer))
    parser.removeErrorListeners()
    parser.addErrorListener(listener)
//    MapFileVisitor().visitProgram(parser.program())
  }

  println(program)
}
