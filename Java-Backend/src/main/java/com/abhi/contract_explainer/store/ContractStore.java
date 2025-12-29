package com.abhi.contract_explainer.store;

import com.abhi.contract_explainer.model.StoredContract;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for uploaded contracts.
 *
 * OLD:
 *  - Map<String, String> contracts   (contractId -> fullText only)
 *
 * NEW:
 *  - Map<String, StoredContract> contracts  (contractId -> fullText + clauses)
 */
@Component
public class ContractStore {

    // contractId -> StoredContract (full text + clauses)
    private final Map<String, StoredContract> contracts = new ConcurrentHashMap<>();

    /**
     * Save a StoredContract in memory and return its generated contractId.
     */
    public String save(StoredContract contract) {
        String contractId = UUID.randomUUID().toString();
        contracts.put(contractId, contract);
        return contractId;
    }

    /**
     * Get the StoredContract (full text + clauses) for the given id.
     * Returns null if not found.
     */
    public StoredContract getById(String contractId) {
        return contracts.get(contractId);
    }
}