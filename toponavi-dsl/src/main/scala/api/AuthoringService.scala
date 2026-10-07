package api

import org.antlr.v4.runtime.*
import topomap.grammar.{MapFileLexer, MapFileParser}

import java.util.{Map as JMap}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

// Declaration discovery does not evaluate constraints or require invocation parameters.
object AuthoringService {
  private def record(fields: (String, AnyRef)*): JMap[String, AnyRef] = fields.toMap.asJava

  def inspect(files: JMap[String, String]): JMap[String, AnyRef] = {
    val diagnostics = ListBuffer.empty[JMap[String, AnyRef]]
    val constraints = ListBuffer.empty[JMap[String, AnyRef]]
    val parameters = ListBuffer.empty[JMap[String, AnyRef]]

    def source(file: String, token: Token): JMap[String, AnyRef] = record(
      "file" -> file, "line" -> Int.box(token.getLine), "column" -> Int.box(token.getCharPositionInLine + 1))

    def parse(file: String, code: String): (MapFileParser.SurfaceDefContext, List[Token]) = {
      val listener = new BaseErrorListener {
        override def syntaxError(recognizer: Recognizer[?, ?], offendingSymbol: Object,
          line: Int, column: Int, message: String, error: RecognitionException): Unit = {
          diagnostics += record("stage" -> "parse", "message" -> message,
            "source" -> record("file" -> file, "line" -> Int.box(line), "column" -> Int.box(column + 1)))
        }
      }
      val lexer = new MapFileLexer(CharStreams.fromString(code))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val tokens = new CommonTokenStream(lexer)
      val parser = new MapFileParser(tokens)
      parser.removeErrorListeners()
      parser.addErrorListener(listener)
      while (tokens.LA(1) == MapFileParser.NL) tokens.consume()
      val tree = parser.surfaceDef()
      while (tokens.LA(1) == MapFileParser.NL) tokens.consume()
      if (tokens.LA(1) != Token.EOF) {
        diagnostics += record("stage" -> "parse", "message" -> "Unexpected content after the declaration.",
          "source" -> source(file, tokens.LT(1)))
      }
      tokens.fill()
      (tree, tokens.getTokens.asScala.toList)
    }

    val configFile = if (files.containsKey("configuration.tcfg")) "configuration.tcfg" else "configuration"
    val required = ListBuffer.empty[String]
    Option(files.get(configFile)) match {
      case Some(code) => parse(configFile, code)._1 match {
        case ctx: MapFileParser.SurfaceDefGlobalConfigExprContext =>
          ctx.globalConfigBody().globalConfigElement().asScala.foreach {
            case ref: MapFileParser.GlobalConfigElementSubmapRefContext =>
              val ids = ref.ID().asScala.map(_.getText).toList
              val base = if (Option(ref.children).exists(_.asScala.exists(_.getText == "using"))) ids.lift(1).getOrElse(ids.head) else ids.head
              required += (if (files.containsKey(base + ".tmap")) base + ".tmap" else base)
            case ref: MapFileParser.GlobalConfigElementVehicleRefContext =>
              val name = ref.ID().getText
              required += (if (files.containsKey(name + ".ttr")) name + ".ttr" else name)
            case _ => ()
          }
        case _ => diagnostics += record("stage" -> "parse", "message" -> "Expected building-includes configuration.")
      }
      case None => diagnostics += record("stage" -> "parse", "message" -> "Missing configuration.tcfg or configuration.")
    }
    if (files.containsKey("root")) required += "root"

    required.distinct.sorted.foreach { file =>
      Option(files.get(file)) match {
        case None => diagnostics += record("stage" -> "parse", "message" -> s"Missing referenced source: $file")
        case Some(code) =>
          val (tree, tokens) = parse(file, code)
          val scope = tree match {
            case ctx: MapFileParser.SurfaceDefRootExprContext =>
              Option(ctx.paramList()).foreach(_.param().asScala.foreach { param =>
                if (param.ID() != null && param.typeExpr() != null) {
                  parameters += record("name" -> param.ID().getText, "type" -> param.typeExpr().getText,
                    "source" -> source(file, param.getStart))
                }
              })
              Some(("root", ctx.ID().getText, ctx.surfaceBody()))
            case ctx: MapFileParser.SurfaceDefTopoMapExprContext =>
              Some(("map", ctx.ID(0).getText, ctx.surfaceBody()))
            case ctx: MapFileParser.SurfaceDefTransportExprContext =>
              Some(("transport", ctx.ID().getText, ctx.surfaceBody()))
            case _ => None
          }
          scope.foreach { case (kind, name, _) =>
            // Token recovery retains top-level declarations when an unfinished body cannot form a parse subtree.
            tokens.zipWithIndex.foldLeft(0) { case (depth, (token, index)) =>
              if (depth == 1 && token.getText == "constraint") {
                tokens.lift(index + 1).filter(_.getType == MapFileParser.ID).foreach { identifier =>
                  constraints += record("name" -> identifier.getText,
                    "scope" -> record("kind" -> kind, "name" -> name),
                    "source" -> source(file, token), "evaluation" -> "notEvaluated")
                }
              }
              token.getText match {
                case "{" => depth + 1
                case "}" => depth - 1
                case _ => depth
              }
            }
          }
      }
    }
    record("constraints" -> record("entries" -> constraints.toList.asJava,
      "complete" -> Boolean.box(diagnostics.isEmpty)),
      "parameters" -> parameters.toList.asJava, "diagnostics" -> diagnostics.toList.asJava)
  }
}
