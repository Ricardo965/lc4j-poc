package com.example.lc4jpoc.controller;

import com.example.lc4jpoc.service.DynamicRagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final DynamicRagService dynamicRagService;

    public RagController(DynamicRagService dynamicRagService) {
        this.dynamicRagService = dynamicRagService;
    }

    /**
     * Endpoint to ingest documents into the in-memory store.
     * Expects JSON: { "chunkingMethod": "recursive", "embeddingModel": "openai" }
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestBody Map<String, String> payload) {
        String chunkingMethod = payload.getOrDefault("chunkingMethod", "recursive");
        String embeddingModel = payload.getOrDefault("embeddingModel", "local");

        try {
            dynamicRagService.ingest(chunkingMethod, embeddingModel);
            return ResponseEntity.ok("Successfully ingested documents using: " +
                    "chunking=" + chunkingMethod + ", embedding=" + embeddingModel);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during ingestion: " + e.getMessage());
        }
    }

    /**
     * Endpoint to ask questions using the in-memory store.
     * Expects JSON: { "question": "What is...", "llmProvider": "openai", "embeddingModel": "local" }
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String llmProvider = payload.getOrDefault("llmProvider", "openai");
        String embeddingModel = payload.getOrDefault("embeddingModel", "local");

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Question must be provided.");
        }

        try {
            String answer = dynamicRagService.chat(question, llmProvider, embeddingModel);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during chat: " + e.getMessage());
        }
    }
}
