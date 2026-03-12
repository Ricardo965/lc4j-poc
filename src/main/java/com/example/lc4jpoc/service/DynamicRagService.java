package com.example.lc4jpoc.service;

import com.example.lc4jpoc.factory.ComponentFactory;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DynamicRagService {

    private final ComponentFactory componentFactory;
    private EmbeddingStore<TextSegment> embeddingStore;

    public DynamicRagService(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        // Initialize an empty store
        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }

    /**
     * Ingests documents into the EmbeddingStore using the requested chunking and embedding logic.
     */
    public void ingest(String chunkingMethod, String embeddingModelName) {
        // Reset the store for the POC so we don't mix embeddings from different models
        this.embeddingStore = new InMemoryEmbeddingStore<>();

        Path docPath = Paths.get("src/main/resources/docs");
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(docPath, new ApacheTikaDocumentParser());

        dev.langchain4j.data.document.DocumentSplitter documentSplitter = componentFactory.getDocumentSplitter(chunkingMethod);
        EmbeddingModel embeddingModel = componentFactory.getEmbeddingModel(embeddingModelName);

        // Splitting
        List<TextSegment> segments = documentSplitter.splitAll(documents);

        // Embedding & Storing
        embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);

        System.out.println("Ingestion complete. Documents split using '" + chunkingMethod + "' and embedded using '" + embeddingModelName + "'.");
    }

    /**
     * Builds the AI Service dynamically and answers the user query.
     */
    public String chat(String question, String llmProvider, String embeddingModelName) {
        ChatLanguageModel chatModel = componentFactory.getChatModel(llmProvider);
        EmbeddingModel embeddingModel = componentFactory.getEmbeddingModel(embeddingModelName);

        // Create the Retriever with the dynamically selected embedding model
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();

        // Note: For a true POC we build the AiService proxy dynamically each time. 
        // In reality, you'd cache these or rely on ConversationalRetrievalChain, but AiServices is simpler.
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();

        return assistant.chat(question);
    }

    // Required Interface for AiService
    public interface Assistant {
        String chat(String userMessage);
    }
}
