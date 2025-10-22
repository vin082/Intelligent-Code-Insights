# Intelligent Code Insights - Professional Flow Diagrams

## 1. Complete System Flow - High Level

```mermaid
graph TB
    Start([User enters natural language query]) --> QualityCheck{Query Quality<br/>Assessment}

    QualityCheck -->|Poor Quality| Rewrite1[Rewrite Query<br/>Make it codebase-specific]
    QualityCheck -->|Good Quality| Retrieve[Vector Search<br/>FAISS Similarity]
    Rewrite1 --> Retrieve

    Retrieve --> Filter[Relationship Filtering<br/>Apply metadata filters]
    Filter --> Grade[Document Grading<br/>LLM judges relevance]

    Grade --> RelevantCheck{Any Relevant<br/>Documents?}

    RelevantCheck -->|None Found & Retries < 2| Rewrite2[Rewrite Query<br/>Attempt #2]
    RelevantCheck -->|None Found & Max Retries| NoResults[Return: No relevant<br/>code found]
    RelevantCheck -->|Found Relevant| Generate[Generate Answer<br/>Using relevant code]

    Rewrite2 --> Retrieve

    Generate --> Reflect[Self-Reflection<br/>Quality validation]

    Reflect --> QualityGood{Answer Quality<br/>Acceptable?}

    QualityGood -->|Poor & Retries < 2| Rewrite3[Rewrite Query<br/>Attempt #3]
    QualityGood -->|Poor & Max Retries| Finalize1[Finalize<br/>Best available answer]
    QualityGood -->|Good| Finalize2[Finalize<br/>Validated answer]

    Rewrite3 --> Retrieve

    Finalize1 --> End([Display answer to user])
    Finalize2 --> End
    NoResults --> End

    style Start fill:#e1f5fe
    style End fill:#c8e6c9
    style QualityCheck fill:#fff9c4
    style RelevantCheck fill:#fff9c4
    style QualityGood fill:#fff9c4
    style Generate fill:#f3e5f5
    style Reflect fill:#f3e5f5
    style NoResults fill:#ffcdd2
```

## 2. Indexing Pipeline Flow

```mermaid
graph LR
    A[Java Codebase<br/>*.java files] --> B[Directory Loader<br/>Scan & Load]
    B --> C[Text Splitter<br/>Code-aware chunking<br/>Size: 1500 chars<br/>Overlap: 300]
    C --> D[Metadata Extractor<br/>Parse Java structure]
    D --> E[Extract Relationships:<br/>• imports<br/>• classes<br/>• methods<br/>• calls<br/>• extends<br/>• implements]
    E --> F[OpenAI Embeddings<br/>text-embedding-3-large<br/>3072 dimensions]
    F --> G[FAISS Vector Store<br/>Indexed & Ready]

    style A fill:#e3f2fd
    style B fill:#f3e5f5
    style C fill:#fff9c4
    style D fill:#ffe0b2
    style E fill:#c8e6c9
    style F fill:#f3e5f5
    style G fill:#c5e1a5
```

## 3. Query Processing - Detailed Workflow

