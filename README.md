# Contract Intelligence – Contract & Policy Explainer ⚖️🤖

A Java + ML + LLM backend that helps users **understand contract PDFs in simple language**.

Upload a contract → ask a question → get:

- the **most relevant clause**,
- a **legal-ish label** (e.g. Termination, Confidentiality),
- and a **plain-language explanation** powered by Llama 3.


---

## 🧩 High-Level Idea

I wanted to build something more real than a “hello world” ML demo:

> *“What if I could upload a real contract and actually ask it questions in plain English?”*

So this project combines:

- **Java / Spring Boot** – core backend, PDF handling, REST APIs  
- **Python / FastAPI + LegalBERT** – clause classification  
- **Llama 3 via Ollama** – summaries + explanations  
- **HTML / JS** – a minimal UI to tie it all together

---

## 🖼 Architecture Overview

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/570074cd-8cfc-410b-ad95-21d0e88a0343" />


---

## 🔁 End-to-End Flow

1. **User uploads a contract PDF** in the browser.
2. **Spring Boot backend**:
   - extracts text from the PDF,
   - splits it into clauses,
   - stores contract + clauses in memory,
   - calls Llama 3 (via Ollama) to generate a **bullet-point summary**.
3. **User asks a question**, e.g. _“If I resign, how much notice do I need to give?”_
4. Backend:
   - picks the **most relevant clause** for that question,
   - sends the clause text to the **Python classifier API** (FastAPI + fine-tuned LegalBERT),
   - receives a **clause label** (e.g. `Termination For Convenience`),
   - calls **Llama 3** again with:
     - the clause text  
     - the user’s question  
   - Llama 3 returns:
     - a **quoted “Relevant contract text”** block
     - an **“Explanation”** in simple language.
5. The **frontend** displays:
   - contract summary,
   - matched clause snippet,
   - clause label,
   - explanation (with “This is not legal advice.”).

---

## 🧱 Tech Stack

**Backend / API**

- Java (17 / 21)
- Spring Boot
- PDF parsing (e.g. Apache PDFBox) in `PdfService`
- REST endpoints:
  - `POST /api/contracts/upload`
  - `POST /api/contracts/{id}/ask`

**ML / Classifier**

- Python 3.9+
- FastAPI + Uvicorn
- Hugging Face Transformers
- PyTorch
- LegalBERT base model: `nlpaueb/legal-bert-base-uncased`
- Trained on a cleaned subset of **CUAD** (CUAD_v1) → `clauses_clean.csv`

**LLM Layer**

- [Ollama](https://ollama.com/)
- `llama3` model pulled and run locally
- Java `HttpClient` → `http://localhost:11434/api/chat`

**Frontend**

- Single `index.html` served by Spring Boot (`src/main/resources/static/index.html`)
- Vanilla HTML/CSS/JS
- Uses `fetch()` to call the Spring endpoints

---

## 📁 Project Structure

```text
Contract-Intelligence/
  ├─ Java-Backend/
  │   ├─ src/main/java/com/abhi/contract_explainer/
  │   │   ├─ controller/           # REST controllers
  │   │   ├─ service/              # PdfService, LlmService, ClauseSelectionService, ...
  │   │   ├─ model/                # DTOs: UploadResponse, QuestionRequest, AnswerResponse, ClauseMatch, ...
  │   │   └─ store/                # ContractStore (in-memory)
  │   ├─ src/main/resources/
  │   │   ├─ static/index.html     # Minimal UI: upload + ask
  │   │   └─ application.properties
  │   └─ pom.xml
  │
  ├─ Python-classifier/
  │   ├─ clauses_clean.csv         # Cleaned CUAD subset: text + label
  │   ├─ train_clause_classifier.py # Fine-tunes LegalBERT
  │   ├─ classifier_api.py         # FastAPI: POST /classify
  │   ├─ requirements.txt          # Python dependencies (you create this)
  │   └─ (ignored in git)
  │       ├─ clause_classifier_legalbert/  # Fine-tuned model weights (local only)
  │       └─ venv/                        # Python virtualenv
  │
  └─ README.md
