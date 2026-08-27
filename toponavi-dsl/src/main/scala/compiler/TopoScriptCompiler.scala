package compiler

import util.catchError
import syntax.TopoMapVisitor
import surfacelang.{GlobalConfigExpr, RootExpr, TopoEnvironment}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import data.{ElevatorBank, Escalator, LinearTransport, NavigationGraph, StairCase, TransportGraph}
import corelang.{Environment, Identifier, Value}
import enums.AttributeValue
import enums.ElevatorStationCategory.{Entrance, Occupant}
import surfacelang.{TopoMapValue, TransportValue}
import pprint.pprintln
import reasoner.CoordEstimator
import topomap.grammar.{MapFileLexer, MapFileParser}

import java.io.File
import scala.collection.mutable
import java.util.Map as JMap
import scala.jdk.CollectionConverters.*

class TopoScriptCompiler() {

  private val metadata = new CompilerMetadataContext()

  def compile(targetDirectory: String, params: Map[String, Value] = Map.empty): CompilationResult = {
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
    validateConfiguredManagementDomains(globalConfig)

    // Populate the global SubmapRefRegistry from the parsed config
    metadata.submapRefRegistry = SubmapRefRegistry(globalConfig.submapUsages)

    // 1b. Parse and elaborate the root file (declares params, constraints, shared defs).
    //     Its elaborated context becomes the base Env for all submaps and transports.
    val rootFile = new File(targetDirectory, "root")
    val rootEnv = if (rootFile.exists()) {
      val rootCode = scala.io.Source.fromFile(rootFile).mkString
      val rootExpr = parseRootFile(rootCode)
      val paramEnv = params.foldLeft(Environment.empty[Identifier, corelang.Type, Value]) { case (acc, (k, v)) =>
        acc.addValueVar(Identifier.Symbol(k), v)
      }
      rootExpr.elaborate(using TopoEnvironment(paramEnv, Map.empty, Map.empty, Map.empty)).context
    } else {
      // No root file — build the base env directly from the supplied params
      params.foldLeft(Environment.empty[Identifier, corelang.Type, Value]) { case (acc, (k, v)) =>
        acc.addValueVar(Identifier.Symbol(k), v)
      }
    }

    // 2. Parse and Elaborate each topo-map file
    val topoMapFiles = globalConfig.submaps.map(_.name)
    val elaboratedMaps = mutable.Map[String, TopoMapValue]()
    
    for (mapName <- topoMapFiles) {
      val mapFile = new File(targetDirectory, mapName + ".tmap")
      val fileToRead = if (mapFile.exists()) mapFile else new File(targetDirectory, mapName)
      
      if (!fileToRead.exists()) {
         println(s"${Console.YELLOW}Warning: Map file not found: ${mapFile.getAbsolutePath} (or without extension)${Console.RESET}")
      } else {
        val mapCode = scala.io.Source.fromFile(fileToRead).mkString
        val topoMapExpr = parseMapFile(mapCode)
        val elaboratedMap = topoMapExpr.elaborate(using TopoEnvironment(rootEnv, Map.empty, Map.empty, Map.empty))
        elaboratedMaps.put(mapName, elaboratedMap)
      }
    }

    // 2b. Also parse and elaborate any base maps referenced via "using" that were not
    //     listed as standalone submaps.
    for (baseRef <- metadata.submapRefRegistry.submapUsages.keys if !elaboratedMaps.contains(baseRef.name)) {
      val baseMapFile = new File(targetDirectory, baseRef.name + ".tmap")
      val fileToRead  = if (baseMapFile.exists()) baseMapFile else new File(targetDirectory, baseRef.name)
      if (!fileToRead.exists()) {
        println(s"${Console.YELLOW}Warning: Base map file not found: ${baseMapFile.getAbsolutePath} (or without extension)${Console.RESET}")
      } else {
        val mapCode     = scala.io.Source.fromFile(fileToRead).mkString
        val topoMapExpr = parseMapFile(mapCode)
        val elaboratedMap = topoMapExpr.elaborate(using TopoEnvironment(rootEnv, Map.empty, Map.empty, Map.empty))
        elaboratedMaps.put(baseRef.name, elaboratedMap)
      }
    }

    // 3. Parse each transport file and link with the topo-maps
    val transFiles = globalConfig.vehicles.map(_.name)
    val elaboratedTransports = mutable.ListBuffer[TransportValue]()

    val derivedMaps: Map[String, TopoMapValue] = metadata.submapRefRegistry.submapUsages.flatMap {
      case (baseRef, userNames) =>
        elaboratedMaps.get(baseRef.name) match {
          case Some(baseMapVal) =>
            userNames.map { userName => userName -> baseMapVal.copy(name = userName) }
          case None => List.empty
        }
    }.toMap

    val allElaboratedMaps = elaboratedMaps.toMap ++ derivedMaps
    val topoEnvForTransport = TopoEnvironment(rootEnv, Map.empty, Map.empty, allElaboratedMaps)
    val effectiveManagementDomains = resolveEffectiveManagementDomains(globalConfig, allElaboratedMaps)

    for (transName <- transFiles) {
      val transFile = new File(targetDirectory, transName + ".ttr")
      val fileToRead = if (transFile.exists()) transFile else new File(targetDirectory, transName)
      if (!fileToRead.exists()) {
        println(s"${Console.YELLOW}Warning: Transport file not found: ${transFile.getAbsolutePath} (or without extension)${Console.RESET}")
      } else {
        println(s"Parsing transport file: ${fileToRead.getAbsolutePath}")
        val transCode = scala.io.Source.fromFile(fileToRead).mkString
        val transExpr = parseTransportFile(transCode)
        val elaboratedTransport = transExpr.elaborate(using topoEnvForTransport)
        elaboratedTransports += elaboratedTransport
      }
    }
    pprintln(elaboratedTransports)

    // 4. Convert to toponavi-core Data Structures
    val baseNavigationGraphs = elaboratedMaps.map { case (name, mapVal) =>
      name -> buildNavigationGraph(mapVal)
    }.toMap

    val navigationGraphs = baseNavigationGraphs ++ metadata.submapRefRegistry.submapUsages.flatMap {
      case (baseRef, userNames) =>
        baseNavigationGraphs.get(baseRef.name) match {
          case Some(baseGraph) =>
            userNames.map { userName =>
              val derivedGraph = NavigationGraph(
                identifier    = userName,
                nodes         = baseGraph.nodes,
                adjacencyList = baseGraph.adjacencyList,
                reverseAdjacency = baseGraph.reverseAdjacency
              )
              metadata.floorPopulation.get(baseGraph).foreach { pop =>
                metadata.floorPopulation.put(derivedGraph, pop)
              }
              userName -> derivedGraph
            }
          case None =>
            println(s"${Console.YELLOW}Warning: Base map '${baseRef.name}' referenced in 'using' clause not found${Console.RESET}")
            List.empty
        }
    }

    val linearTransports = elaboratedTransports.map { transVal =>
      buildLinearTransport(transVal, navigationGraphs, effectiveManagementDomains)
    }.toList

    // TODO: TransportGraph is built here from the original navigationGraphs, BEFORE CoordEstimator
    //   enriches them with estimated coords. This causes a stale-reference problem: StationNode.ownerGraph
    //   and StationNode.localNode inside TransportGraph point to the old graph/node objects, while
    //   RoutePlanner works with the enriched copies — breaking reference equality checks.
    //   Fix: move TransportGraph construction to AFTER CoordEstimator.estimate() completes, so all
    //   references are consistently to the enriched graphs. See RoutePlanner inter-graph edge expansion
    //   for the current identifier-based workaround.
    val transportGraph = TransportGraph(linearTransports)

    println("=== navigationGraphs ===")
    pprintln(navigationGraphs)
    println("=== linearTransports ===")
    pprintln(linearTransports)
    println("=== transportGraph ===")
    pprintln(transportGraph)

    val compilationResultMetadata = mutable.Map[String, AttributeValue]()
    effectiveManagementDomains.foreach { case (submapName, domain) =>
      compilationResultMetadata.put(s"managementDomain.$submapName", AttributeValue.StringValue(domain))
    }

    val (finalGraphs, finalLinearPaths, finalArrows) =
      SpatialMetadataExtractor.extract(
        rootEnv              = rootEnv,
        graphs               = navigationGraphs,
        elaboratedMaps       = elaboratedMaps.toMap,
        elaboratedTransports = elaboratedTransports.toList,
        linearTransports     = linearTransports
      ) match {
        case ExtractionResult(linearPaths, directionalArrows, Some(metadataMap), Some(startGraph), Some(startNode)) =>
          val enriched = CoordEstimator.estimate(
            graphs           = navigationGraphs,
            metadata         = metadataMap,
            startGraph       = startGraph,
            startNode        = startNode,
            linearTransports = linearTransports
          )
          compilationResultMetadata.put("coordEstimated", AttributeValue.BoolValue(true))
          (enriched, linearPaths, directionalArrows)
        case ExtractionResult(linearPaths, directionalArrows, None, _, _) =>
          (navigationGraphs, linearPaths, directionalArrows)
      }

    CompilationResult(
      graphs            = finalGraphs,
      transportGraph    = transportGraph,
      graphSequence     = globalConfig.orderedSubmapNames,
      linearPaths       = finalLinearPaths,
      directionalArrows = finalArrows,
      metadata          = compilationResultMetadata.toMap
    )
  }

