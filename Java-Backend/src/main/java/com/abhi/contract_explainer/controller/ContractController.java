package com.abhi.contract_explainer.controller;

import com.abhi.contract_explainer.model.AnswerResponse;
import com.abhi.contract_explainer.model.QuestionRequest;
import com.abhi.contract_explainer.model.UploadResponse;
import com.abhi.contract_explainer.model.ClauseTextRequest;
import com.abhi.contract_explainer.model.StoredContract;
import com.abhi.contract_explainer.model.Clause;
import com.abhi.contract_explainer.service.LlmService;
import com.abhi.contract_explainer.service.PdfService;
import com.abhi.contract_explainer.service.ClauseClassifierService;
import com.abhi.contract_explainer.service.ClauseService;
import com.abhi.contract_explainer.service.ClauseSelectionService;
import com.abhi.contract_explainer.store.ContractStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final PdfService pdfService;
    private final ContractStore contractStore;
    private final LlmService llmService;
    private final ClauseClassifierService clauseClassifierService;
    private final ClauseService clauseService;
    private final ClauseSelectionService clauseSelectionService;

    // Constructor: Spring will automatically pass all required services here
    public ContractController(PdfService pdfService,
                              ContractStore contractStore,
                              LlmService llmService,
                              ClauseClassifierService clauseClassifierService,
                              ClauseService clauseService,
                              ClauseSelectionService clauseSelectionService) {
        this.pdfService = pdfService;
        this.contractStore = contractStore;
        this.llmService = llmService;
        this.clauseClassifierService = clauseClassifierService;
        this.clauseService = clauseService;
        this.clauseSelectionService = clauseSelectionService;
    }

    // 1️⃣ Endpoint to upload a PDF
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws Exception {
        // a) extract text from the uploaded PDF
        String fullText = pdfService.extractText(file);

        // b) split full text into clause-like chunks
        List<String> clauseTexts = clauseService.splitIntoClauses(fullText);

        // c) for each clause text, call the classifier and build Clause objects
        List<Clause> clauses = new ArrayList<>();
        int index = 1;
        for (String clauseText : clauseTexts) {
            String label = clauseClassifierService.classifyClause(clauseText);
            String clauseId = "clause-" + index;

            Clause clause = new Clause(clauseId, label, clauseText);
            clauses.add(clause);

            index++;
        }

        // d) build StoredContract with full text + all classified clauses
        StoredContract storedContract = new StoredContract(fullText, clauses);

        // e) save the StoredContract and get a contractId
        String contractId = contractStore.save(storedContract);

        // f) get a summary for the contract from the LLM (uses fullText)
        String summary = llmService.summarizeContract(fullText);

        // g) return contractId + summary as JSON
        return new UploadResponse(contractId, summary);
    }

    // 2️⃣ Endpoint to ask a question about a previously uploaded contract
    // 2️⃣ Endpoint to ask a question about a previously uploaded contract
    @PostMapping("/{id}/ask")
    public AnswerResponse ask(@PathVariable String id,
                              @RequestBody QuestionRequest request) {

        // a) get the StoredContract (full text + clauses) for that id
        StoredContract storedContract = contractStore.getById(id);

        if (storedContract == null) {
            // no contract found for that id
            return new AnswerResponse("No contract found for id: " + id);
        }

        String question = request.getQuestion();

        // b) Try to find the best clause for this question
        List<Clause> clauses = storedContract.getClauses();
        Clause bestClause = clauseSelectionService.findBestClause(clauses, question);

        String contextText;
        String clauseId = null;
        String clauseLabel = null;
        String clauseText = null;

        if (bestClause != null) {
            // Use the best clause as context for the LLM
            contextText = bestClause.getText();
            clauseId = bestClause.getId();
            clauseLabel = bestClause.getLabel();

            String fullClauseText = bestClause.getText();

            // 🔹 Return only a preview of the clause text (e.g., first 400 characters)
            if (fullClauseText != null && fullClauseText.length() > 400) {
                clauseText = fullClauseText.substring(0, 400) + "...";
            } else {
                clauseText = fullClauseText;
            }
        } else {
            // Fallback: use the full contract text if we couldn't pick a clause
            contextText = storedContract.getFullText();
            // We skip clauseText here to avoid dumping the whole contract
            clauseText = null;
        }

        // c) ask the LLM service to answer this question using the chosen context
        String answer = llmService.answerQuestion(contextText, question);

        // d) return the answer + clause info as JSON
        return new AnswerResponse(answer, clauseId, clauseLabel, clauseText);
    }

    // 3️⃣ Debug endpoint to test the clause classifier from Java
    @PostMapping("/debug/classify-clause")
    public Map<String, Object> classifyClause(@RequestBody ClauseTextRequest request) {
        String clauseText = request.getText();
        String label = clauseClassifierService.classifyClause(clauseText);

        Map<String, Object> response = new HashMap<>();
        response.put("text", clauseText);
        response.put("label", label);
        return response;
    }
}