# IBM Db2 Database Schema & Data Dictionary

**Schema**: `DATASENTRY`  
**Compatibility**: IBM Db2 LUW v11.5, v12.1+ and IBM Db2 on Cloud (BLUDB)  
**Encoding**: UTF-8  

---

## 1. Table Definitions

### 1.1 `DATASENTRY.DATASETS`
Stores metadata for ingested business datasets.
| Column | Db2 Data Type | Constraints | Description |
|---|---|---|---|
| `ID` | `VARCHAR(64)` | `NOT NULL PRIMARY KEY` | Unique UUID identifier |
| `NAME` | `VARCHAR(255)` | `NOT NULL` | Dataset display name |
| `DESCRIPTION` | `CLOB(64K)` | | Business context and provenance |
| `ROW_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | Total number of row records |
| `COLUMN_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | Total number of attributes |
| `COLUMNS_JSON` | `CLOB(1M)` | | Serialized schema & inferred Db2 types |
| `FILE_SIZE` | `VARCHAR(32)` | | Human-readable size |
| `CREATED_AT` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT TIMESTAMP` | Ingestion timestamp |
| `STATUS` | `VARCHAR(32)` | `NOT NULL DEFAULT 'ACTIVE'` | Table status |
| `IS_DEMO` | `SMALLINT` | `NOT NULL DEFAULT 0` | 1 if synthetic hackathon dataset, 0 if production |
| `DB2_TABLE_NAME` | `VARCHAR(128)` | | Target Db2 physical table name |

**Indexes**:
- `CREATE INDEX DATASENTRY.IDX_DATASETS_CREATED ON DATASENTRY.DATASETS(CREATED_AT DESC);`

---

### 1.2 `DATASENTRY.DATA_ROWS`
Stores imported row-level payload data with JSON serialization.
| Column | Db2 Data Type | Constraints | Description |
|---|---|---|---|
| `ID` | `BIGINT` | `NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Sequential row ID |
| `DATASET_ID` | `VARCHAR(64)` | `NOT NULL, FK -> DATASETS(ID) ON DELETE CASCADE` | Parent dataset |
| `ROW_INDEX` | `INTEGER` | `NOT NULL` | Original row index |
| `ROW_DATA_JSON` | `CLOB(2M)` | `NOT NULL` | Raw tuple values as key-value pairs |
| `INGESTED_AT` | `TIMESTAMP` | `DEFAULT CURRENT TIMESTAMP` | Storage timestamp |

**Indexes**:
- `CREATE INDEX DATASENTRY.IDX_ROWS_DATASET ON DATASENTRY.DATA_ROWS(DATASET_ID, ROW_INDEX);`

---

### 1.3 `DATASENTRY.QUALITY_RUNS`
Historical records of automated profiling runs.
| Column | Db2 Data Type | Constraints | Description |
|---|---|---|---|
| `ID` | `VARCHAR(64)` | `NOT NULL PRIMARY KEY` | Quality run UUID |
| `DATASET_ID` | `VARCHAR(64)` | `NOT NULL, FK -> DATASETS(ID) ON DELETE CASCADE` | Associated dataset |
| `OVERALL_SCORE` | `INTEGER` | `NOT NULL` | Composite quality index (0 - 100) |
| `MISSING_VALUES_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | Total null or empty fields |
| `DUPLICATE_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | Duplicate records / keys |
| `FORMAT_ERRORS_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | RFC regex or type errors |
| `ANOMALY_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | Outliers & negative quantities |
| `STATUS` | `VARCHAR(32)` | `NOT NULL DEFAULT 'COMPLETED'` | Execution state |
| `RUN_TIMESTAMP` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT TIMESTAMP` | Execution timestamp |
| `EXECUTION_TIME_MS` | `BIGINT` | `NOT NULL DEFAULT 0` | Profiling duration in ms |

**Indexes**:
- `CREATE INDEX DATASENTRY.IDX_RUNS_DATASET ON DATASENTRY.QUALITY_RUNS(DATASET_ID, RUN_TIMESTAMP DESC);`

---

### 1.4 `DATASENTRY.QUALITY_ISSUES`
Individual discovered data defects with root-cause diagnostic annotations.
| Column | Db2 Data Type | Constraints | Description |
|---|---|---|---|
| `ID` | `VARCHAR(64)` | `NOT NULL PRIMARY KEY` | Issue UUID |
| `RUN_ID` | `VARCHAR(64)` | `NOT NULL, FK -> QUALITY_RUNS(ID) ON DELETE CASCADE` | Parent run |
| `DATASET_ID` | `VARCHAR(64)` | `NOT NULL` | Associated dataset |
| `ISSUE_TYPE` | `VARCHAR(64)` | `NOT NULL` | `MISSING_VALUE`, `DUPLICATE`, `STATISTICAL_ANOMALY`, etc. |
| `SEVERITY` | `VARCHAR(16)` | `NOT NULL` | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| `COLUMN_NAME` | `VARCHAR(128)` | `NOT NULL` | Affected attribute |
| `AFFECTED_RECORDS_COUNT`| `INTEGER` | `NOT NULL` | Impacted row count |
| `DESCRIPTION` | `CLOB(64K)` | `NOT NULL` | Detected defect description (Ground truth) |
| `ROOT_CAUSE_HYPOTHESIS`| `CLOB(128K)`| | AI-inferred architectural etiology |
| `BUSINESS_IMPACT` | `CLOB(128K)`| | Financial & reporting risk |
| `SUGGESTED_REMEDIATION`| `CLOB(128K)`| | Actionable engineering fix |
| `REMEDIATION_SQL` | `CLOB(64K)` | | Executable Db2 DDL / DML fix |
| `CONFIDENCE_SCORE` | `DECIMAL(4,3)`| `NOT NULL DEFAULT 0.950` | AI confidence rating (0.000 to 1.000) |
| `EVIDENCE_SNIPPET` | `CLOB(64K)` | | Grounded SQL verification proof |
| `EMBEDDING_VECTOR` | `VECTOR(768, FLOAT)` | Optional | Semantic embedding for Db2 vector search |

**Indexes**:
- `CREATE INDEX DATASENTRY.IDX_ISSUES_SEVERITY ON DATASENTRY.QUALITY_ISSUES(SEVERITY, ISSUE_TYPE);`
- `CREATE INDEX DATASENTRY.IDX_ISSUES_DATASET ON DATASENTRY.QUALITY_ISSUES(DATASET_ID);`

---

### 1.5 `DATASENTRY.AI_REPORTS`
Persisted executive data quality intelligence reports.
| Column | Db2 Data Type | Constraints | Description |
|---|---|---|---|
| `ID` | `VARCHAR(64)` | `NOT NULL PRIMARY KEY` | Report UUID |
| `DATASET_ID` | `VARCHAR(64)` | `NOT NULL, FK -> DATASETS(ID) ON DELETE CASCADE` | Source dataset |
| `DATASET_NAME` | `VARCHAR(255)` | `NOT NULL` | Dataset title |
| `TITLE` | `VARCHAR(255)` | `NOT NULL` | Report headline |
| `EXECUTIVE_SUMMARY` | `CLOB(256K)` | `NOT NULL` | Strategic assessment summary |
| `QUALITY_SCORE` | `INTEGER` | `NOT NULL` | Verified reliability score |
| `KEY_FINDINGS_JSON` | `CLOB(256K)` | | Priority audit findings |
| `RECOMMENDATIONS_JSON`| `CLOB(256K)` | | Engineering roadmap |
| `BUSINESS_IMPACT` | `CLOB(128K)` | | Financial loss prevention analysis |
| `GENERATED_AT` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT TIMESTAMP` | Generation timestamp |