  // Java-friendly overload
  def compileProject(files: JMap[String, String], params: JMap[String, AnyRef] = java.util.Collections.emptyMap()): CompilationResult = {
    val scalaFiles = files.asScala.toMap

    // Convert Java params map to Scala Map[String, Value]
    val scalaParams: Map[String, Value] = params.asScala.toMap.map { case (k, v) =>
      val value: Value = v match {
        case b: java.lang.Boolean => Value.BoolVal(b.booleanValue())
        case i: java.lang.Integer => Value.IntVal(i.longValue())
        case l: java.lang.Long    => Value.IntVal(l.longValue())
        case d: java.lang.Double  => Value.FloatVal(d.doubleValue())
        case f: java.lang.Float   => Value.FloatVal(f.doubleValue())
        case s: java.lang.String  => Value.StringVal(s)
        case other => throw new RuntimeException(s"Unsupported param type for key '$k': ${other.getClass.getName}")
      }
      k -> value
    }

    val configContent = scalaFiles.getOrElse("configuration.tcfg",
      scalaFiles.getOrElse("configuration", throw new RuntimeException("Missing configuration.tcfg")))

    val globalConfig = parseConfigFile(configContent) match {
      case config: GlobalConfigExpr => config
      case _ => throw new RuntimeException("Global config parsing failed")
    }
    validateConfiguredManagementDomains(globalConfig)

    metadata.submapRefRegistry = SubmapRefRegistry(globalConfig.submapUsages)

    // 1b. Parse and elaborate the root file to get the base env with constraint BoolVals bound.
    val rootEnv = scalaFiles.get("root").map { rootCode =>
      val rootExpr = parseRootFile(rootCode)
      val paramEnv = scalaParams.foldLeft(Environment.empty[Identifier, corelang.Type, Value]) { case (acc, (k, v)) =>
        acc.addValueVar(Identifier.Symbol(k), v)
      }
      rootExpr.elaborate(using TopoEnvironment(paramEnv, Map.empty, Map.empty, Map.empty)).context
    }.getOrElse {
      scalaParams.foldLeft(Environment.empty[Identifier, corelang.Type, Value]) { case (acc, (k, v)) =>
        acc.addValueVar(Identifier.Symbol(k), v)
      }
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
        val elaboratedMap = topoMapExpr.elaborate(using TopoEnvironment(rootEnv, Map.empty, Map.empty, Map.empty))
        elaboratedMaps.put(mapName, elaboratedMap)
      }
    }