```mermaid
flowchart TD
    Start([User Query]) --> Parse[Parse Query Intent]

    Parse --> Q1{Query Length<br/>& Clarity}
    Q1 -->|< 2 words or typos| RW1[Rewrite Node:<br/>Improve specificity]
    Q1 -->|Good| LLMCheck[LLM Quality Check:<br/>Context sufficient?]

    RW1 --> Embed
    LLMCheck -->|Needs improvement| RW2[Rewrite Node:<br/>Add technical context]
    LLMCheck -->|Approved| Embed[Embed Query<br/>Convert to vector]
    RW2 --> Embed

    Embed --> VS[Vector Search<br/>Retrieve top-k chunks<br/>k=4 by default]

    VS --> DetectType{Query Type<br/>Detection}
    DetectType -->|Semantic<br/>'How does X work?'| Chunks1[Return all<br/>retrieved chunks]
    DetectType -->|Structural<br/>'What calls X?'| RelFilter[Relationship Filter:<br/>• Detect keywords<br/>• Extract target<br/>• Filter by metadata]

    RelFilter --> Chunks2[Return filtered<br/>chunks]

    Chunks1 --> Grade[Grading Node]
    Chunks2 --> Grade

    Grade --> GradeLoop[For each chunk:<br/>Ask LLM if relevant]
    GradeLoop --> Score1[Chunk 1: yes/no]
    GradeLoop --> Score2[Chunk 2: yes/no]
    GradeLoop --> Score3[Chunk 3: yes/no]
    GradeLoop --> Score4[Chunk 4: yes/no]

    Score1 --> Check
    Score2 --> Check
    Score3 --> Check
    Score4 --> Check{Any marked<br/>as relevant?}

    Check -->|None & Retry<1| Retry1[Increment retry<br/>Rewrite query]
    Check -->|None & Retry=2| NoneFound[No relevant code<br/>Generate honest response]
    Check -->|≥1 relevant| Context[Build Context:<br/>Concatenate relevant chunks<br/>Include file paths]

    Retry1 --> Embed

    Context --> GenPrompt[Generate Prompt:<br/>Question + Code context<br/>+ Instructions]
    GenPrompt --> LLMGen[GPT-4o Generation:<br/>Create detailed answer]
    LLMGen --> Answer[Generated Answer]

    Answer --> SelfReflect[Self-Reflection Node:<br/>Quality assessment]

    SelfReflect --> RefPrompt[Reflection Prompt:<br/>• Addresses question?<br/>• Specific references?<br/>• Logical?<br/>• No hallucinations?]

    RefPrompt --> RefCheck{Quality<br/>Assessment}

    RefCheck -->|Poor & Retry<1| Retry2[Increment retry<br/>Rewrite query]
    RefCheck -->|Poor & Retry=2| FinalBest[Finalize:<br/>Best available answer]
    RefCheck -->|Good| FinalGood[Finalize:<br/>Validated answer]

    Retry2 --> Embed

    NoneFound --> Display
    FinalBest --> Display
    FinalGood --> Display([Display to User<br/>with file references])

    style Start fill:#e1f5fe
    style Display fill:#c8e6c9
    style VS fill:#f3e5f5
    style Grade fill:#fff9c4
    style LLMGen fill:#f3e5f5
    style SelfReflect fill:#ffe0b2
    style NoneFound fill:#ffcdd2
```

## 4. Retrieval Strategy - Hybrid Approach

```mermaid
graph TB
    subgraph "Input"
        Query[User Query:<br/>'What calls OrderService?']
    end

    subgraph "Query Analysis"
        Embed[Embed Query<br/>Vector representation]
        Detect[Detect Query Type:<br/>Check for keywords]
        Detect -->|Contains 'calls'| Structural[Structural Query]
        Detect -->|No keywords| Semantic[Semantic Query]
    end

    subgraph "Vector Retrieval"
        Search[FAISS Search<br/>Cosine similarity]
        Results[Top 4 chunks<br/>with metadata]
    end

    subgraph "Post-Processing"
        Semantic --> NoFilter[Use all retrieved<br/>chunks as-is]
        Structural --> Filter[Relationship Filter]
        Filter --> Extract[Extract target:<br/>'OrderService']
        Extract --> MetaCheck[Check metadata:<br/>method_calls field]
        MetaCheck --> Filtered[Keep only chunks<br/>that call target]
    end

    subgraph "Output"
        NoFilter --> Final1[4 chunks]
        Filtered --> Final2[2-3 filtered chunks<br/>High precision]
    end

    Query --> Embed
    Query --> Detect
    Embed --> Search
    Search --> Results
    Results --> NoFilter
    Results --> Filter

    style Query fill:#e1f5fe
    style Structural fill:#ffcdd2
    style Semantic fill:#c8e6c9
    style Filter fill:#fff9c4
    style Final2 fill:#c8e6c9
```

## 5. LLM Interaction Points

```mermaid
flowchart LR
    subgraph "LLM Touchpoints in Workflow"
        A[1. Query Quality Check<br/>Model: GPT-4o<br/>Task: Assess clarity]
        B[2. Query Rewriting<br/>Model: GPT-4o<br/>Task: Improve specificity]
        C[3. Document Grading<br/>Model: GPT-4o<br/>Task: Judge relevance<br/>Called 4x per query]
        D[4. Answer Generation<br/>Model: GPT-4o<br/>Task: Create response]
        E[5. Self-Reflection<br/>Model: GPT-4o<br/>Task: Validate quality]
    end

    Start([Query Start]) --> A
    A -->|Poor| B
    B --> Retrieve[Vector Search]
    A -->|Good| Retrieve
    Retrieve --> C
    C --> D
    D --> E
    E -->|Good| End([Query End])
    E -->|Poor| B

    style A fill:#f3e5f5
    style B fill:#f3e5f5
    style C fill:#f3e5f5
    style D fill:#f3e5f5
    style E fill:#f3e5f5
```

