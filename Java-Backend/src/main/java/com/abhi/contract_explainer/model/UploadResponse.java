package com.abhi.contract_explainer.model;

public class UploadResponse {


    private  String contractId;
    private  String summary;

    public UploadResponse(String contractId, String summary)
    {
        this.contractId = contractId;
        this.summary = summary;
    }

    public String getContractId(){
        return contractId;
    }
    public String getSummary(){
        return summary;
    }

}
