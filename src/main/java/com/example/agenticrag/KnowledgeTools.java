package com.example.agenticrag;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeTools {

    private final KnowledgeBase knowledgeBase;

    public KnowledgeTools(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Tool(description = "Search local text and Markdown knowledge files for relevant passages.")
    public List<KnowledgeBase.KnowledgeChunk> searchKnowledgeBase(
            @ToolParam(description = "A concise search query about the information needed") String query) {
        return knowledgeBase.search(query);
    }

    @Tool(description = "List the local text and Markdown files currently loaded into the knowledge base.")
    public List<String> listKnowledgeSources() {
        return knowledgeBase.sources();
    }
}