## 6. State Management Flow

```mermaid
stateDiagram-v2
    [*] --> QueryReceived: User input

    QueryReceived --> QueryQuality: Assess quality

    QueryQuality --> Rewriting: needs_improvement = True
    QueryQuality --> Retrieval: needs_improvement = False

    Rewriting --> Retrieval: rewritten_query updated

    Retrieval --> Grading: retrieved_code populated

    Grading --> RetrievalRetry: any_relevant = False<br/>retry_count < 2
    Grading --> Generation: any_relevant = True
    Grading --> NoResults: any_relevant = False<br/>retry_count = 2

    RetrievalRetry --> Rewriting: Increment retry_count

    Generation --> Reflection: generation populated

    Reflection --> ReflectionRetry: answer_quality_good = False<br/>retry_count < 2
    Reflection --> Finalization: answer_quality_good = True
    Reflection --> Finalization: answer_quality_good = False<br/>retry_count = 2

    ReflectionRetry --> Rewriting: Increment retry_count

    NoResults --> Finalization: final_answer = "Not found"
    Finalization --> [*]: Return final_answer

    note right of QueryReceived
        GraphState initialized:
        - question: str
        - intermediate_steps: []
        - retry_count: 0
    end note

    note right of Grading
        State updated:
        - grading_scores: List
        - any_relevant: bool
    end note

    note right of Generation
        State updated:
        - generation: str
        - code_files: List
    end note
```

## 7. Architecture Layers

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[Streamlit Web UI<br/>Port: 8501]
        UI1[Chat Interface]
        UI2[File References]
        UI3[Code Snippets]
        UI4[Process Steps]
        UI --> UI1
        UI --> UI2
        UI --> UI3
        UI --> UI4
    end

    subgraph "Orchestration Layer"
        LG[LangGraph State Machine]
        N1[Query Quality Node]
        N2[Rewrite Node]
        N3[Retrieve Node]
        N4[Grade Node]
        N5[Generate Node]
        N6[Reflect Node]
        N7[Finalize Node]
        LG --> N1
        LG --> N2
        LG --> N3
        LG --> N4
        LG --> N5
        LG --> N6
        LG --> N7
    end

    subgraph "Service Layer"
        DL[Document Loader]
        VS[Vector Store Service]
        LLM[LLM Service]
        DL --> Load[Load Java Files]
        VS --> Search[Similarity Search]
        LLM --> Call[OpenAI API Calls]
    end

    subgraph "Data Layer"
        FAISS[(FAISS Index<br/>Vector Embeddings)]
        Meta[(Metadata<br/>Relationships)]
        Cache[(Session Cache<br/>State)]
    end

    subgraph "External Services"
        OpenAI[OpenAI API<br/>• GPT-4o<br/>• text-embedding-3-large]
    end

    UI1 --> LG
    N3 --> VS
    N1 --> LLM
    N2 --> LLM
    N4 --> LLM
    N5 --> LLM
    N6 --> LLM

    VS --> FAISS
    N3 --> Meta
    LG --> Cache

    LLM --> OpenAI
    DL --> FAISS

    style UI fill:#e1f5fe
    style LG fill:#f3e5f5
    style FAISS fill:#c8e6c9
    style OpenAI fill:#fff9c4
```

## 8. Data Flow - End to End

```mermaid
sequenceDiagram
    actor User
    participant UI as Streamlit UI
    participant WF as LangGraph Workflow
    participant LLM as LLM Service
    participant VS as Vector Store
    participant FAISS as FAISS Index

    User->>UI: Enter query: "What calls OrderService?"
    UI->>WF: Initialize GraphState

    WF->>WF: Query Quality Node
    WF->>LLM: Check quality
    LLM-->>WF: Quality: Good

    WF->>WF: Retrieve Node
    WF->>VS: Get embeddings for query
    VS->>FAISS: Similarity search
    FAISS-->>VS: Top 4 chunks
    VS-->>WF: Retrieved chunks with metadata

    WF->>WF: Relationship Filter
    Note over WF: Detect "calls" keyword<br/>Filter by method_calls metadata
    WF->>WF: Filtered to 2 chunks

    WF->>WF: Grade Documents Node
    loop For each chunk
        WF->>LLM: Is chunk relevant?
        LLM-->>WF: yes/no
    end
    WF->>WF: 2/2 relevant

    WF->>WF: Generate Node
    WF->>LLM: Generate answer from chunks
    LLM-->>WF: Generated answer with references

    WF->>WF: Self-Reflection Node
    WF->>LLM: Validate answer quality
    LLM-->>WF: Quality: Good

    WF->>WF: Finalize Node
    WF->>UI: Return final answer
    UI->>User: Display answer with file references

    Note over User,FAISS: Total time: ~2-3 seconds
