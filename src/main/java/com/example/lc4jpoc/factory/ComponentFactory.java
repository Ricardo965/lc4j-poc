package com.example.lc4jpoc.factory;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ComponentFactory {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String openAiApiKey;

    @Value("${langchain4j.google-ai.gemini.chat-model.api-key}")
    private String geminiApiKey;

    @Value("${langchain4j.google-ai.gemini.chat-model.model-name}")
    private String geminiModelName;

    /**
     * Swaps ChatLanguageModel based on the requested provider.
     */
    public ChatLanguageModel getChatModel(String provider) {
        return switch (provider.toLowerCase()) {
            case "openai" -> OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName("gpt-4o-mini")
                    .build();
            case "gemini" -> GoogleAiGeminiChatModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName(geminiModelName)
                    .build();
            default -> throw new IllegalArgumentException("Unsupported LLM provider: " + provider);
        };
    }

    /**
     * Swaps EmbeddingModel based on the requested model name.
     */
    public EmbeddingModel getEmbeddingModel(String modelName) {
        return switch (modelName.toLowerCase()) {
            case "openai" -> OpenAiEmbeddingModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName("text-embedding-3-small")
                    .build();
            case "local" -> new AllMiniLmL6V2EmbeddingModel();
            default -> throw new IllegalArgumentException("Unsupported Embedding model: " + modelName);
        };
    }

    /**
     * Swaps DocumentSplitter based on the requested chunking method.
     */
    public DocumentSplitter getDocumentSplitter(String method) {
        return switch (method.toLowerCase()) {
            case "recursive" -> DocumentSplitters.recursive(300, 30);
            case "sentence" -> DocumentSplitters.recursive(100, 10); // Simulating another strategy to showcase swapping
            default -> throw new IllegalArgumentException("Unsupported chunking method: " + method);
        };
    }
}
