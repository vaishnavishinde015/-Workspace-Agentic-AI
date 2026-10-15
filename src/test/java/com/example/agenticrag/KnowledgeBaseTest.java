package com.example.agenticrag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KnowledgeBaseTest {

    @TempDir
    Path directory;

    @Test
    void loadsSupportedFilesAndFindsRelevantChunks() throws Exception {
        Files.writeString(directory.resolve("guide.md"), "Configure OpenRouter with an API key.");
        Files.writeString(directory.resolve("ignore.pdf"), "This file is not supported.");

        KnowledgeBase knowledgeBase = new KnowledgeBase(directory.toString());
        knowledgeBase.load();

        List<KnowledgeBase.KnowledgeChunk> results = knowledgeBase.search("OpenRouter API");

        assertEquals(1, results.size());
        assertEquals("guide.md", results.get(0).source());
        assertTrue(results.get(0).text().contains("OpenRouter"));
        assertEquals(List.of("guide.md"), knowledgeBase.sources());
    }

    @Test
    void rejectsBlankSearchQueries() {
        KnowledgeBase knowledgeBase = new KnowledgeBase(directory.toString());
        knowledgeBase.load();

        assertThrows(IllegalArgumentException.class, () -> knowledgeBase.search(" "));
    }
}