```

## 9. Error Handling and Retry Logic

```mermaid
graph TD
    Start([Query Start]) --> Process[Normal Processing]

    Process --> Error1{Error Type}

    Error1 -->|No Relevant<br/>Documents| Check1{Retry Count<br/>< 2?}
    Error1 -->|Poor Answer<br/>Quality| Check2{Retry Count<br/>< 2?}
    Error1 -->|Success| Success[Return Answer]

    Check1 -->|Yes| Retry1[Increment Retry<br/>Rewrite Query<br/>Try Again]
    Check1 -->|No| Fail1[Finalize:<br/>No relevant code found]

    Check2 -->|Yes| Retry2[Increment Retry<br/>Rewrite Query<br/>Try Again]
    Check2 -->|No| Fail2[Finalize:<br/>Best available answer]

    Retry1 --> Process
    Retry2 --> Process

    Fail1 --> End([Honest Response:<br/>Code not found])
    Fail2 --> End
    Success --> End

    style Start fill:#e1f5fe
    style Success fill:#c8e6c9
    style Fail1 fill:#ffcdd2
    style Fail2 fill:#fff9c4
    style End fill:#c8e6c9
```

## 10. Component Interaction Diagram

```mermaid
graph LR
    subgraph "Frontend"
        A[User Input]
        B[Display Output]
    end

    subgraph "Core Engine"
        C[Query Processor]
        D[Workflow Orchestrator<br/>LangGraph]
        E[State Manager]
    end

    subgraph "Retrieval"
        F[Vector Search<br/>FAISS]
        G[Metadata Filter]
        H[Result Ranker]
    end

    subgraph "AI Services"
        I[Quality Checker]
        J[Query Rewriter]
        K[Document Grader]
        L[Answer Generator]
        M[Quality Validator]
    end

    subgraph "Data Sources"
        N[(Codebase<br/>Java Files)]
        O[(Vector Store<br/>Embeddings)]
        P[(Metadata<br/>Relationships)]
    end

    A --> C
    C --> D
    D --> E

    D --> I
    D --> J
    D --> F
    D --> K
    D --> L
    D --> M

    F --> O
    G --> P
    F --> G
    G --> H
    H --> K

    I -.LLM.-> OpenAI
    J -.LLM.-> OpenAI
    K -.LLM.-> OpenAI
    L -.LLM.-> OpenAI
    M -.LLM.-> OpenAI

    N -.Indexing.-> O
    N -.Parsing.-> P

    M --> B

    style A fill:#e1f5fe
    style B fill:#c8e6c9
    style D fill:#f3e5f5
    style OpenAI fill:#fff9c4
```

## How to View These Diagrams

### Option 1: GitHub (Recommended)
- Push this file to GitHub
- Diagrams render automatically with Mermaid support

### Option 2: VS Code
- Install "Markdown Preview Mermaid Support" extension
- Open this file and preview (Ctrl+Shift+V)

### Option 3: Online Viewer
- Visit: https://mermaid.live/
- Copy diagram code and paste
- Export as PNG/SVG

### Option 4: Convert to Images
```bash
# Install mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# Convert to PNG
mmdc -i FLOW_DIAGRAM_PROFESSIONAL.md -o diagrams/
```

## Diagram Legend

| Color | Meaning |
|-------|---------|
| 🔵 Blue (`#e1f5fe`) | Input/Start points |
| 🟢 Green (`#c8e6c9`) | Success/Output points |
| 🟡 Yellow (`#fff9c4`) | Decision points |
| 🟣 Purple (`#f3e5f5`) | Processing/LLM nodes |
| 🔴 Red (`#ffcdd2`) | Error/Failure states |
| 🟠 Orange (`#ffe0b2`) | Intermediate processing |

---

**Created for**: Intelligent Code Insights Technical Documentation
**Version**: 1.0
**Date**: January 2025
