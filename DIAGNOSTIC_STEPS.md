# Diagnostic Steps: Why Evaluations Not Appearing in LangSmith

## Quick Test

1. **Run a query in your Streamlit app**
   ```bash
   streamlit run src/app.py
   ```

2. **Enable "View Process Steps"** in the sidebar

3. **Ask any question**, e.g., "OrderService"

4. **Expand "View Process Steps"** and look for these messages:

### What You Should See:

#### Success Case ✅
```
📊 Evaluation:
✓ Accuracy: 0.95 (Pass)
✓ Groundedness: 1.0 (Pass)
✓ Relevancy: 100% (Pass)
✓ Precision: 1.0 (Pass)

🔍 Found run ID via run_tree: abc12345...
✅ Sent 4 evaluations to LangSmith
```

#### Partial Success ⚠️
```
⚠️ Method 1 failed: No run context
🔍 Using most recent run ID: xyz78901...
✅ Sent 4 evaluations to LangSmith
```

#### Failure Case ❌
```
⚠️ Method 1 failed: No run context
⚠️ Method 2 failed: Invalid API key
⚠️ Could not find run ID - feedback not sent
```

## Common Issues & Fixes

### Issue 1: "Could not find run ID"

**Cause**: LangSmith tracing not properly enabled

**Fix**:
```bash
# Check your .env file
LANGCHAIN_TRACING_V2=true
LANGCHAIN_API_KEY=lsv2_pt_your-key-here
LANGCHAIN_PROJECT=IntelligentCodeInsights

# Restart Streamlit
# Press Ctrl+C
streamlit run src/app.py
```

### Issue 2: "Method 1 failed: No run context"

**Cause**: This is NORMAL. LangGraph doesn't expose run context to nested functions.

**Expected**: Method 2 should succeed using recent run ID.

**Check**: Do you see "Using most recent run ID" after this message?
- ✅ Yes → This is working correctly
- ❌ No → Continue to Issue 3

### Issue 3: "Method 2 failed: Invalid project name"

**Cause**: Project name mismatch

**Fix**:
```bash
# In .env
LANGCHAIN_PROJECT=IntelligentCodeInsights

# Must match exactly (case-sensitive)
```

### Issue 4: Feedback sent but not visible in dashboard

**Cause**: Timing issue - feedback attached to wrong run

**Fix**: Use the manual feedback script below

## Manual Feedback Test

Create a test script to verify LangSmith API works:

```python
# test_feedback.py
from langsmith import Client
import os

# Set up
client = Client()
project_name = "IntelligentCodeInsights"

print("🔍 Fetching recent runs...")

# Get most recent run
runs = list(client.list_runs(project_name=project_name, limit=1))

if not runs:
    print("❌ No runs found. Run a query in the app first.")
    exit(1)

run = runs[0]
print(f"✅ Found run: {run.id}")
print(f"   Input: {run.inputs.get('question', 'N/A')}")
print(f"   Output: {run.outputs.get('final_answer', 'N/A')[:100]}...")

# Send test feedback
print("\n📤 Sending test feedback...")

try:
    client.create_feedback(
        run_id=run.id,
        key="test_accuracy",
        score=0.95,
        comment="Test feedback from diagnostic script"
    )
    print("✅ Feedback sent successfully!")
    print(f"\n🔗 View in dashboard:")
    print(f"https://smith.langchain.com/o/default/projects/p/{project_name}/r/{run.id}")

except Exception as e:
    print(f"❌ Failed to send feedback: {e}")
```

**Run it:**
```bash
python test_feedback.py
```

**Expected output:**
```
🔍 Fetching recent runs...
✅ Found run: abc12345-6789-...
   Input: OrderService
   Output: The OrderService class manages order lifecycle...

📤 Sending test feedback...
✅ Feedback sent successfully!

🔗 View in dashboard:
https://smith.langchain.com/o/default/projects/p/IntelligentCodeInsights/r/abc12345...
```

**Then**:
1. Click the dashboard link
2. Scroll to bottom
3. Look for "Feedback & Evaluation" section
4. You should see: `test_accuracy: 0.95`

## Checking LangSmith Dashboard

### Step 1: Verify Traces Exist

1. Go to: https://smith.langchain.com
2. Click project: `IntelligentCodeInsights`
3. Do you see traces listed?
   - ✅ Yes → Good, tracing works
   - ❌ No → Fix tracing first (see Issue 1)

### Step 2: Check Individual Trace

1. Click on any recent trace
2. Scroll down to "Feedback & Evaluation" section
3. What do you see?

#### Option A: Section exists but empty
```
📊 Feedback & Evaluation
   (no feedback yet)
```
**Diagnosis**: Feedback not being sent
**Next**: Check intermediate steps in app

#### Option B: Section doesn't exist
```
[No "Feedback & Evaluation" section visible]
```
**Diagnosis**: This section only appears if feedback exists
**Next**: Send test feedback (see Manual Test above)

#### Option C: Feedback visible ✅
```
📊 Feedback & Evaluation
   accuracy: 0.95
   groundedness: 1.0
   ...
```
**Success!** Evaluations are working.

## Alternative: Check Via API

```python
from langsmith import Client

client = Client()
project = "IntelligentCodeInsights"

# Get recent run
runs = list(client.list_runs(project_name=project, limit=1))
if runs:
    run_id = runs[0].id

    # Check for feedback
    feedback_list = list(client.list_feedback(run_ids=[run_id]))

    print(f"Run ID: {run_id}")
    print(f"Feedback count: {len(feedback_list)}")

    for fb in feedback_list:
        print(f"  - {fb.key}: {fb.score}")
```

## Next Steps Based on Results

### If intermediate steps show "✅ Sent 4 evaluations"

1. Wait 10-15 seconds
2. Refresh LangSmith dashboard
3. Check feedback section again
4. If still not visible, try manual test script

### If intermediate steps show "⚠️ Could not find run ID"

1. Verify `.env` has correct settings
2. Restart Streamlit
3. Run query again
4. Check intermediate steps again

### If manual test script works but app doesn't

**Issue**: Run ID detection timing problem

**Solution**: The app may be attaching feedback to wrong run. Try this:

1. Run a query
2. Copy the trace URL from LangSmith
3. Extract run ID from URL
4. Manually attach feedback to that run ID

## Report Back

After testing, please share:

1. ✅ What you see in "View Process Steps" (copy exact messages)
2. ✅ What you see in LangSmith dashboard (screenshot if possible)
3. ✅ Result of manual test script
4. ✅ Your `.env` settings (LANGCHAIN_* variables only, hide API key)

This will help me identify the exact issue and provide a fix.

## Expected Behavior Summary

**When everything works:**
1. You run a query → "OrderService"
2. App shows evaluation metrics in UI ✅
3. Intermediate steps show "✅ Sent 4 evaluations to LangSmith" ✅
4. LangSmith dashboard shows 4 feedback scores ✅
5. You can filter by: `feedback.accuracy >= 0.8` ✅

**Current state:**
- ✅ Step 1-2 working (evaluation metrics in UI)
- ❓ Step 3 unknown (need to check intermediate steps)
- ❌ Step 4-5 not working (no feedback in dashboard)

Let's debug step 3 to find the issue!
