# LangSmith Setup Guide - Phase 1

## Overview

LangSmith provides observability and evaluation capabilities for your RAG system. This guide covers **Phase 1: Setup & Configuration**.

## Prerequisites

- ✅ Python environment with project dependencies installed
- ✅ OpenAI API key configured
- ✅ Internet connection

## Step 1: Sign Up for LangSmith

1. **Visit**: https://smith.langchain.com
2. **Sign up** using your GitHub or email account
3. **Verify** your email address

## Step 2: Get Your API Key

1. Navigate to **Settings**: https://smith.langchain.com/settings
2. Click on **API Keys** tab
3. Click **Create API Key**
4. **Copy** the generated key (starts with `lsv2_pt_`)
5. **⚠️ Important**: Store this key securely - you won't see it again!

## Step 3: Configure Environment Variables

Edit your `.env` file and add the following:

```bash
# LangSmith Configuration
LANGCHAIN_TRACING_V2=true
LANGCHAIN_API_KEY=lsv2_pt_your-actual-api-key-here
LANGCHAIN_PROJECT=IntelligentCodeInsights
LANGCHAIN_ENDPOINT=https://api.smith.langchain.com
ENVIRONMENT=development
```

**Example** `.env` file:
```bash
# OpenAI
OPENAI_API_KEY=sk-...your-openai-key...

# LangSmith (Observability)
LANGCHAIN_TRACING_V2=true
LANGCHAIN_API_KEY=lsv2_pt_...your-langsmith-key...
LANGCHAIN_PROJECT=IntelligentCodeInsights
LANGCHAIN_ENDPOINT=https://api.smith.langchain.com
ENVIRONMENT=development
```

## Step 4: Verify Installation

Run the test script:

```bash
python test_langsmith_connection.py
```

**Expected Output:**
```
============================================================
LangSmith Connection Test
============================================================

1. Configuration Check:
   - LANGCHAIN_TRACING_V2: true
   - LANGSMITH_ENABLED: True
   - LANGSMITH_PROJECT: IntelligentCodeInsights
   - API Key present: True

2. Testing API Connection...
   Connecting to LangSmith...
   ✅ Successfully connected to LangSmith!
   Endpoint: https://api.smith.langchain.com
   Project: IntelligentCodeInsights

3. Testing Trace Creation...
   Sending test query to OpenAI...
   ✅ Response: LangSmith test successful!

   🎉 Trace should be visible in LangSmith dashboard:
   https://smith.langchain.com/o/default/projects/p/IntelligentCodeInsights

============================================================
✅ ALL TESTS PASSED
============================================================
```

## Step 5: Run the Application

Start your Streamlit app:

```bash
streamlit run src/app.py
```

**Check LangSmith Status in UI:**
1. Open the app in your browser (usually `http://localhost:8501`)
2. Look at the **left sidebar**
3. Scroll to the **"📊 Observability"** section
4. You should see:
   ```
   ✅ LangSmith Observability: ENABLED
   - Project: IntelligentCodeInsights
   - Environment: development
   - Dashboard: View Traces
   ```

## Step 6: Make Test Queries

Execute some queries to generate traces:

1. **Semantic Query**: "How does payment processing work?"
2. **Structural Query**: "What calls OrderService?"
3. **Simple Query**: "User class"

## Step 7: View Traces in LangSmith Dashboard

1. **Open Dashboard**: https://smith.langchain.com
2. **Select Project**: `IntelligentCodeInsights`
3. **View Runs**: You'll see all your queries listed

### What You'll See:

Each trace shows:
- **Input**: The user's question
- **LLM Calls**: All GPT-4o invocations (quality check, grading, generation, reflection)
- **Retrievals**: FAISS vector searches
- **Output**: Final answer
- **Metadata**: Tokens used, latency, costs
- **Timeline**: Visual representation of workflow

### Example Trace Structure:

```
RunnableSequence (Main Workflow)
├── Query Quality Check (LLM Call)
│   ├── Input: "What calls OrderService?"
│   └── Output: "Good quality"
├── Retrieve Node
│   ├── Vector Search (FAISS)
│   └── Retrieved: 4 chunks
├── Grade Documents (4 LLM Calls)
│   ├── Chunk 1: "yes"
│   ├── Chunk 2: "yes"
│   ├── Chunk 3: "no"
│   └── Chunk 4: "yes"
├── Generate Answer (LLM Call)
│   └── Output: "OrderService is called by..."
└── Self-Reflection (LLM Call)
    └── Output: "Quality: Good"
```

