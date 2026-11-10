# IBM Db2 TechX Summit 2026 — 90-Second & 5-Minute Demo Script

**Project**: DataSentry AI — AI-Powered Enterprise Data Intelligence & Root Cause Analysis Platform  
**Target Audience**: Hackathon Judges, Enterprise Chief Data Officers, IBM Db2 Technical Specialists  

---

## 90-Second Lightning Pitch & Demonstration (Video Script)

### [00:00 - 00:15] Hook & Problem Statement
*Visual: Executive Dashboard with circular score gauge, showing Data Health Index (74/100) and Db2 Connected indicator.*
> "Enterprise data pipelines lose millions every quarter to silent schema drift, duplicate keys, and corrupt numeric records that slip past legacy batch jobs into critical analytical stores. Welcome to **DataSentry AI**, an enterprise intelligence platform engineered for IBM Db2 and Google Gemini that detects data anomalies, investigates root causes, and executes safe remediation."

### [00:15 - 00:35] Data Ingestion & Live Profiling
*Visual: Click 'Upload' -> Select 'Retail Omni-channel Sales Demo' -> Click 'Import & Profile in IBM Db2'.*
> "With one click, we ingest a 100-row enterprise retail dataset into IBM Db2 table `DATASENTRY.RETAIL_TRANSACTIONS`. Our automated profiling engine discovers schemas, infers Db2 SQL data types (`DECIMAL`, `VARCHAR`, `TIMESTAMP`), and calculates an objective Data Quality Score of 68 out of 100 based on completeness, uniqueness, format validity, and statistical distribution."

### [00:35 - 00:55] Grounded Root Cause Diagnostics
*Visual: Click into 'Quality Audit' -> Click Critical Issue 'UNIT_PRICE negative values' -> Root Cause Screen.*
> "Look at this critical defect: 2 negative transaction values detected in `UNIT_PRICE`. Unlike generic dashboards, DataSentry AI clearly separates verified database facts from AI hypotheses. Our Gemini-powered Root Cause Investigator pinpoints the exact etiology: an accounting reversal sign-inversion bug in the upstream ERP billing export service. It delivers verified business impact and immediately generates the exact IBM Db2 remediation DDL constraint: `ALTER TABLE ... ADD CONSTRAINT CHK_UNIT_PRICE_POS CHECK (UNIT_PRICE >= 0)`."

### [00:55 - 01:15] Conversational Copilot with Genuine LangChain Tool Calls
*Visual: Navigate to 'Copilot' -> Click chip: 'Which dataset has the most errors?' -> Tool execution card shows ⚡ SQL Assistant (Db2 query executed in 24ms, 1 row returned).*
> "Now let's consult the AI Copilot. When we ask: 'Which dataset has the most errors?', Gemini doesn't hallucinate. It uses LangChain tool calling to execute `SELECT ... FROM DATASENTRY.QUALITY_RUNS ORDER BY OVERALL_SCORE ASC`. Every answer is backed by live query telemetry, latency metrics, and row citations in our SQL Inspector."

### [01:15 - 01:30] Executive Reporting & Db2 Persistence
*Visual: Navigate to 'Reports' -> Open saved report 'Executive Data Intelligence Report'.*
> "Finally, the Report Generator synthesizes findings into an executive report with an actionable remediation roadmap, permanently stored in IBM Db2 table `DATASENTRY.AI_REPORTS`. DataSentry AI: transforming enterprise data quality from reactive fire-fighting into proactive, intelligent resilience."

---

## 5-Minute Comprehensive Technical Walkthrough

### Step 1: System Health & Db2 Connectivity (0:00 - 0:45)
1. Open the **Top Bar** and tap the **Db2 Connected** badge to open **Settings**.
2. Explain the dual-mode architecture:
   - **Production Mode**: Connects directly to IBM Db2 LUW v12.1+ or Db2 on Cloud (`BLUDB`) via SSL/TLS on port 32551 using parameterized statements.
   - **Demo Mode**: Explicitly labeled synthetic data mode when external credentials are not supplied, ensuring demo records are never misrepresented as live production Db2 data.
3. Review the complete IBM Db2 DDL schema generator with all 7 tables: `DATASETS`, `DATA_ROWS`, `QUALITY_RUNS`, `QUALITY_ISSUES`, `AI_REPORTS`, `CHAT_HISTORY`, and `QUERY_AUDIT`.

### Step 2: Automated Data Profiling & Scoring Engine (0:45 - 1:45)
1. Navigate to **Upload** and import the **Core Banking General Ledger** dataset.
2. Observe real-time parsing, column detection, and data type inference.
3. Navigate to **Data Quality** to review the composite score (82/100):
   - Missing Value Rate: accounts missing GL codes.
   - Duplicate Collisions: identical journal entries.
   - Statistical Anomalies: non-zero ledger balancing flags.

### Step 3: AI Root Cause Investigator (1:45 - 2:45)
1. Select the **Primary/Foreign Key collision** on `ENTRY_ID`.
2. Inspect the **Grounded Db2 Evidence**: `SELECT ENTRY_ID, COUNT(*) FROM ... HAVING COUNT(*) > 1`.
3. Read the **AI Root Cause Hypothesis** (98% confidence): Concurrent worker generation collision without database sequence lock.
4. Review the **Business Impact**: Db2 primary key violation and ledger reconciliation breakdown.
5. Review the **Db2 Remediation SQL**: `ALTER TABLE ... ADD CONSTRAINT PK_ENTRY_ID PRIMARY KEY (ENTRY_ID);`.

### Step 4: AI Copilot & Tool Calling Telemetry (2:45 - 4:00)
1. Go to **AI Copilot**.
2. Test sample queries:
   - *"Find duplicate transactions"* -> Triggers **SQL Assistant** tool and returns verified duplicate tuples.
   - *"Which columns have missing values?"* -> Triggers **Data Profiler** tool.
   - *"What should I fix first?"* -> Triggers **Root Cause Investigator** + **Recommendation Engine**.
3. Point out that the tool execution cards display exact SQL executed, execution duration in milliseconds, and row counts.

### Step 5: SQL Inspector & Query Audit Compliance (4:00 - 4:45)
1. Navigate to **SQL Inspector**.
2. Examine the historical audit trail in `DATASENTRY.QUERY_AUDIT`.
3. Try executing an arbitrary read-only query in the **SQL Playground**.
4. Demonstrate security enforcement: typing `DROP TABLE ...` triggers an immediate security alert preventing mutation.

### Step 6: Conclusion & Judge Summary (4:45 - 5:00)
- Highlight alignment with IBM Db2 TechX Summit judging criteria: real Db2 schema architecture, LangChain tool execution, zero AI hallucinations, and clean enterprise user experience.