    // 2b. Also parse and elaborate any base maps referenced via "using".
    for (baseRef <- metadata.submapRefRegistry.submapUsages.keys if !elaboratedMaps.contains(baseRef.name)) {
      val baseCodeOption = scalaFiles.get(baseRef.name + ".tmap").orElse(scalaFiles.get(baseRef.name))
      if (baseCodeOption.isEmpty) {
        println(s"${Console.YELLOW}Warning: Base map file not found in provided files: ${baseRef.name}${Console.RESET}")
      } else {
        val topoMapExpr   = parseMapFile(baseCodeOption.get)
        val elaboratedMap = topoMapExpr.elaborate(using TopoEnvironment(rootEnv, Map.empty, Map.empty, Map.empty))
        elaboratedMaps.put(baseRef.name, elaboratedMap)
      }
    }

    // 3. Parse each transport file and link with the topo-maps
    val transFiles = globalConfig.vehicles.map(_.name)
    val elaboratedTransports = mutable.ListBuffer[TransportValue]()

    val derivedMaps: Map[String, TopoMapValue] = metadata.submapRefRegistry.submapUsages.flatMap {
      case (baseRef, userNames) =>
        elaboratedMaps.get(baseRef.name) match {
          case Some(baseMapVal) =>
            userNames.map { userName => userName -> baseMapVal.copy(name = userName) }
          case None => List.empty
        }
    }.toMap

