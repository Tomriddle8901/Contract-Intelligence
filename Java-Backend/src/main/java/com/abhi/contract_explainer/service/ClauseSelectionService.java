package com.abhi.contract_explainer.service;

import com.abhi.contract_explainer.model.Clause;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Selects the "best" clause for a given user question.
 *
 * For now, we use a very simple heuristic:
 *  - Split the question into keywords (lowercase words, length >= 3)
 *  - For each clause, count how many of those keywords appear in the clause text
 *  - Pick the clause with the highest overlap score
 */
@Service
public class ClauseSelectionService {

    /**
     * Find the most relevant clause for the given question.
     *
     * @param clauses  list of clauses from the contract
     * @param question user question
     * @return best Clause, or null if no clauses
     */
    public Clause findBestClause(List<Clause> clauses, String question) {
        if (clauses == null || clauses.isEmpty()) {
            return null;
        }

        if (question == null || question.isBlank()) {
            // no question: we can't meaningfully pick
            return null;
        }

        // Build a set of simple keywords from the question
        Set<String> questionKeywords = extractKeywords(question);

        Clause bestClause = null;
        int bestScore = -1;

        for (Clause clause : clauses) {
            String text = clause.getText();
            if (text == null || text.isBlank()) {
                continue;
            }

            int score = computeOverlapScore(questionKeywords, text);

            if (score > bestScore) {
                bestScore = score;
                bestClause = clause;
            }
        }

        // If all scores were 0, this might still return the first non-empty clause,
        // but that's okay as a baseline. We can improve the heuristic later.
        return bestClause;
    }

    /**
     * Extract keywords from the question: lowercase, only words length >= 3.
     */
    private Set<String> extractKeywords(String text) {
        String lower = text.toLowerCase();
        String[] tokens = lower.split("\\W+"); // split on non-word characters

        Set<String> keywords = new HashSet<>();
        for (String token : tokens) {
            if (token.length() >= 3) {
                keywords.add(token);
            }
        }
        return keywords;
    }

    /**
     * Count how many keywords appear in the clause text (case-insensitive).
     */
    private int computeOverlapScore(Set<String> questionKeywords, String clauseText) {
        String lowerClause = clauseText.toLowerCase();

        int score = 0;
        for (String kw : questionKeywords) {
            if (lowerClause.contains(kw)) {
                score++;
            }
        }
        return score;
    }
}