package compiler

import util.catchError
import syntax.TopoMapVisitor
import surfacelang.{GlobalConfigExpr, TopoEnvironment}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import data.{ElevatorBank, LinearTransport, NavigationGraph, TransportGraph}
import corelang.{Environment, Value}
import enums.ElevatorStationCategory.{Entrance, Occupant}
import surfacelang.{TopoMapValue, TransportValue}
import pprint.pprintln

import topomap.grammar.{MapFileLexer, MapFileParser}

import java.io.File
import scala.collection.mutable
import java.util.Map as JMap
import scala.jdk.CollectionConverters.*

class TopoScriptCompiler() {

  private val metadata = new CompilerMetadataContext()

  def compile(targetDirectory: String): CompilationResult = {
    // 1. Parse Global Config
    val configFile = new File(targetDirectory, "configuration.tcfg")
    val configFileNoExt = new File(targetDirectory, "configuration")
    val validConfigFile = if (configFile.exists()) configFile else configFileNoExt

    if (!validConfigFile.exists()) {
      throw new RuntimeException(s"Configuration file not found at: ${configFile.getAbsolutePath} (or without extension)")
    }
    
    val globalConfig = parseConfigFile(scala.io.Source.fromFile(validConfigFile).mkString) match {
      case config: GlobalConfigExpr => config
      case _ => throw new RuntimeException("Global config parsing did not return a GlobalConfigExpr")
    }
    
    // 2. Parse and Elaborate each topo-map file
    val topoMapFiles = globalConfig.submaps.map(_.name)
    val elaboratedMaps = mutable.Map[String, TopoMapValue]()
    
    for (mapName <- topoMapFiles) {
      val mapFile = new File(targetDirectory, mapName + ".tmap")
      // If .tm doesn't exist, try without extension
      val fileToRead = if (mapFile.exists()) mapFile else new File(targetDirectory, mapName)
      
      if (!fileToRead.exists()) {
         println(s"${Console.YELLOW}Warning: Map file not found: ${mapFile.getAbsolutePath} (or without extension)${Console.RESET}")
      } else {
        val mapCode = scala.io.Source.fromFile(fileToRead).mkString
        val topoMapExpr = parseMapFile(mapCode)
        val elaboratedMap = topoMapExpr.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty, Map.empty))
        elaboratedMaps.put(mapName, elaboratedMap)
      }
    }

    // 3. Parse each transport files, and link them with the topo-maps
    val transFiles = globalConfig.vehicles.map(_.name)
    val elaboratedTransports = mutable.ListBuffer[TransportValue]()
    
    // Create a base environment with common definitions like "Elevator"
