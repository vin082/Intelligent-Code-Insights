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
