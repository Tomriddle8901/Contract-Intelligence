from datasets import load_dataset, Dataset
from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
    TrainingArguments,
    Trainer,
)
from sklearn.metrics import accuracy_score, f1_score
from sklearn.model_selection import train_test_split
import numpy as np
import os

DATA_FILE = "clauses_clean.csv"
MODEL_NAME = "nlpaueb/legal-bert-base-uncased"
OUTPUT_DIR = "clause_classifier_legalbert"


def main():
    if not os.path.exists(DATA_FILE):
        raise FileNotFoundError(f"{DATA_FILE} not found in {os.getcwd()}")

    # 1. Load CSV as one Hugging Face dataset
    raw = load_dataset("csv", data_files={"train": DATA_FILE})
    all_ds = raw["train"]

    # 2. Get label names from the file
    labels = sorted(list(set(all_ds["label"])))
    print("Found labels:", labels)

    label2id = {label: i for i, label in enumerate(labels)}
    id2label = {i: label for label, i in label2id.items()}
    print("Label2id mapping:", label2id)

    # 3. Add numeric label column
    def encode_label(example):
        example["label_id"] = label2id[example["label"]]
        return example

    all_ds = all_ds.map(encode_label)

    # 4. Use scikit-learn for stratified train/test split
    df = all_ds.to_pandas()
    train_df, eval_df = train_test_split(
        df,
        test_size=0.2,
        random_state=42,
        stratify=df["label_id"],
    )

    train_ds = Dataset.from_pandas(train_df, preserve_index=False)
    eval_ds = Dataset.from_pandas(eval_df, preserve_index=False)

    print("Train size:", len(train_ds), "Eval size:", len(eval_ds))

    # 5. Tokenizer
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

    def tokenize_batch(batch):
        return tokenizer(
            batch["text"],
            truncation=True,
            padding="max_length",
            max_length=512,
        )

    train_ds = train_ds.map(tokenize_batch, batched=True)
    eval_ds = eval_ds.map(tokenize_batch, batched=True)

    # 6. Set torch format
    train_ds = train_ds.rename_column("label_id", "labels")
    eval_ds = eval_ds.rename_column("label_id", "labels")

    train_ds.set_format(
        type="torch",
        columns=["input_ids", "attention_mask", "labels"],
    )
    eval_ds.set_format(
        type="torch",
        columns=["input_ids", "attention_mask", "labels"],
    )

    # 7. Load base model
    model = AutoModelForSequenceClassification.from_pretrained(
        MODEL_NAME,
        num_labels=len(labels),
        id2label=id2label,
        label2id=label2id,
    )

    # 8. Metrics
    def compute_metrics(eval_pred):
        logits, labels_ids = eval_pred
        preds = np.argmax(logits, axis=-1)

        acc = accuracy_score(labels_ids, preds)
        macro_f1 = f1_score(labels_ids, preds, average="macro")

        return {
            "accuracy": acc,
            "macro_f1": macro_f1,
        }

    # 9. Training arguments — **no evaluation_strategy here**
    training_args = TrainingArguments(
        output_dir="clause_classifier_ckpt",
        learning_rate=5e-5,
        per_device_train_batch_size=8,
        per_device_eval_batch_size=8,
        num_train_epochs=5,
        weight_decay=0.01,
        logging_steps=10,
)
    

    # 10. Trainer
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_ds,
        eval_dataset=eval_ds,
        tokenizer=tokenizer,
        compute_metrics=compute_metrics,
    )

    # 11. Train
    trainer.train()

    # 11.5 Evaluate after training
    metrics = trainer.evaluate()
    print("Eval metrics:", metrics)

    # 12. Save final model
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    model.save_pretrained(OUTPUT_DIR)
    tokenizer.save_pretrained(OUTPUT_DIR)

    print(f"✅ Training complete. Model saved to: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()