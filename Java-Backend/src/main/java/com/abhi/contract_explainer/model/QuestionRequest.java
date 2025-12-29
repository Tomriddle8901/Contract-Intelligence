package com.abhi.contract_explainer.model;

public class QuestionRequest {
    private String question;

    public String getQuestion()
    {
        return question;
    }
    public QuestionRequest()
    {
    }
    public QuestionRequest(String question)
    {
        this.question = question;
    }


    public void setQuestion(String question) {
        this.question = question;
    }
}
