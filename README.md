# DataSentry AI — AI-Powered Enterprise Data Intelligence & Root Cause Analysis Platform

> **Submitted to IBM Db2 TechX Summit 2026 Hackathon**  
> *Track: Enterprise AI & Autonomous Database Intelligence*

---

## 1. Problem Statement
Modern enterprises run mission-critical operations on hybrid cloud databases like **IBM Db2**. However, upstream distributed microservices, message queue consumer retry storms, and unvalidated ETL pipelines constantly inject dirty data into operational databases:
- **Silent Schema Drift & Incomplete Records**: Missing customer keys leading to orphaned ledger entries and join failures.
- **Duplicate Records**: Duplicate transactions inflated by at-least-once message delivery without idempotent consumer keys.
- **Statistical Inversions & Typo Anomalies**: Negative quantities or unit prices distorting quarterly revenue calculations.
- **Format Non-Compliance**: Invalid emails, dates, and postal codes that cause downstream CRM failures.

Engineering and business teams spend over **40% of their engineering sprint cycles** manually tracing corrupted data back to upstream source code commits.

---

## 2. Solution Overview
**DataSentry AI** provides an autonomous data quality assurance and root-cause diagnostic intelligence layer for IBM Db2. Combining **IBM Db2**, **Google Gemini**, and **LangChain.js tool-calling agents**, DataSentry AI:
1. **Automatically Profiles Ingested Datasets** across completeness, uniqueness, validity, and statistical distribution.
2. **Generates an Objective Data Health Score (0 - 100)** with transparent penalty weights.
3. **Clearly Distinguishes Grounded Database Facts from AI Hypotheses**.
4. **Traces Root Causes** to upstream microservices, network timeouts, and serialization errors with confidence scoring.
5. **Generates Actionable IBM Db2 DDL Remediation SQL** (such as domain `CHECK` constraints and `NOT NULL` rules).
6. **Empowers Executives via an AI Copilot** that calls real database tools rather than hallucinating answers.
7. **Maintains a Permanent Audit Trail** in Db2 table `DATASENTRY.QUERY_AUDIT`.

---

## 3. Technology Stack

### Database:
- **IBM Db2** (LUW v11.5 / v12.1+ and Db2 on Cloud / `BLUDB`)
- **Official `ibm_db` Driver** for Node.js
- **Parameterized SQL** with zero SQL-injection exposure
- **Relational Tables & Schema**: `DATASETS`, `DATA_ROWS`, `QUALITY_RUNS`, `QUALITY_ISSUES`, `AI_REPORTS`, `CHAT_HISTORY`, `QUERY_AUDIT`
- **Native IBM Db2 Vector Search** (`VECTOR(768, FLOAT)` & `VECTOR_DISTANCE`) with relational full-text fallback

### AI & Agent Framework:
- **Google Gemini API** (`gemini-2.5-pro` & `gemini-2.5-flash`)
- **LangChain.js** Structured Tool-Calling Agents
- **6 Domain Tools**:
  1. `Schema Inspector` (Db2 catalog inspector)
  2. `Data Profiler` (statistical anomaly & completeness analyzer)
  3. `SQL Assistant` (safe read-only SQL generator & validator)
  4. `Root Cause Investigator` (upstream failure mode analyzer)
  5. `Recommendation Engine` (Db2 DDL & schema constraint synthesizer)
  6. `Report Generator` (executive intelligence report compiler)

### Frontend & Executive UI:
- **Jetpack Compose (Android Streaming Emulator)** / **React + Tailwind CSS**
- **Material Design 3 (M3)** with custom Enterprise Navy theme (`#080E1E`, `#0F62FE`, `#00E5FF`)
- **Custom Canvas Circular Score Gauge** with animated spring transitions
- **Interactive Data Table Preview** and **SQL Inspector**

---

## 4. Key Features

| Feature | Description |
|---|---|
| **Executive Dashboard** | Real-time Data Health Index (0-100), active datasets, total indexed records, critical defects, and quality trends. |
| **Automated Data Profiler** | Automatic detection of nulls, duplicate keys, format regex violations, and 3-sigma statistical outliers. |
| **AI Root Cause Investigator** | In-depth failure analysis explaining *why* defects occurred, their business impact, and how to fix them. |
| **Conversational Copilot** | Natural language queries executed via genuine LangChain tool calls with visible SQL execution cards and row counts. |
| **Executive Reports** | Multi-section management audit reports persisted permanently in IBM Db2 table `DATASENTRY.AI_REPORTS`. |
| **Live SQL Inspector** | Full governance audit log with latency tracking in milliseconds, returned record counts, and a safe read-only SQL playground. |
| **Dual-Mode Connectivity** | Transparent toggle between live SSL IBM Db2 connection and labeled synthetic Demo Mode. |

---

## 5. IBM Db2 Setup Instructions

### 5.1 Provisioning Db2 Instance
DataSentry AI connects to any IBM Db2 instance (on-premises LUW or Db2 on Cloud).
1. Configure credentials in `.env`:
   ```properties
   DB2_DATABASE=BLUDB
   DB2_HOSTNAME=db2w-your-instance.databases.appdomain.cloud
   DB2_PORT=32551
   DB2_PROTOCOL=TCPIP
   DB2_UID=db2admin
   DB2_PWD=your_secure_password
   DB2_SECURITY=SSL
   DB2_SCHEMA=DATASENTRY
   ```
2. Execute the initialization DDL script found in `database-schema.md` or via the **Settings -> View DDL** button in the app.

---

## 6. Execution & Testing Instructions

### Android Executive Client:
1. The app compiles and launches automatically in the AI Studio Streaming Android Emulator.
2. Run automated test suite:
   ```bash
   gradle :app:testDebugUnitTest
   ```
   *Result: 4 tests executed, 0 failures (100% pass rate).*

### Node.js Backend:
```bash
# Install dependencies
npm install

# Run backend development server
npm run dev

# Run test suite
npm test
```

---

## 7. Demonstration Workflow for Judges

1. **Launch App**: Observe the Executive Dashboard with animated score gauge and Db2 status badge.
2. **Ingest Data**: Go to **Upload**, tap **1-Click Benchmark: Retail Omni-channel Sales**, and click **Import & Profile in IBM Db2**.
3. **Inspect Quality**: Go to **Data Quality** to see discovered missing values, negative prices, and duplicate order hashes.
4. **Investigate Root Cause**: Tap the **Negative UNIT_PRICE** defect. Read the verified root-cause hypothesis and Db2 remediation DDL constraint.
5. **Chat with AI Copilot**: Open **AI Copilot** and tap *"Which dataset has the most errors?"*. Watch the LangChain **SQL Assistant** tool execute real Db2 queries and return grounded telemetry.
6. **Query Audit**: Open **SQL Inspector** to examine the immutable telemetry record in `DATASENTRY.QUERY_AUDIT`.

---

## 8. Limitations & Future Roadmap
- Direct DDL modification from the AI Copilot is intentionally gated behind human confirmation for production database safety.
- Native Db2 vector search requires Db2 v12.1+ with vector extensions; relational search is automatically active on earlier versions.
