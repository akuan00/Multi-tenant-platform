package com.company.ai.platform.knowledge.chunker;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    public static List<String> split(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and < chunkSize");
        }

        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            start += step;
            if (end == text.length()) {
                break;
            }
        }

        return chunks;
    }
}
