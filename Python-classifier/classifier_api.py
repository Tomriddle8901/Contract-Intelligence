from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F

MODEL_DIR = "clause_classifier_legalbert"

app = FastAPI(title="Clause Classifier API")


class ClassifyRequest(BaseModel):
    text: str


class ClassifyResponse(BaseModel):
    label: str
    scores: dict


# Load model & tokenizer once at startup
print("Loading model from:", MODEL_DIR)
tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR)
model.eval()


@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.post("/classify", response_model=ClassifyResponse)
def classify(req: ClassifyRequest):
    text = req.text

    inputs = tokenizer(
        text,
        return_tensors="pt",
        truncation=True,
        padding=True,
        max_length=512,
    )

    with torch.no_grad():
        outputs = model(**inputs)
        probs = F.softmax(outputs.logits, dim=-1)[0]

    pred_id = int(torch.argmax(probs))
    label = model.config.id2label[pred_id]
    scores = {
        model.config.id2label[i]: float(probs[i])
        for i in range(len(probs))
    }

    return ClassifyResponse(label=label, scores=scores)