//    val baseValues = Map(
//       corelang.Identifier.Symbol("Elevator") -> Value.RecordVal(Map(
//         "maxVelocity" -> Value.FloatVal(2.5), // Defaults
//         "acceleration" -> Value.FloatVal(1.0),
//         "capacity" -> Value.IntVal(21),
//         "duty" -> Value.IntVal(1600)
//       ))
//    )
//    val baseEnv = Environment[corelang.Identifier, corelang.Type, Value](Map.empty, baseValues)
    
    val topoEnvForTransport = TopoEnvironment(Environment.empty, Map.empty, Map.empty, elaboratedMaps.toMap)

    for (transName <- transFiles) {
      val transFile = new File(targetDirectory, transName + ".ttr")
      val fileToRead = if (transFile.exists()) transFile else new File(targetDirectory, transName)
      if (!fileToRead.exists()) {
         println(s"${Console.YELLOW}Warning: Transport file not found: ${transFile.getAbsolutePath} (or without extension)${Console.RESET}")
      } else {
        val transCode = scala.io.Source.fromFile(fileToRead).mkString
        val transExpr = parseTransportFile(transCode)
        val elaboratedTransport = transExpr.elaborate(using topoEnvForTransport)
        elaboratedTransports += elaboratedTransport
      }
    }
    pprintln(elaboratedTransports)

    // 4. Convert to toponavi-core Data Structures
    val navigationGraphs = elaboratedMaps.map { case (name, mapVal) =>
      name -> buildNavigationGraph(mapVal)
    }.toMap

    val linearTransports = elaboratedTransports.map { transVal =>
      buildLinearTransport(transVal, navigationGraphs)
    }.toList

    val transportGraph = TransportGraph(linearTransports)

    println("=== navigationGraphs ===")
    pprintln(navigationGraphs)
    println("=== linearTransports ===")
    pprintln(linearTransports)
    println("=== transportGraph ===")
    pprintln(transportGraph)

    CompilationResult(navigationGraphs, transportGraph)
  }

  // Java-friendly overload
  def compileProject(files: JMap[String, String]): CompilationResult = {
    // Convert Java Map to Scala Map
    val scalaFiles = files.asScala.toMap

    // Reuse logic (or implement here) to parse config from string content
    val configContent = scalaFiles.getOrElse("configuration.tcfg",
      scalaFiles.getOrElse("configuration", throw new RuntimeException("Missing configuration.tcfg")))

    val globalConfig = parseConfigFile(configContent) match {
      case config: GlobalConfigExpr => config
      case _ => throw new RuntimeException("Global config parsing failed")
    }

    // 2. Parse and Elaborate each topo-map file
    val topoMapFiles = globalConfig.submaps.map(_.name)
    val elaboratedMaps = mutable.Map[String, TopoMapValue]()

    for (mapName <- topoMapFiles) {
      val mapCodeOption = scalaFiles.get(mapName + ".tmap").orElse(scalaFiles.get(mapName))
      
      if (mapCodeOption.isEmpty) {
        println(s"${Console.YELLOW}Warning: Map file not found: ${mapName}.tmap (or without extension)${Console.RESET}")
      } else {
        val mapCode = mapCodeOption.get
        val topoMapExpr = parseMapFile(mapCode)
        val elaboratedMap = topoMapExpr.elaborate(using TopoEnvironment(Environment.empty, Map.empty, Map.empty, Map.empty))
        elaboratedMaps.put(mapName, elaboratedMap)
      }
    }

    // 3. Parse each transport files, and link them with the topo-maps
    val transFiles = globalConfig.vehicles.map(_.name)
    val elaboratedTransports = mutable.ListBuffer[TransportValue]()

    // Create a base environment with common definitions like "Elevator"
    //    val baseValues = Map(
    //       corelang.Identifier.Symbol("Elevator") -> Value.RecordVal(Map(
    //         "maxVelocity" -> Value.FloatVal(2.5), // Defaults
    //         "acceleration" -> Value.FloatVal(1.0),
    //         "capacity" -> Value.IntVal(21),
    //         "duty" -> Value.IntVal(1600)
    //       ))
    //    )
    //    val baseEnv = Environment[corelang.Identifier, corelang.Type, Value](Map.empty, baseValues)

    val topoEnvForTransport = TopoEnvironment(Environment.empty, Map.empty, Map.empty, elaboratedMaps.toMap)

    for (transName <- transFiles) {
      val transCodeOption = scalaFiles.get(transName + ".ttr").orElse(scalaFiles.get(transName))

      if (transCodeOption.isEmpty) {
        println(s"${Console.YELLOW}Warning: Transport file not found: ${transName}.ttr (or without extension)${Console.RESET}")
      } else {
        val transCode = transCodeOption.get
        val transExpr = parseTransportFile(transCode)
        val elaboratedTransport = transExpr.elaborate(using topoEnvForTransport)
        elaboratedTransports += elaboratedTransport
      }
    }
    pprintln(elaboratedTransports)

    // 4. Convert to toponavi-core Data Structures
    val navigationGraphs = elaboratedMaps.map { case (name, mapVal) =>
      name -> buildNavigationGraph(mapVal)
    }.toMap

    val linearTransports = elaboratedTransports.map { transVal =>
      buildLinearTransport(transVal, navigationGraphs)
    }.toList

    val transportGraph = TransportGraph(linearTransports)

    println("=== navigationGraphs ===")
    pprintln(navigationGraphs)
    println("=== linearTransports ===")
    pprintln(linearTransports)
    println("=== transportGraph ===")
    pprintln(transportGraph)

    // Return result
    // Note: ensure CompilationResult and its fields are accessible to Java 
    // (Case classes work fine, but Java sees them as normal classes with getters)
    CompilationResult(navigationGraphs, transportGraph)
  }
  
  def parseConfigFile(rawCode: String): Any = {
    val stripedCode = rawCode.strip()
    catchError(stripedCode) { listener =>
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefGlobalConfigExprContext =>
          new TopoMapVisitor().visitSurfaceDefGlobalConfigExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type. Need Global Config here!")
      }
    }
  }

  def parseMapFile(rawCode: String): surfacelang.SubTopoMapExpr = {
    val stripedCode = rawCode.strip()
    catchError(stripedCode) { listener =>
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefTopoMapExprContext =>
          new TopoMapVisitor().visitSurfaceDefTopoMapExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type. Need Topo Map here!")
      }
    }
  }

  def parseTransportFile(rawCode: String): surfacelang.TransportExpr = {
      catchError(rawCode.strip) { listener =>
      val stripedCode = rawCode.strip()
      val lexer = MapFileLexer(CharStreams.fromString(stripedCode))
      lexer.removeErrorListeners()
      lexer.addErrorListener(listener)
      val parser = MapFileParser(CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(listener)

      val surface = parser.surfaceDef()
      surface match {
        case ctx: MapFileParser.SurfaceDefTransportExprContext =>
          new TopoMapVisitor().visitSurfaceDefTransportExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type. Need Transport here!")
      }
    }
  }

  private def buildNavigationGraph(mapVal: TopoMapValue): NavigationGraph = {
    val paramsOption = mapVal.context.values.get(corelang.Identifier.Symbol("params"))
    val d = paramsOption match {
      case Some(Value.RecordVal(fields)) => fields
      case Some(_) => throw new RuntimeException(s"Symbol 'params' in transport ${mapVal.name} must be a RecordVal")
      case None => throw new RuntimeException("Must contain a 'params' record in topo-map context")
    }

    val floorPopulation = d.get("permResident").map {
      case Value.IntVal(v) => v.toInt
      case _ => throw new RuntimeException("permResident must be an Int in topo-map params")
    }


    // 1. Convert Nodes
    val coreNodes = mapVal.nodes.map { nodeVal =>
      nodeVal -> data.TopoNode(
        identifier = nodeVal.name,
        attributes = convertAttributes(nodeVal.data)
      )
    }.toMap

    // 2. Convert Paths
    val corePaths = mapVal.paths.flatMap { pathVal =>
       val source = coreNodes(pathVal.from)
       val target = coreNodes(pathVal.to)
       val attrs = convertAttributes(pathVal.data)
       // Extract costs if available in attributes? 
       // Currently AtomicPath takes Map[VisitingMode, Double] for costs.
       // Assuming 'cost' field in DSL data maps to cost.
       val cost = pathVal.data.fields.get("cost") match {
         case Some(Value.FloatVal(v)) => v
         case Some(Value.IntVal(v)) => v.toDouble
         case _ => throw RuntimeException("Must contain a 'cost' field of type int or float in path data")
       }
       // TODO: Refactor?
       val costs = Map(
         enums.VisitingMode.Normal -> cost,
         enums.VisitingMode.Emergency -> cost * 0.5,
         enums.VisitingMode.Prioritized -> cost * 0.7,
         enums.VisitingMode.Wheeled -> cost * 2.0
       )
       
       val forward = data.AtomicPath(source, target, attrs, costs, enums.PathType.General)
       
       if (pathVal.bidirectional) {
          val backward = data.AtomicPath(target, source, attrs, costs, enums.PathType.General)
          List(forward, backward)
       } else {
          List(forward)
       }
    }.toList
    
    // 3. Build Reverse Adjacency
    val reverseAdj = corePaths.groupBy(_.target)

    val graph = NavigationGraph(
      identifier = mapVal.name,
      nodes = coreNodes.values.toList,
      adjacencyList = corePaths,
      reverseAdjacency = reverseAdj
    )

    // Append this population data into the CompilerMetadataContext
    floorPopulation.foreach { pop =>
      metadata.floorPopulation.put(graph, pop)
    }

    graph
  }

  private def buildLinearTransport(transVal: TransportValue, graphs: Map[String, NavigationGraph]): LinearTransport = {
     // Retrieve 'params' from the context, which is expected to be a RecordVal
     val paramsOption = transVal.context.values.get(corelang.Identifier.Symbol("params"))

     val d = paramsOption match {
       case Some(Value.RecordVal(fields)) => fields
       case Some(_) => throw new RuntimeException(s"Symbol 'params' in transport ${transVal.name} must be a RecordVal")
       case None => throw new RuntimeException("Must contain a 'params' record in transport context for transport data")
     }

     val maxV = d.get("maxVelocity").map { case Value.FloatVal(v) => v; case Value.IntVal(v) => v.toDouble; case _ => throw RuntimeException("maxVelocity must be either Int or Float") }.getOrElse(2.5)
     val acc = d.get("acceleration").map { case Value.FloatVal(v) => v; case Value.IntVal(v) => v.toDouble; case _ => throw RuntimeException("acceleration must be either Int or Float") }.getOrElse(0.8)
     val duty = d.get("duty").map { case Value.IntVal(v) => v.toInt; case _ => throw RuntimeException("duty must be Int") }.getOrElse(1000)
     val cap = d.get("capacity").map { case Value.IntVal(v) => v.toInt; case _ => throw RuntimeException("capacity must be Int") }.getOrElse(13)
     val carAmount = d.get("carAmount").map { case Value.IntVal(v) => v.toInt; case _ => throw RuntimeException("carAmount must be Int") }.getOrElse(1)

     val stations = transVal.stations.map { case (nodeRef, stationData) =>
       val loopGraph = graphs(nodeRef.fromMapName)
       val loopNode = loopGraph.nodes.find(n => n.identifier == nodeRef.nodeName)
         .getOrElse(throw new RuntimeException(s"Node not found in Core Graph: ${nodeRef.nodeName}"))
       (loopGraph, loopNode)
     }.toMap

     val locations = transVal.stations.map { case (nodeRef, stationData) =>
       val loopGraph = graphs(nodeRef.fromMapName)
       val loc = stationData.fields.get("location") match {
         case Some(Value.FloatVal(v)) => v
         case Some(Value.IntVal(v)) => v.toDouble
         case _ => throw RuntimeException("location must be specified as an int or float for each station")
       }
       (loopGraph, loc)
     }.toMap


     val depRates = transVal.stations.map { case (nodeRef, stationData) =>
       val loopGraph = graphs(nodeRef.fromMapName)
       val depRate = stationData.fields.get("departureRate") match {
         case Some(Value.FloatVal(v)) => v
         case _ => throw RuntimeException("departureRate must be specified as a float for each station") // TODO: Only applicable for elevator
       }
       (loopGraph, depRate)
     }.toMap

    // TODO: Awaiting constraint system to determine permissions properly. For now, default to FullyGranted for all graphs.
    // We need permissions only for station graphs
     val stPermissions = stations.keys.map { g =>
         g -> enums.TransportServicePermission.FullyGranted
     }.toMap

    // TODO: Refactor with the stationCategories enum values in TopoScript
     val categories = if (locations.isEmpty) Map.empty else {
       val entranceStation = locations.minBy(_._2)._1
       locations.keys.map { g =>
         if (g == entranceStation) g -> Entrance
         else g -> Occupant
       }.toMap
     }

     ElevatorBank(
       identifier = transVal.name,
       stationNodes = stations,
       stationLocations = locations,
       stationCategories = categories, // Make one pair with value Entrance and others Occupant
       stationPermissions = stPermissions,
       stationPopulations = stations.keys.map { g =>
         g -> metadata.floorPopulation.getOrElse(g, 1)
       }.toMap,
       departureRate = depRates,
       maxVelocity = maxV,
       acceleration = acc,
       carAmount = carAmount,
       capacity = cap,
       duty = duty
     )
  }

  private def convertAttributes(record: Value.RecordVal): Map[String, enums.AttributeValue] = {
    record.fields.map { case (k, v) =>
      val attrVal = v match {
        case Value.IntVal(n) => enums.AttributeValue.IntValue(n.toInt)
        case Value.FloatVal(n) => enums.AttributeValue.DoubleValue(n)
        case Value.BoolVal(b) => enums.AttributeValue.BoolValue(b)
        case Value.StringVal(s) => enums.AttributeValue.StringValue(s)
        case _ => enums.AttributeValue.StringValue(v.toString)
      }
      k -> attrVal
    }
  }

}

class CompilerMetadataContext() {
  val floorPopulation: mutable.Map[NavigationGraph, Int] = mutable.Map.empty
  // Sth else in the future
}
