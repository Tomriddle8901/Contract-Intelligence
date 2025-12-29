import pandas as pd
import ast
from pathlib import Path

# ======== CONFIG: EDIT THIS PART IF YOU WANT DIFFERENT LABELS ========

# These must match the column names in master_clauses.csv EXACTLY.
SELECTED_LABELS = [
    "Termination For Convenience",
    "Non-Compete",
    "Exclusivity",
    "Anti-Assignment",
    "Ip Ownership Assignment",
    "Cap On Liability",
]

MASTER_CSV_PATH = Path("master_clauses.csv")
OUTPUT_PATH = Path("clauses_clean.csv")

# ======== HELPER: CLEAN ONE CELL INTO A LIST OF CLAUSE STRINGS ========

def extract_clauses_from_cell(cell_value):
    """
    Takes a single cell from master_clauses.csv and returns
    a list of clean clause strings.
    Handles:
      - empty / NaN
      - simple string
      - string that looks like a Python list: "['clause1', 'clause2']"
    """
    if pd.isna(cell_value):
        return []

    s = str(cell_value).strip()
    if not s:
        return []

    # If the cell looks like "['...']", try to parse as Python list.
    if s.startswith("[") and s.endswith("]"):
        try:
            parsed = ast.literal_eval(s)
            if isinstance(parsed, list):
                return [str(x).replace("\n", " ").strip() for x in parsed if str(x).strip()]
            else:
                return [str(parsed).replace("\n", " ").strip()]
        except Exception:
            # If parsing fails, just return the raw string
            return [s.replace("\n", " ").strip()]
    else:
        # Normal string
        return [s.replace("\n", " ").strip()]

# ======== MAIN SCRIPT ========

def main():
    if not MASTER_CSV_PATH.exists():
        raise FileNotFoundError(f"Cannot find {MASTER_CSV_PATH}. "
                                f"Make sure CUAD_v1/master_clauses.csv is extracted.")

    print(f"Loading {MASTER_CSV_PATH} ...")
    # engine='python' is a bit more tolerant with weird quoting/newlines
    df = pd.read_csv(MASTER_CSV_PATH, engine="python")

    print("Columns in master_clauses.csv:")
    print(df.columns.tolist())

    missing = [label for label in SELECTED_LABELS if label not in df.columns]
    if missing:
        print("\n⚠️ WARNING: These labels are NOT columns in the CSV:")
        for m in missing:
            print("  -", m)
        print("They will be skipped.\n")

    rows = []

    # Go through each contract (row)
    for _, row in df.iterrows():
        # For each selected label, pull the clause text
        for label in SELECTED_LABELS:
            if label not in df.columns:
                continue  # skip missing labels

            cell_value = row[label]
            clauses = extract_clauses_from_cell(cell_value)

            for clause_text in clauses:
                if clause_text:  # skip empty
                    rows.append({
                        "text": clause_text,
                        "label": label,
                    })

    out_df = pd.DataFrame(rows)
    # Drop exact duplicates just to be neat
    out_df = out_df.drop_duplicates()

    print(f"\nTotal extracted clauses: {len(out_df)}")

    out_df.to_csv(OUTPUT_PATH, index=False)
    print(f"Saved cleaned dataset to: {OUTPUT_PATH.resolve()}")

if __name__ == "__main__":
    main()