---

### 1.6 `DATASENTRY.QUERY_AUDIT`
Immutable governance audit trail of all SQL executed by AI and users.
| Column | Db2 Data Type | Constraints | Description |
|---|---|---|---|
| `ID` | `VARCHAR(64)` | `NOT NULL PRIMARY KEY` | Audit record UUID |
| `USER_PROMPT` | `CLOB(32K)` | | Input natural language query |
| `TOOL_SELECTED` | `VARCHAR(128)` | `NOT NULL` | LangChain tool name invoked |
| `SQL_QUERY` | `CLOB(64K)` | `NOT NULL` | Raw SQL executed against Db2 |
| `EXECUTION_STATUS` | `VARCHAR(32)` | `NOT NULL` | `SUCCESS` or `FAILED` |
| `EXECUTION_TIME_MS` | `BIGINT` | `NOT NULL` | Execution latency in milliseconds |
| `ROW_COUNT` | `INTEGER` | `NOT NULL DEFAULT 0` | Number of returned records |
| `AUDIT_TIMESTAMP` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT TIMESTAMP` | Execution timestamp |
| `DB_TARGET` | `VARCHAR(64)` | `NOT NULL DEFAULT 'IBM Db2 LUW'` | Database instance identifier |

**Indexes**:
- `CREATE INDEX DATASENTRY.IDX_AUDIT_TIME ON DATASENTRY.QUERY_AUDIT(AUDIT_TIMESTAMP DESC);`

---

## 2. IBM Db2 Vector Search Compatibility
When running on Db2 LUW v12.1+ with vector extensions enabled, semantic search over historical root causes is executed natively via:
```sql
SELECT ID, ISSUE_TYPE, SEVERITY, DESCRIPTION, ROOT_CAUSE_HYPOTHESIS,
       VECTOR_DISTANCE(EMBEDDING_VECTOR, ?, 'COSINE') AS SIMILARITY_DISTANCE
FROM DATASENTRY.QUALITY_ISSUES
WHERE EMBEDDING_VECTOR IS NOT NULL
ORDER BY SIMILARITY_DISTANCE ASC
FETCH FIRST 5 ROWS ONLY;
```
If vector extensions are unavailable on earlier Db2 versions (v11.5), DataSentry AI automatically falls back to indexed relational full-text search without interruption:
```sql
SELECT ID, ISSUE_TYPE, SEVERITY, DESCRIPTION, ROOT_CAUSE_HYPOTHESIS
FROM DATASENTRY.QUALITY_ISSUES
WHERE LOWER(DESCRIPTION) LIKE LOWER(?)
   OR LOWER(ROOT_CAUSE_HYPOTHESIS) LIKE LOWER(?)
ORDER BY CASE SEVERITY WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END
FETCH FIRST 5 ROWS ONLY;
```
