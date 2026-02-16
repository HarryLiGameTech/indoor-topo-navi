package com.e611.toponavi.web;

import compiler.TopoScriptCompiler;
import compiler.CompilationResult;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final TopoScriptCompiler compiler = new TopoScriptCompiler();

    @PostMapping("/compile")
    public CompilationResultDTO compile(@RequestBody Map<String, String> projectFiles) {
        // Call the Scala compiler
        // Since Scala methods can throw checked exceptions that Java doesn't enforce, wrap in try-catch if needed
        CompilationResult result = compiler.compileProject(projectFiles);

        // Convert Scala result to a Java DTO for JSON serialization
        // (Spring's Jackson serializer might struggle with deep Scala collections,
        //  so explicitly mapping to Java POJOs is safer and "industry standard")
        return DTOConverter.convert(result);
    }
}