package com.abhi.contract_explainer.model;

/**
 * Response for the /ask endpoint.
 *
 * We now return:
 *  - answer:      LLM explanation in simple language
 *  - clauseId:    which clause we used as context (e.g. "clause-3")
 *  - clauseLabel: classifier label (e.g. "Termination For Convenience")
 *  - clauseText:  the actual contract text we used to answer
 */
public class AnswerResponse {

    private String answer;
    private String clauseId;
    private String clauseLabel;
    private String clauseText;

    // 🔹 No-args constructor: needed by Spring/Jackson
    public AnswerResponse() {
    }

    // 🔹 Constructor if we only had an answer (fallback / error)
    public AnswerResponse(String answer) {
        this.answer = answer;
    }

    // 🔹 Full constructor with clause metadata
    public AnswerResponse(String answer, String clauseId, String clauseLabel, String clauseText) {
        this.answer = answer;
        this.clauseId = clauseId;
        this.clauseLabel = clauseLabel;
        this.clauseText = clauseText;
    }

    // 🔹 Getters and setters

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getClauseId() {
        return clauseId;
    }

    public void setClauseId(String clauseId) {
        this.clauseId = clauseId;
    }

    public String getClauseLabel() {
        return clauseLabel;
    }

    public void setClauseLabel(String clauseLabel) {
        this.clauseLabel = clauseLabel;
    }

    public String getClauseText() {
        return clauseText;
    }

    public void setClauseText(String clauseText) {
        this.clauseText = clauseText;
    }
}