## What's Enabled in Phase 1

✅ **Automatic Tracing** of:
- All LangChain/LangGraph operations
- LLM calls (GPT-4o)
- Retriever operations (FAISS)
- Chain executions

✅ **Metadata Captured**:
- Timestamps
- Latency per step
- Token usage
- Costs
- Errors/exceptions

✅ **Dashboard Access**:
- View all traces
- Search and filter
- Inspect individual runs
- Monitor performance

## What's NOT Yet Enabled (Future Phases)

⏳ **Custom Evaluators** (Phase 2-3)
- Accuracy measurement
- Groundedness checks
- Relevancy scoring

⏳ **Automated Evaluation** (Phase 3)
- Batch evaluation against test dataset
- Regression detection
- A/B testing

⏳ **Advanced Monitoring** (Phase 4)
- Custom dashboards
- Alerts
- Quality metrics over time

## Troubleshooting

### Issue 1: "Connection Failed"

**Symptoms**: Test script fails at connection test

**Solutions**:
1. Verify API key is correct (starts with `lsv2_pt_`)
2. Check internet connection
3. Ensure no firewall blocking `api.smith.langchain.com`
4. Try regenerating API key

### Issue 2: "LangSmith is not enabled"

**Symptoms**: UI shows "LangSmith: Disabled"

**Solutions**:
1. Check `.env` file has `LANGCHAIN_TRACING_V2=true`
2. Restart Streamlit app after changing `.env`
3. Verify dotenv is loading: add debug print in settings.py

### Issue 3: "No traces appearing"

**Symptoms**: Queries work but no traces in dashboard

**Solutions**:
1. Verify environment variables are loaded
2. Check project name matches: `IntelligentCodeInsights`
3. Look in "All Projects" if not in specific project
4. Wait 5-10 seconds for traces to sync

### Issue 4: "Missing API key"

**Symptoms**: Warning in UI about missing API key

**Solutions**:
1. Ensure `.env` has `LANGCHAIN_API_KEY=...`
2. Restart application
3. Check `.env` is in project root
4. Verify no typos in variable names

## Cost Implications

### LangSmith Pricing (as of 2024)

- **Free Tier**: 5,000 traces/month
- **Pay-as-you-go**: $0.01 per 1,000 traces after free tier
- **No change to OpenAI costs**: Observability doesn't add LLM calls

### Example Monthly Costs

| Usage | Traces | LangSmith Cost | OpenAI Cost |
|-------|--------|----------------|-------------|
| Development (100 queries/day) | ~3,000 | Free | ~$40 |
| Production (500 queries/day) | ~15,000 | $0.10 | ~$200 |
| High Volume (2000 queries/day) | ~60,000 | $0.55 | ~$800 |

**Note**: One query = 1 trace (even if it has 5-6 LLM calls internally)

## Next Steps

Once Phase 1 is complete:

1. ✅ **Phase 1 Complete**: Basic tracing working
2. ⏭️ **Phase 2**: Add custom evaluators (accuracy, groundedness, relevancy)
3. ⏭️ **Phase 3**: Create evaluation dataset and run batch evaluations
4. ⏭️ **Phase 4**: Set up monitoring dashboards and alerts

## Resources

- **LangSmith Docs**: https://docs.smith.langchain.com
- **Evaluation Guide**: https://docs.smith.langchain.com/evaluation
- **Dashboard**: https://smith.langchain.com
- **Support**: support@langchain.dev

---

## Quick Reference Card

```bash
# Start application
streamlit run src/app.py

# Test connection
python test_langsmith_connection.py

# View traces
https://smith.langchain.com/o/default/projects/p/IntelligentCodeInsights

# Disable tracing (for development)
LANGCHAIN_TRACING_V2=false

# Switch projects
LANGCHAIN_PROJECT=IntelligentCodeInsights-Dev
```

---

