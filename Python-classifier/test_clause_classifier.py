from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F

MODEL_DIR = "clause_classifier_legalbert"


def load_model():
    print("Loading model from:", MODEL_DIR)
    tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR)
    model = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR)
    return tokenizer, model


def classify_clause(text: str, tokenizer, model):
    print("\nClassifying text:")
    print(text)
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
    return label, scores


if __name__ == "__main__":
    tokenizer, model = load_model()

    test_text = "Either party may terminate this Agreement for convenience upon thirty (30) days prior written notice."
    label, scores = classify_clause(test_text, tokenizer, model)

    print("\nPredicted label:", label)
    print("Scores:")
    for k, v in scores.items():
        print(f"  {k}: {v:.3f}")