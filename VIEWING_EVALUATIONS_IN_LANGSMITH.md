# Viewing Evaluations in LangSmith Dashboard

## Overview

This guide explains how to view and analyze evaluation metrics in the LangSmith dashboard. After running queries, your custom evaluators automatically send feedback scores to LangSmith.

## How Evaluations Appear in LangSmith

### What Gets Sent

Every query generates **4 feedback scores** that are attached to the trace:

1. **accuracy** - Answer correctness (0.0 to 1.0)
2. **groundedness** - Hallucination detection (0.0 to 1.0)
3. **retrieval_relevancy** - Chunk relevance (0.0 to 1.0)
4. **context_precision** - Ranking quality (0.0 to 1.0)

Each score includes:
- **Score**: Numeric value (0.0-1.0)
- **Comment**: Reasoning/explanation
- **Key**: Metric identifier

## Step-by-Step: Viewing Evaluations

### 1. Access LangSmith Dashboard

```
https://smith.langchain.com
```

1. Log in to your account
2. Select project: `IntelligentCodeInsights`

### 2. View All Traces

You'll see a list of all traces (queries executed):

```
┌─────────────────────────────────────────────────────┐
│ Traces                                               │
├─────────────────────────────────────────────────────┤
│ 📊 What does OrderService do?          2min ago     │
│ 📊 AuthenticationService                5min ago     │
│ 📊 How does payment processing work?   10min ago    │
└─────────────────────────────────────────────────────┘
```

### 3. Click on a Trace

Click any trace to see detailed view:

```
┌─────────────────────────────────────────────────────┐
│ Trace: What does OrderService do?                   │
├─────────────────────────────────────────────────────┤
│ ⏱️ Duration: 8.2s                                   │
│ 💰 Cost: $0.013                                      │
│ 📥 Input: What does OrderService do?                │
│ 📤 Output: The OrderService class manages...        │
│                                                      │
│ 🎯 Feedback & Evaluation                            │
│   ✅ accuracy: 0.95                                 │
│   ✅ groundedness: 1.0                              │
│   ✅ retrieval_relevancy: 1.0                       │
│   ✅ context_precision: 1.0                         │
└─────────────────────────────────────────────────────┘
```

### 4. View Feedback Details

Scroll to "Feedback & Evaluation" section:

#### Example 1: High-Quality Response
```
Feedback (4)
────────────

📊 accuracy
   Score: 0.95
   Comment: The answer accurately describes OrderService functionality
            with correct method names and class relationships.

📊 groundedness
   Score: 1.0
   Comment: Hallucinations: None

📊 retrieval_relevancy
   Score: 1.0
   Comment: 3/3 chunks relevant

📊 context_precision
   Score: 1.0
   Comment: Relevancy-based precision: 1.00
```

#### Example 2: Poor Response with Hallucinations
```
Feedback (4)
────────────

📊 accuracy
   Score: 0.45
   Comment: Answer contains incorrect method names and
            misrepresents class relationships.

📊 groundedness
   Score: 0.3
   Comment: Hallucinations: Mentioned OAuth integration that doesn't
            exist in code, claimed methods that aren't present

📊 retrieval_relevancy
   Score: 0.33
   Comment: 1/3 chunks relevant

📊 context_precision
   Score: 0.33
   Comment: Relevancy-based precision: 0.33
```

### 5. Filter by Evaluation Scores

Use LangSmith's filter to find traces by quality:

**Filter Examples:**

```
# Find all high-quality responses
feedback.accuracy >= 0.8

# Find responses with hallucinations
feedback.groundedness < 0.8

# Find poor retrieval
feedback.retrieval_relevancy < 0.6

# Find all failing responses
feedback.accuracy < 0.6 OR feedback.groundedness < 0.8

# Combination filters
feedback.accuracy >= 0.8 AND feedback.groundedness >= 0.8
```

**How to Apply Filters:**

1. Click "Add Filter" in dashboard
2. Select "Feedback"
3. Choose metric (accuracy, groundedness, etc.)
4. Set condition (>=, <, ==)
5. Enter value

### 6. Analyze Trends

**View Metrics Over Time:**

1. Go to "Analytics" tab
2. Select metric to track
3. View graph of scores over time

**Example Analytics View:**
```
Accuracy Trend (Last 30 Days)
────────────────────────────────────

1.0 ┤                              ╭─
0.9 ┤                         ╭────╯
0.8 ┤                    ╭────╯
0.7 ┤               ╭────╯
0.6 ┤          ╭────╯
    └──┬──┬──┬──┬──┬──┬──┬──┬──┬──
       5  10 15 20 25 30

Average: 0.87
Improving: ✅
```

## Understanding Feedback Scores

### Accuracy (0.0 to 1.0)

**What it measures**: How correct and complete the answer is

| Score | Interpretation | Example |
|-------|---------------|---------|
| 1.0 | Perfect answer | Accurate code examples, complete explanation |
| 0.8 | Good answer | Minor details missing but fundamentally correct |
| 0.6 | Fair answer | Partially correct, some inaccuracies |
| 0.4 | Poor answer | Significant errors |
| 0.2 | Very poor | Mostly wrong |
| 0.0 | Completely wrong | No useful information |

### Groundedness (0.0 to 1.0)

**What it measures**: Whether answer is supported by retrieved code

| Score | Interpretation | Hallucinations |
|-------|---------------|----------------|
| 1.0 | Fully grounded | None |
| 0.8 | Mostly grounded | Minor unsupported details |
| 0.6 | Partially grounded | Some invented claims |
| 0.4 | Poorly grounded | Many hallucinations |
| 0.2 | Mostly hallucinated | Most claims unsupported |
| 0.0 | Completely fabricated | All invented |