    val allElaboratedMaps = elaboratedMaps.toMap ++ derivedMaps
    val topoEnvForTransport = TopoEnvironment(rootEnv, Map.empty, Map.empty, allElaboratedMaps)
    val effectiveManagementDomains = resolveEffectiveManagementDomains(globalConfig, allElaboratedMaps)

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

    // 4. Convert to toponavi-core Data Structures
    val baseNavigationGraphs = elaboratedMaps.map { case (name, mapVal) =>
      name -> buildNavigationGraph(mapVal)
    }.toMap

    val navigationGraphs = baseNavigationGraphs ++ metadata.submapRefRegistry.submapUsages.flatMap {
      case (baseRef, userNames) =>
        baseNavigationGraphs.get(baseRef.name) match {
          case Some(baseGraph) =>
            userNames.map { userName =>
              val derivedGraph = NavigationGraph(
                identifier       = userName,
                nodes            = baseGraph.nodes,
                adjacencyList    = baseGraph.adjacencyList,
                reverseAdjacency = baseGraph.reverseAdjacency
              )
              metadata.floorPopulation.get(baseGraph).foreach { pop =>
                metadata.floorPopulation.put(derivedGraph, pop)
              }
              userName -> derivedGraph
            }
          case None =>
            println(s"${Console.YELLOW}Warning: Base map '${baseRef.name}' referenced in 'using' clause not found${Console.RESET}")
            List.empty
        }
    }

    val linearTransports = elaboratedTransports.map { transVal =>
      buildLinearTransport(transVal, navigationGraphs, effectiveManagementDomains)
    }.toList

    // TODO: Same stale-reference issue as above — TransportGraph must be built after CoordEstimator.
    val transportGraph = TransportGraph(linearTransports)

    println("=== navigationGraphs ===")
    println("=== linearTransports ===")
    println("=== transportGraph ===")


    val compilationResultMetadata = mutable.Map[String, AttributeValue]()
    effectiveManagementDomains.foreach { case (submapName, domain) =>
      compilationResultMetadata.put(s"managementDomain.$submapName", AttributeValue.StringValue(domain))
    }

