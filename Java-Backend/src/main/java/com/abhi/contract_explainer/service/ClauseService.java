package com.abhi.contract_explainer.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper service to split a full contract text into smaller clause-like chunks.
 *
 * This is a SIMPLE, heuristic splitter:
 *  - Splits on two or more line breaks
 *  - Trims each chunk
 *  - Ignores very short chunks (e.g., headings only)
 *
 * Later, if you want, you can make this smarter (e.g., detect numbered sections).
 */
@Service
public class ClauseService {

    /**
     * Split the contract full text into a list of "clauses".
     * For now we:
     *  - Split on 2+ newlines (blank lines)
     *  - Trim each part
     *  - Drop very short parts (less than 40 chars)
     */
    public List<String> splitIntoClauses(String fullText) {
        List<String> clauses = new ArrayList<>();

        if (fullText == null || fullText.isBlank()) {
            return clauses;
        }

        // Normalize line endings (in case of Windows-style \r\n)
        String normalized = fullText.replace("\r\n", "\n");

        // Split on two or more newline characters -> blank-line separation
        // This treats big blocks separated by blank lines as "clauses"
        String[] rawParts = normalized.split("\\n{2,}");

        for (String part : rawParts) {
            String trimmed = part.trim();

            // Skip very short chunks (likely headings or noise)
            if (trimmed.length() < 40) {
                continue;
            }

            clauses.add(trimmed);
        }

        return clauses;
    }
}