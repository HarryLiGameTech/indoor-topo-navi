import topomap.grammar.{MapFileBaseVisitor, MapFileParser}

class MapFileVisitor extends MapFileBaseVisitor[Any]{
  override def visitProgram(ctx: MapFileParser.ProgramContext): Any = 114
}