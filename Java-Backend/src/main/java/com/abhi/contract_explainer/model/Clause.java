package com.abhi.contract_explainer.model;

/**
 * Represents a single clause inside a contract.
 * Example:
 *  - id: "clause-1"
 *  - label: "Termination For Convenience"
 *  - text: "Either party may terminate this Agreement..."
 */
public class Clause {

    private String id;      // "clause-1"
    private String label;   // classifier label: "Termination For Convenience"
    private String text;    // actual clause text from the contract

    // No-args constructor for Jackson / Spring
    public Clause() {
    }

    // All-args constructor in this exact order: (id, label, text)
    public Clause(String id, String label, String text) {
        this.id = id;
        this.label = label;
        this.text = text;
    }

    // Getters + setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}