    val (finalGraphs, finalLinearPaths, finalArrows) =
      compiler.SpatialMetadataExtractor.extract(
        rootEnv              = rootEnv,
        graphs               = navigationGraphs,
        elaboratedMaps       = elaboratedMaps.toMap,
        elaboratedTransports = elaboratedTransports.toList,
        linearTransports     = linearTransports
      ) match {
        case ExtractionResult(lp, da, Some(metadataMap), Some(startGraph), Some(startNode)) =>
          val enriched = CoordEstimator.estimate(
            graphs           = navigationGraphs,
            metadata         = metadataMap,
            startGraph       = startGraph,
            startNode        = startNode,
            linearTransports = linearTransports
          )
          compilationResultMetadata.put("coordEstimated", AttributeValue.BoolValue(true))
          (enriched, lp, da)
        case ExtractionResult(lp, da, None, _, _) =>
          (navigationGraphs, lp, da)
      }

    CompilationResult(
      graphs            = finalGraphs,
      transportGraph    = transportGraph,
      graphSequence     = globalConfig.orderedSubmapNames,
      linearPaths       = finalLinearPaths,
      directionalArrows = finalArrows,
      metadata          = compilationResultMetadata.toMap
    )
  }

  def parseConfigFile(rawCode: String): GlobalConfigExpr = {
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

  def parseRootFile(rawCode: String): RootExpr = {
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
        case ctx: MapFileParser.SurfaceDefRootExprContext =>
          new TopoMapVisitor().visitSurfaceDefRootExpr(ctx)
        case _ => throw new RuntimeException("Unexpected surface definition type. Need Root here!")
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

  private sealed trait ResolvedRideOperand
  private case class RideSubmap(name: String) extends ResolvedRideOperand
  private case class RideDomain(name: String) extends ResolvedRideOperand
  private case object RideAny extends ResolvedRideOperand

  private case class ResolvedRidePolicy(
    source: ResolvedRideOperand,
    target: ResolvedRideOperand,
    allowed: Boolean
  )

  private def validateConfiguredManagementDomains(globalConfig: GlobalConfigExpr): Unit = {
    val submapNames = globalConfig.orderedSubmapNames.toSet
    val collisions = globalConfig.configuredManagementDomains.values.toSet.intersect(submapNames)
    if (collisions.nonEmpty) {
      throw new RuntimeException(
        s"Management domain names must not equal submap names: ${collisions.toList.sorted.mkString(", ")}"
      )
    }
  }

  private def resolveEffectiveManagementDomains(
    globalConfig: GlobalConfigExpr,
    maps: Map[String, TopoMapValue]
  ): Map[String, String] = {
    val effectiveDomains = globalConfig.orderedSubmapNames.distinct.map { submapName =>
      val configured = globalConfig.configuredManagementDomains.get(submapName)
      val overrideDomain = maps.get(submapName).flatMap(_.managementDomainOverride)

      overrideDomain.foreach { domain =>
        configured match {
          case Some(configuredDomain) if configuredDomain == domain =>
            println(s"${Console.YELLOW}Warning: Redundant override for submap $submapName${Console.RESET}")
          case None =>
            println(s"${Console.YELLOW}Warning: Submap domain not specified in config file${Console.RESET}")
          case _ => ()
        }
      }

      submapName -> overrideDomain.orElse(configured).getOrElse("Misc")
    }.toMap

    val submapNames = globalConfig.orderedSubmapNames.toSet
    val collisions = effectiveDomains.values.toSet.intersect(submapNames)
    if (collisions.nonEmpty) {
      throw new RuntimeException(
        s"Management domain names must not equal submap names: ${collisions.toList.sorted.mkString(", ")}"
      )
    }

    effectiveDomains
  }

  private def resolveRideOperand(
    rawOperand: String,
    submapNames: Set[String],
    domainNames: Set[String]
  ): ResolvedRideOperand = {
    if (rawOperand == "any") RideAny
    else if (submapNames.contains(rawOperand)) RideSubmap(rawOperand)
    else if (domainNames.contains(rawOperand)) RideDomain(rawOperand)
    else throw new RuntimeException(s"Unknown ride-from operand '$rawOperand'")
  }

  private def rideOperandMatches(
    operand: ResolvedRideOperand,
    submapName: String,
    domainName: String
  ): Boolean = operand match {
    case RideSubmap(name) => name == submapName
    case RideDomain(name) => name == domainName
    case RideAny          => true
  }

  private def ridePolicySpecificity(policy: ResolvedRidePolicy): Int =
    (policy.source, policy.target) match {
      case (_: RideSubmap, _: RideSubmap) => 1
      case (_: RideSubmap, _: RideDomain) => 2
      case (_: RideDomain, _: RideSubmap) => 3
      case (_: RideDomain, _: RideDomain) => 4
      case (RideAny, _: RideSubmap)       => 5
      case (_: RideSubmap, RideAny)       => 6
      case (RideAny, _: RideDomain)       => 7
      case (_: RideDomain, RideAny)       => 8
      case (RideAny, RideAny)             => 9
    }

  private def buildAllowedRidePairs(
    transVal: TransportValue,
    graphs: Map[String, NavigationGraph],
    effectiveManagementDomains: Map[String, String]
  ): Option[Set[(NavigationGraph, NavigationGraph)]] = {
    if (transVal.ridePolicies.isEmpty) return None

    val submapNames = effectiveManagementDomains.keySet
    val domainNames = effectiveManagementDomains.values.toSet
    val policies = transVal.ridePolicies.map { policy =>
      ResolvedRidePolicy(
        source = resolveRideOperand(policy.source, submapNames, domainNames),
        target = resolveRideOperand(policy.target, submapNames, domainNames),
        allowed = policy.allowed
      )
    }

    policies.groupBy(policy => policy.source -> policy.target).foreach { case ((source, target), sameScope) =>
      if (sameScope.map(_.allowed).distinct.size > 1) {
        throw new RuntimeException(
          s"Conflicting ride-from rules with equal specificity for $source to $target"
        )
      }
    }

    val stationGraphs = transVal.stations.map { case (nodeRef, _) => graphs(nodeRef.fromMapName) }.distinct
    val allowedPairs = for {
      sourceGraph <- stationGraphs
      targetGraph <- stationGraphs
      if sourceGraph != targetGraph
      sourceDomain = effectiveManagementDomains.getOrElse(sourceGraph.identifier, "Misc")
      targetDomain = effectiveManagementDomains.getOrElse(targetGraph.identifier, "Misc")
      matching = policies.filter { policy =>
        rideOperandMatches(policy.source, sourceGraph.identifier, sourceDomain) &&
          rideOperandMatches(policy.target, targetGraph.identifier, targetDomain)
      }
      best = if (matching.isEmpty) List.empty else {
        val bestSpecificity = matching.map(ridePolicySpecificity).min
        matching.filter(ridePolicySpecificity(_) == bestSpecificity)
      }
      if best.isEmpty || best.forall(_.allowed)
    } yield sourceGraph -> targetGraph

    Some(allowedPairs.toSet)
  }

  private def buildLinearTransport(
    transVal: TransportValue,
    graphs: Map[String, NavigationGraph],
    effectiveManagementDomains: Map[String, String]
  ): LinearTransport = {
    val allowedRidePairs = buildAllowedRidePairs(transVal, graphs, effectiveManagementDomains)
    transVal.surfaceType match {
      case "Elevator"  => buildElevatorBank(transVal, graphs, allowedRidePairs)
      case "Escalator" => buildEscalator(transVal, graphs, allowedRidePairs)
      case "Stairs"    => buildStairCase(transVal, graphs, allowedRidePairs)
      case _ => throw new RuntimeException(s"Unsupported transport type '${transVal.surfaceType}'.")
    }
  }

  private def buildEscalator(
    transVal: TransportValue,
    graphs: Map[String, NavigationGraph],
    allowedRidePairs: Option[Set[(NavigationGraph, NavigationGraph)]]
  ): Escalator = {
    val invalidNumericTime =
      new RuntimeException(s"Escalator '${transVal.name}' must contain numeric 'params.time'")

    val travelTime = transVal.context.values.get(corelang.Identifier.Symbol("params")) match {
      case Some(Value.RecordVal(fields)) =>
        fields.get("time") match {
          case Some(Value.FloatVal(value)) => value
          case Some(Value.IntVal(value)) => value.toDouble
          case _ => throw invalidNumericTime
        }
      case _ => throw invalidNumericTime
    }

    if (!travelTime.isFinite || travelTime <= 0.0) {
      throw new RuntimeException(s"Escalator '${transVal.name}' params.time must be finite and greater than 0")
    }

    if (transVal.stations.size != 2) {
      throw new RuntimeException(s"Escalator '${transVal.name}' must have exactly 2 stations")
    }

    val resolvedStations = transVal.stations.map { case (nodeRef, stationData) =>
      val graph = graphs(nodeRef.fromMapName)
      val node = graph.nodes.find(_.identifier == nodeRef.nodeName)
        .getOrElse(throw new RuntimeException(s"Node not found in Core Graph: ${nodeRef.nodeName}"))
      (nodeRef, stationData, graph, node)
    }

    if (resolvedStations.map(_._3).distinct.size != 2) {
      throw new RuntimeException(s"Escalator '${transVal.name}' stations must belong to two distinct submaps")
    }

    val stations = resolvedStations.map { case (_, _, graph, node) => graph -> node }.toMap
    val locations = resolvedStations.zipWithIndex.map { case ((_, _, graph, _), index) =>
      graph -> index.toDouble
    }.toMap
    val stationLabels = transVal.stationLabels.map { case (nodeRef, label) =>
      graphs(nodeRef.fromMapName) -> label
    }
    val stationPermissions = resolvedStations.map { case (_, stationData, graph, _) =>
      val permission = stationData.fields.get("_permission") match {
        case Some(Value.StringVal("NoAccess"))   => enums.TransportServicePermission.NoAccess
        case Some(Value.StringVal("ArriveOnly")) => enums.TransportServicePermission.ArriveOnly
        case Some(Value.StringVal("DepartOnly")) => enums.TransportServicePermission.DepartOnly
        case _                                    => enums.TransportServicePermission.FullyGranted
      }
      graph -> permission
    }.toMap

    Escalator(
      identifier = transVal.name,
      stationNodes = stations,
      stationLocations = locations,
      stationPermissions = stationPermissions,
      travelTimeSeconds = travelTime,
      stationLabels = stationLabels,
      displayName = transportDisplayName(transVal),
      allowedRidePairs = allowedRidePairs
    )
  }

  private def buildElevatorBank(
    transVal: TransportValue,
    graphs: Map[String, NavigationGraph],
    allowedRidePairs: Option[Set[(NavigationGraph, NavigationGraph)]]
  ): ElevatorBank = {
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
    val displayName = transportDisplayName(transVal)

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

    val stationLabels = transVal.stationLabels.map { case (nodeRef, label) =>
      graphs(nodeRef.fromMapName) -> label
    }


    val depRates = transVal.stations.map { case (nodeRef, stationData) =>
      val loopGraph = graphs(nodeRef.fromMapName)
      val depRate = stationData.fields.get("departureRate") match {
        case Some(Value.FloatVal(v)) => v
        case _ => throw RuntimeException("departureRate must be specified as a float for each station") // TODO: Only applicable for elevator
      }
      (loopGraph, depRate)
    }.toMap

    // Read "_permission" from each station's RecordVal (injected by the constraint evaluation).
    // If the field is absent the station is FullyGranted; "NoAccess" maps to NoAccess.
    val stPermissions = transVal.stations.map { case (nodeRef, stationData) =>
      val loopGraph = graphs(nodeRef.fromMapName)
      val permission = stationData.fields.get("_permission") match {
        case Some(Value.StringVal("NoAccess"))    => enums.TransportServicePermission.NoAccess
        case Some(Value.StringVal("ArriveOnly"))  => enums.TransportServicePermission.ArriveOnly
        case Some(Value.StringVal("DepartOnly"))  => enums.TransportServicePermission.DepartOnly
        case _                                    => enums.TransportServicePermission.FullyGranted
      }
      loopGraph -> permission
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
      duty = duty,
      stationLabels = stationLabels,
      displayName = displayName,
      allowedRidePairs = allowedRidePairs
    )
  }

  private def buildStairCase(
    transVal: TransportValue,
    graphs: Map[String, NavigationGraph],
    allowedRidePairs: Option[Set[(NavigationGraph, NavigationGraph)]]
  ): StairCase = {
    // Retrieve 'params' from the context, which is expected to be a RecordVal
    val paramsOption = transVal.context.values.get(corelang.Identifier.Symbol("params"))
    val d = paramsOption match {
      case Some(Value.RecordVal(fields)) => fields
      case Some(_) => throw new RuntimeException(s"Symbol 'params' in transport ${transVal.name} must be a RecordVal")
      case None => throw new RuntimeException("Must contain a 'params' record in transport context for transport data")
    }

    val turnBackCost = d.get("turnBackCost").map { case Value.IntVal(v) => v.toInt; case _ => throw RuntimeException("turnBackCost must be Int") }.getOrElse(3)
    val displayName = transportDisplayName(transVal)

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

    val stationLabels = transVal.stationLabels.map { case (nodeRef, label) =>
      graphs(nodeRef.fromMapName) -> label
    }

    val runIndices = transVal.stations.map { case (nodeRef, stationData) =>
      val loopGraph = graphs(nodeRef.fromMapName)
      val loc = stationData.fields.get("directSegmentIndex") match {
        case Some(Value.IntVal(v)) => v.toInt
        case _ => throw RuntimeException("directSegmentIndex must be specified as an Int for each station")
      }
      (loopGraph, loc)
    }.toMap
    

    StairCase(
      identifier = transVal.name,
      stationNodes = stations,
      stationLocations = locations,
      stationRunIndices = runIndices,
      turnAroundLoss = turnBackCost,
      stationLabels = stationLabels,
      displayName = displayName,
      allowedRidePairs = allowedRidePairs
    )
  }

  private def transportDisplayName(transVal: TransportValue): Option[String] =
    transVal.context.values.get(corelang.Identifier.Symbol("displayName")) match {
      case Some(Value.StringVal(value)) => Some(value)
      case Some(_) => throw RuntimeException(s"displayName must be a String in transport ${transVal.name}")
      case None => None
    }


  private def convertAttributes(record: Value.RecordVal): Map[String, enums.AttributeValue] = {
    record.fields.map { case (k, v) =>
      k -> convertAttributeValue(v)
    }
  }

  private def convertAttributeValue(value: Value): enums.AttributeValue = value match {
    case Value.IntVal(n) => enums.AttributeValue.IntValue(n.toInt)
    case Value.FloatVal(n) => enums.AttributeValue.DoubleValue(n)
    case Value.BoolVal(b) => enums.AttributeValue.BoolValue(b)
    case Value.StringVal(s) => enums.AttributeValue.StringValue(s)
    case Value.ListVal(_, elements) =>
      enums.AttributeValue.ListValue(elements.map(convertAttributeValue))
    case other => enums.AttributeValue.StringValue(other.toString)
  }

}



class CompilerMetadataContext() {
  val floorPopulation: mutable.Map[NavigationGraph, Int] = mutable.Map.empty
  var submapRefRegistry: SubmapRefRegistry = SubmapRefRegistry(Map.empty)
}
