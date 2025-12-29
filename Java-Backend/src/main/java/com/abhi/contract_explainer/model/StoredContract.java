package com.abhi.contract_explainer.model;

import java.util.List;

/**
 * Represents one uploaded contract in our system.
 *
 * - fullText: the entire contract text as one big string
 * - clauses:  the list of clauses we extracted and classified from this contract
 */
public class StoredContract {

    private String fullText;      // Whole contract text
    private List<Clause> clauses; // All clauses (id + label + text)

    // 🔹 No-args constructor: needed by Spring/Jackson
    public StoredContract() {
    }

    // 🔹 All-args constructor: for us to create StoredContract easily
    public StoredContract(String fullText, List<Clause> clauses) {
        this.fullText = fullText;
        this.clauses = clauses;
    }

    // 🔹 Getters and setters

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    public void setClauses(List<Clause> clauses) {
        this.clauses = clauses;
    }
}