**Phase 1 Checklist**:
- [ ] LangSmith account created
- [ ] API key generated
- [ ] `.env` file updated
- [ ] Test script passes
- [ ] UI shows "LangSmith: ENABLED"
- [ ] Test queries executed
- [ ] Traces visible in dashboard

**Ready for Phase 2?** ✅

---

# Phase 2: Custom Evaluators ✅ COMPLETE

Phase 2 adds custom evaluation metrics that run automatically with each query to measure:
1. **Accuracy** - Is the answer correct?
2. **Groundedness** - Are there hallucinations?
3. **Retrieval Relevancy** - Are retrieved chunks relevant?
4. **Context Precision** - Are relevant chunks ranked well?

## What Was Implemented

### 1. Custom Evaluators (`src/evaluations/evaluators.py`)

Four evaluator classes using LLM-as-judge pattern:

**AccuracyEvaluator**
- Scores answer accuracy (0.0 to 1.0)
- Checks correctness, completeness, relevance
- Pass threshold: 0.6

**GroundednessEvaluator**
- Detects hallucinations
- Identifies unsupported claims
- Pass threshold: 0.8 (stricter)

**RetrievalRelevancyEvaluator**
- Evaluates each retrieved chunk
- Calculates % of relevant chunks
- Pass threshold: 60%

**ContextPrecisionEvaluator**
- Measures ranking quality
- Based on relevancy score
- Pass threshold: 0.6

### 2. Workflow Integration