**Common Hallucinations Detected:**
- Methods that don't exist in retrieved code
- Incorrect class relationships (extends, implements)
- Features claimed but not present
- Wrong method signatures or parameters

### Retrieval Relevancy (0.0 to 1.0)

**What it measures**: Percentage of retrieved chunks that are relevant

| Score | Interpretation | Example |
|-------|---------------|---------|
| 1.0 | All relevant | 5/5 chunks helpful |
| 0.8 | Mostly relevant | 4/5 chunks helpful |
| 0.6 | Partially relevant | 3/5 chunks helpful |
| 0.4 | Poorly relevant | 2/5 chunks helpful |
| 0.2 | Barely relevant | 1/5 chunks helpful |
| 0.0 | Not relevant | 0/5 chunks helpful |

### Context Precision (0.0 to 1.0)

**What it measures**: Quality of retrieval ranking

| Score | Interpretation |
|-------|---------------|
| 1.0 | Excellent ranking |
| 0.8 | Good ranking |
| 0.6 | Fair ranking |
| 0.4 | Poor ranking |
| 0.2 | Very poor |
| 0.0 | Failed |

## Exporting Evaluation Data

### Method 1: Dashboard Export

1. Go to project: `IntelligentCodeInsights`
2. Click "Export" button
3. Select date range
4. Choose format (CSV, JSON)
5. Download includes all feedback scores

### Method 2: API Export

```python
from langsmith import Client

client = Client()

# Get runs with feedback
runs = client.list_runs(
    project_name="IntelligentCodeInsights",
    limit=100
)

for run in runs:
    feedback = list(client.list_feedback(run_ids=[run.id]))
    for fb in feedback:
        print(f"{fb.key}: {fb.score} - {fb.comment}")
```

### Method 3: Use Helper Function

```python
from utils.langsmith_helper import get_feedback_stats

# Get stats on last 100 runs
stats = get_feedback_stats(limit=100)

print(f"Total runs: {stats['total_runs']}")
print(f"Runs with feedback: {stats['runs_with_feedback']}")
print(f"Avg accuracy: {stats.get('avg_accuracy', 'N/A')}")
print(f"Avg groundedness: {stats.get('avg_groundedness', 'N/A')}")
print(f"Avg relevancy: {stats.get('avg_relevancy', 'N/A')}")
print(f"Avg precision: {stats.get('avg_precision', 'N/A')}")
```

## Troubleshooting

### "I don't see any feedback scores"

**Possible causes:**

1. **LangSmith not enabled**
   ```bash
   # Check .env
   LANGCHAIN_TRACING_V2=true
   ```

2. **Evaluations not running**
   - Check intermediate steps in UI for "📊 Evaluation: Sent to LangSmith"
   - If you see "⚠️ Evaluation skipped", check error message

3. **Wrong project**
   - Verify you're viewing project: `IntelligentCodeInsights`
   - Check `.env`: `LANGCHAIN_PROJECT=IntelligentCodeInsights`

4. **Feedback not synced yet**
   - Wait 5-10 seconds after query completes
   - Refresh LangSmith dashboard

5. **Run context issue**
   - Feedback requires active trace context
   - Ensure workflow runs through LangGraph properly

### "Feedback shows but scores are all 0.0"

**Cause**: Evaluators failing to execute

**Fix:**
```bash
# Test evaluators locally
cd src
python -c "from evaluations.evaluators import evaluate_accuracy; print('OK')"

# Check OpenAI API key
echo $OPENAI_API_KEY
```

### "Some metrics missing"

**Cause**: Individual evaluator failures

**Fix:**
- Check intermediate steps for specific errors
- Relevancy evaluator needs retrieved documents
- Some metrics skip if no context available

## Best Practices

### 1. Monitor Regularly

- Check dashboard weekly for trends
- Set up alerts for quality degradation
- Review traces with low scores

### 2. Filter Smart

```
# Weekly quality check filters
feedback.accuracy < 0.7
feedback.groundedness < 0.8
feedback.retrieval_relevancy < 0.5
```

### 3. Investigate Outliers

When you see unusual scores:
1. Click the trace
2. Read the full input/output
3. Check retrieved code snippets
4. Review feedback comments
5. Identify patterns

### 4. Track Improvements

After making changes:
1. Note the date
2. Filter traces after that date
3. Compare metrics before/after
4. Document improvements

## Creating Custom Views

### View 1: Quality Dashboard

Filter: `feedback.accuracy >= 0.8 AND feedback.groundedness >= 0.9`

Shows: Only high-quality responses

Use for: Sharing success examples

### View 2: Problem Cases

Filter: `feedback.accuracy < 0.6 OR feedback.groundedness < 0.7`

Shows: Responses needing improvement

Use for: Debugging and optimization

### View 3: Retrieval Issues

Filter: `feedback.retrieval_relevancy < 0.6`

Shows: Cases where retrieval is poor

Use for: Improving vector search/embeddings

## Summary

**To view evaluations:**
1. Go to https://smith.langchain.com
2. Select project `IntelligentCodeInsights`
3. Click any trace
4. Scroll to "Feedback & Evaluation"
5. See 4 scores with comments

**Key metrics:**
- accuracy (correctness)
- groundedness (hallucinations)
- retrieval_relevancy (% relevant chunks)
- context_precision (ranking quality)

**All feedback is automatic** - no manual work required. Every query gets evaluated and results appear in LangSmith within seconds.

---

**Need help?**
- LangSmith Docs: https://docs.smith.langchain.com/evaluation
- Feedback API: https://docs.smith.langchain.com/tracing/faq/logging_feedback
- Support: support@langchain.dev
