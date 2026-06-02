package com.company.ai.platform.knowledge.parser.impl;

import com.company.ai.platform.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class TikaDocumentParser implements DocumentParser {

    private final Tika tika = new Tika();

    @Override
    public String parse(InputStream inputStream, String fileType) {
        try {
            return tika.parseToString(inputStream);
        } catch (Exception e) {
            log.error("Failed to parse document of type: {}", fileType, e);
            throw new RuntimeException("Document parsing failed: " + e.getMessage(), e);
        }
    }
}