**New Evaluation Node** (`src/workflow/nodes.py:246`)
- Runs after self-reflection
- Only executes if `LANGSMITH_ENABLED=true`
- Non-blocking (failures don't stop workflow)
- Results stored in state

**Updated Flow**:
```
Self-Reflection → Evaluation → Finalize
```

### 3. UI Display (`src/evaluations/display.py`)

**Metrics Dashboard**:
- 4-column metric cards with pass/fail indicators
- Expandable details with reasoning
- Overall pass/fail status

**Display Location**:
- Below code snippets in each answer
- Persisted in message history
- Only shown when LangSmith enabled

### 4. State Management

**Updated `GraphState`** (`src/models/state.py:30`):
```python
evaluation_results: Dict  # Stores all evaluation results
```

## How Evaluations Work

### Evaluation Flow

1. **Query executed** → Answer generated
2. **Self-reflection passes** → Route to evaluation node
3. **Evaluation node runs**:
   - Builds context from retrieved documents
   - Calls `run_all_evaluations()`
   - Stores results in state
4. **UI displays** → Metrics cards + expandable details
5. **LangSmith records** → All evaluations traced automatically

### Evaluation Criteria

**Accuracy (LLM-as-Judge)**:
```
- 1.0: Accurate, complete, with code examples
- 0.8: Accurate but missing minor details
- 0.6: Partially accurate (PASS threshold)
- 0.4: Contains inaccuracies
- 0.0: Completely wrong
```

**Groundedness (Hallucination Detection)**:
```
- 1.0: Every claim supported by context
- 0.8: Minor unsupported details (PASS threshold)
- 0.6: Some unsupported claims
- 0.4: Many hallucinations
- 0.0: Completely fabricated
```

**Relevancy (Chunk-level)**:
```
For each chunk:
- RELEVANT: Yes/No decision
Calculate: relevant_count / total_count
- 60%+ = PASS
```

**Precision (Ranking Quality)**:
```
Based on relevancy score:
- High relevancy → Good precision
- 0.6+ = PASS
```

## Testing Evaluations

### Test with Sample Queries

```bash
# Enable LangSmith first
LANGCHAIN_TRACING_V2=true

# Start app
streamlit run src/app.py

# Try these queries:
1. "AuthenticationService" - Should score high on all metrics
2. "How do I implement OAuth?" - May fail groundedness (not in code)
3. "OrderService" - Should pass all if code exists
```

### Viewing Results

**In Streamlit UI**:
- Metrics appear below each answer
- 4 cards: Accuracy, Groundedness, Relevancy, Precision
- Expand for detailed reasoning

**In LangSmith Dashboard**:
1. Go to https://smith.langchain.com
2. Select project: `IntelligentCodeInsights`
3. Click on any trace
4. Evaluation scores appear in trace details
5. Filter by score: `evaluation.accuracy >= 0.8`

## Configuration

### Enable/Disable Evaluations

Evaluations automatically enabled/disabled with LangSmith:

```bash
# In .env file

# Enable evaluations (runs on every query)
LANGCHAIN_TRACING_V2=true

# Disable evaluations (skip evaluation node)
LANGCHAIN_TRACING_V2=false
```

### Adjust Thresholds

Edit `src/evaluations/evaluators.py`:

```python
# Line 99 - Accuracy threshold
'pass': score >= 0.6  # Change to 0.7 for stricter

# Line 193 - Groundedness threshold
'pass': score >= 0.8  # Change to 0.9 for stricter

# Line 283 - Relevancy threshold
'pass': percentage >= 60.0  # Change to 70.0 for stricter

# Line 330 - Precision threshold
'pass': precision_score >= 0.6  # Change to 0.7
```

## Performance Impact

### Latency Added

| Metric | Time Added | LLM Calls |
|--------|------------|-----------|
| Accuracy | ~1.5s | 1 call |
| Groundedness | ~1.5s | 1 call |
| Relevancy | ~0.5s per chunk | N chunks |
| Precision | ~0.1s | 0 calls (calculated) |
| **Total** | **~3-5s** | **2 + N calls** |

**Example**: Query with 3 retrieved chunks:
- Base query: ~4s
- With evaluations: ~8s (4s + 3-5s)
- Cost: +$0.002 per query

### Cost Impact

**OpenAI API costs**:
```
Base query: ~$0.008
+ Accuracy eval: ~$0.001
+ Groundedness eval: ~$0.001
+ Relevancy (3 chunks): ~$0.003
Total: ~$0.013 per query (60% increase)
```

**Monthly costs** (500 queries/day):
```
Without evaluations: $120/month
With evaluations: $195/month (+$75)
```

### Optimization Options

1. **Sample evaluations** (evaluate 20% of queries)
2. **Skip relevancy** (most expensive, 3+ LLM calls)
3. **Batch evaluate** (run offline on sample set)
4. **Cache results** (for identical queries)

## Files Modified/Created

### Created:
- `src/evaluations/__init__.py` - Module exports
- `src/evaluations/evaluators.py` - 4 evaluator classes (540 lines)
- `src/evaluations/display.py` - UI components (180 lines)

### Modified:
- `src/models/state.py:30` - Added `evaluation_results` field
- `src/workflow/nodes.py:246` - Added `evaluation_node()`
- `src/workflow/builder.py:36-61` - Integrated evaluation node
- `src/workflow/routing.py:26-33` - Route to evaluation
- `src/app.py:9-158` - Display evaluation results
- `src/ui/components.py:77-116` - Render evaluations in history

## Troubleshooting

### "Evaluation skipped"

**Cause**: LangSmith not enabled or import error

**Fix**:
```bash
# Check .env
LANGCHAIN_TRACING_V2=true

# Test imports
python -c "from evaluations.evaluators import run_all_evaluations; print('OK')"
```

### "OpenAI API rate limit"

**Cause**: Too many evaluation calls

**Fix**:
- Reduce query frequency
- Upgrade OpenAI tier
- Sample evaluations (Phase 3)

### "Evaluation taking too long"

**Cause**: Many retrieved chunks (relevancy is expensive)

**Fix**:
- Limit chunks to top 3-5
- Skip relevancy for development
- Use smaller model (gpt-4o-mini)

## Next Steps

1. ✅ **Phase 1 Complete**: Basic tracing working
2. ✅ **Phase 2 Complete**: Custom evaluators running
3. ⏭️ **Phase 3**: Create evaluation dataset (test cases + ground truth)
4. ⏭️ **Phase 4**: Set up monitoring dashboards and alerts

**Ready for Phase 3?** See Phase 3 setup below.

---

**Phase 2 Checklist**:
- [x] Evaluators implemented (4 types)
- [x] Workflow integration complete
- [x] UI displays metrics
- [x] State management updated
- [x] Documentation updated
- [x] Performance analyzed
- [ ] Tested with sample queries
- [ ] Verified in LangSmith dashboard
- [ ] Thresholds tuned (optional)

**Ready for Phase 3?** ✅
