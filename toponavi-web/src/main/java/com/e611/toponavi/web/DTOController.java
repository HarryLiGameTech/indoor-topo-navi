//package com.e611.toponavi.web;
//
//import com.e611.toponavi.web.dto.*;
//import compiler.CompilationResult;
//import data.*;
//import enums.AttributeValue;
//import enums.PathType;
//import enums.TransportServicePermission;
//import enums.VisitingMode;
//
//import scala.Tuple2;
//import scala.jdk.javaapi.CollectionConverters;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class DTOController {
//    public static CompilationResultDTO convert(CompilationResult scalaResult) {
//        CompilationResultDTO dto = new CompilationResultDTO();
//
//        // Convert Navigation Graphs
//        Map<String, NavigationGraph> scalaGraphs = CollectionConverters.asJava(scalaResult.graphs());
//        dto.graphs = scalaGraphs.entrySet().stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        entry -> convertNavigationGraph(entry.getValue())
//                ));
//
//        // Convert Transport Graph
//        dto.transportGraph = convertTransportGraph(scalaResult.transportGraph());
//
//        return dto;
//    }
//
//    private static NavigationGraphDTO convertNavigationGraph(NavigationGraph graph) {
//        NavigationGraphDTO dto = new NavigationGraphDTO();
//        dto.identifier = graph.identifier();
//
//        // Convert Nodes
//        List<TopoNode> scalaNodes = CollectionConverters.asJava(graph.nodes());
//        dto.nodes = scalaNodes.stream()
//                .map(DTOController::convertTopoNode)
//                .collect(Collectors.toList());
//
//        // Convert Adjacency List (Edges)
//        List<AtomicPath> scalaEdges = CollectionConverters.asJava(graph.adjacencyList());
//        dto.adjacencyList = scalaEdges.stream()
//                .map(DTOController::convertAtomicPath)
//                .collect(Collectors.toList());
//
//        return dto;
//    }
//
//    private static TopoNodeDTO convertTopoNode(TopoNode node) {
//        TopoNodeDTO dto = new TopoNodeDTO();
//        dto.identifier = node.identifier();
//        dto.attributes = convertAttributes(node.attributes());
//        return dto;
//    }
//
//    private static AtomicPathDTO convertAtomicPath(AtomicPath path) {
//        AtomicPathDTO dto = new AtomicPathDTO();
//        dto.sourceId = path.source().identifier();
//        dto.targetId = path.target().identifier();
//        dto.attributes = convertAttributes(path.attributes());
//        dto.costs = convertCosts(path.costs());
//        dto.pathType = path.pathType().toString();
//        return dto;
//    }
//
//    private static TransportGraphDTO convertTransportGraph(TransportGraph graph) {
//        TransportGraphDTO dto = new TransportGraphDTO();
//
//        List<StationNode> scalaNodes = CollectionConverters.asJava(graph.nodes());
//        dto.nodes = scalaNodes.stream()
//                .map(DTOController::convertStationNode)
//                .collect(Collectors.toList());
//
//        Map<StationNode, scala.collection.immutable.Set<StationNode>> scalaAdj =
//            CollectionConverters.asJava(graph.adjacencyList());
//
//        dto.adjacencyList = scalaAdj.entrySet().stream()
//                .collect(Collectors.toMap(
//                        e -> e.getKey().identifier(),
//                        e -> {
//                            Set<StationNode> nodes = CollectionConverters.asJava(e.getValue());
//                            return nodes.stream().map(StationNode::identifier).collect(Collectors.toSet());
//                        }
//                ));
//
//        return dto;
//    }
//
//    private static StationNodeDTO convertStationNode(StationNode node) {
//        StationNodeDTO dto = new StationNodeDTO();
//        dto.identifier = node.identifier();
//        dto.ownerGraphId = node.ownerGraph().identifier();
//        dto.ownerLineId = node.ownerLine().identifier();
//        dto.permission = node.permission().toString();
//        return dto;
//    }
//
//    // Helper to convert AttributeValue Map
//    private static Map<String, Object> convertAttributes(scala.collection.immutable.Map<String, AttributeValue> attributes) {
//        Map<String, AttributeValue> javaMap = CollectionConverters.asJava(attributes);
//        return javaMap.entrySet().stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        e -> convertAttributeValue(e.getValue())
//                ));
//    }
//
//    private static Object convertAttributeValue(AttributeValue value) {
//        if (value instanceof AttributeValue.IntValue) {
//            return ((AttributeValue.IntValue) value).value();
//        } else if (value instanceof AttributeValue.DoubleValue) {
//            return ((AttributeValue.DoubleValue) value).value();
//        } else if (value instanceof AttributeValue.BoolValue) {
//            return ((AttributeValue.BoolValue) value).value();
//        } else if (value instanceof AttributeValue.StringValue) {
//            return ((AttributeValue.StringValue) value).value();
//        }
//        return value.toString();
//    }
//
//    // Helper to convert Cost Map
//    private static Map<String, Double> convertCosts(scala.collection.immutable.Map<VisitingMode, Object> costs) {
//        // costs is Map[VisitingMode, Double].
//        // In Java it might appear as Map<VisitingMode, Object> due to erasure or generic mapping if Double is primitive.
//        // Actually scala Double is java.lang.Double (boxed) in Map.
//        Map<VisitingMode, Object> javaMap = CollectionConverters.asJava(costs);
//        return javaMap.entrySet().stream()
//                .collect(Collectors.toMap(
//                        e -> e.getKey().toString(),
//                        e -> (Double) e.getValue()
//                ));
//    }
//}
//
//
