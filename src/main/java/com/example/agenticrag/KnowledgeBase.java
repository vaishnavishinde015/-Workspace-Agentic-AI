package com.example.agenticrag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBase {

    private static final int CHUNK_SIZE = 1200;
    private static final int CHUNK_OVERLAP = 200;
    private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]+");

    private final Path directory;
    private volatile List<KnowledgeChunk> chunks = List.of();

    public KnowledgeBase(@Value("${app.knowledge-directory:knowledge}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @jakarta.annotation.PostConstruct
    public void load() {
        try {
            Files.createDirectories(directory);
            List<KnowledgeChunk> loaded = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.filter(Files::isRegularFile)
                        .filter(this::isSupported)
                        .sorted()
                        .forEach(path -> readChunks(path, loaded));
            }
            chunks = List.copyOf(loaded);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load knowledge files from " + directory, exception);
        }
    }

    public List<KnowledgeChunk> search(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        List<String> terms = tokenize(query);
        if (terms.isEmpty()) {
            return List.of();
        }

        return chunks.stream()
                .map(chunk -> new ScoredChunk(chunk, score(terms, chunk.text())))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(3)
                .map(ScoredChunk::chunk)
                .toList();
    }

    public List<String> sources() {
        return chunks.stream()
                .map(KnowledgeChunk::source)
                .distinct()
                .sorted()
                .toList();
    }

    private boolean isSupported(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".md") || name.endsWith(".txt");
    }

    private void readChunks(Path path, List<KnowledgeChunk> destination) {
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            String source = directory.relativize(path).toString();
            for (int start = 0; start < text.length(); start += CHUNK_SIZE - CHUNK_OVERLAP) {
                int end = Math.min(start + CHUNK_SIZE, text.length());
                String content = text.substring(start, end).trim();
                if (!content.isEmpty()) {
                    destination.add(new KnowledgeChunk(source, content));
                }
                if (end == text.length()) {
                    break;
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read knowledge file " + path, exception);
        }
    }

    private int score(List<String> queryTerms, String text) {
        List<String> documentTerms = tokenize(text);
        int score = 0;
        for (String term : queryTerms) {
            score += (int) documentTerms.stream().filter(term::equals).count();
        }
        return score;
    }

    private List<String> tokenize(String text) {
        return WORDS.matcher(text.toLowerCase(Locale.ROOT))
                .results()
                .map(match -> match.group())
                .toList();
    }

    public record KnowledgeChunk(String source, String text) {
    }

    private record ScoredChunk(KnowledgeChunk chunk, int score) {
    }
}
