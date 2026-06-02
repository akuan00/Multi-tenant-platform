package com.company.ai.platform.knowledge.parser;

import java.io.InputStream;

public interface DocumentParser {
    String parse(InputStream inputStream, String fileType);
}
