# 🤖 Adaptive RAG Assistant - Streamlit UI

A beautiful, user-friendly interface for the Adaptive RAG with Self-Reflection system.

## ✨ Features

### 🎨 **Modern UI/UX**
- **Chat-like interface** - Familiar messaging experience
- **Real-time progress** - See which steps are executing
- **Visual indicators** - Color-coded relevance scores
- **Path visualization** - See if query used knowledge base or web search
- **Session history** - Maintains conversation context

### 🔍 **Transparency Options**
- **Intermediate steps** - Toggle to view processing details
- **Document preview** - See retrieved documents with relevance scores
- **Quality indicators** - Self-reflection results displayed

### 🎯 **Smart Features**
- **Expandable sections** - Keep UI clean while offering details
- **Color-coded documents** - Green for relevant, red for irrelevant
- **Clear chat** - Reset conversation anytime
- **Error handling** - Graceful error messages

## 🚀 Quick Start

### 1. Install Dependencies
```bash
pip install -r streamlit_requirements.txt
```

### 2. Set Environment Variables
Create a `.env` file:
```env
OPENAI_API_KEY=your_openai_api_key_here
TAVILY_API_KEY=your_tavily_api_key_here
```

### 3. Run the App
```bash
streamlit run streamlit_app.py
```

The app will open in your browser at `http://localhost:8501`

## 📖 How to Use

### **Ask Questions About:**
- 🧠 **AI Agents** - Memory systems, planning, tool use
- 💡 **Prompt Engineering** - Chain of thought, few-shot, techniques
- 🛡️ **Adversarial Attacks** - LLM vulnerabilities and defenses

### **Example Questions:**
```
✅ "What is agent memory and how does it work?"
✅ "Explain chain of thought prompting with examples"
✅ "What are adversarial attacks on LLMs?"
✅ "How does ReAct prompting differ from standard prompting?"
```

### **UI Controls:**

#### Sidebar Settings:
- **Show intermediate steps** - View the reasoning process
- **Show retrieved documents** - See source documents with scores
- **Clear Chat History** - Start fresh conversation

#### Chat Interface:
- Type your question in the input box
- Click expandable sections to view details
- Scroll through conversation history

## 🎨 UI Elements Explained

### **Path Indicator**
Shows how your question was processed:
- `🧠 Knowledge Base → ✅ Documents Found` - Used vector store successfully
- `🧠 Knowledge Base → ❌ No Relevant Docs → 🌐 Web Search` - Fell back to web
- `🌐 Direct Web Search` - Question unrelated to knowledge base

### **Document Scores**
- 🟢 **Green box** - Relevant document (used in answer)
- 🔴 **Red box** - Irrelevant document (not used)

### **Intermediate Steps**
Track the process:
- `✅ Query Analysis` - Determined relevance to knowledge base
- `📚 Retrieved N documents` - Fetched from vector store
- `✅ M/N documents marked relevant` - Grading results
- `✏️ Query rewritten` - Improved search query
- `🌐 Web search found N results` - Fallback search
- `💬 Generated answer from...` - Answer source
- `🤔 Self-reflection` - Quality validation
- `✅ Answer finalized` - Process complete

## 🏗️ Architecture

```
User Question
    ↓
Query Analysis
    ├─→ Related to KB → Retrieve → Grade → Generate → Self-Reflect
    └─→ Not Related → Web Search → Generate
    ↓
Final Answer
```

## 🎯 Benefits Over Notebook

| Feature | Notebook | Streamlit UI |
|---------|----------|--------------|
| User Experience | Technical | Intuitive |
| Visualization | Code output | Beautiful UI |
| Interactivity | Run cells | Chat interface |
| History | None | Full session |
| Mobile-friendly | No | Yes |
| Shareable | Code only | Live app |
| Non-technical users | Difficult | Easy |

## 🔧 Customization

### **Change Theme**
Edit `.streamlit/config.toml`:
```toml
[theme]
primaryColor = "#1f77b4"
backgroundColor = "#ffffff"
secondaryBackgroundColor = "#f0f2f6"
```

### **Modify Knowledge Base**
Edit the URLs in `streamlit_app.py`:
```python
urls = [
    "your_url_1",
    "your_url_2",
    "your_url_3"
]
```

### **Adjust Model**
Change the LLM model:
```python
llm = ChatOpenAI(model="gpt-4", temperature=0)  # Use GPT-4
llm = ChatOpenAI(model="gpt-3.5-turbo", temperature=0)  # Faster/cheaper
```

## 📊 Performance Tips

1. **First Load** - Takes ~10-30s to load knowledge base (cached afterwards)
2. **Response Time** - Typically 3-10s depending on query complexity
3. **Caching** - Vector store cached across sessions for speed
4. **Concurrent Users** - Can handle multiple users with Streamlit Cloud

## 🚀 Deployment Options

### **Streamlit Cloud (Recommended)**
1. Push code to GitHub
2. Visit [streamlit.io/cloud](https://streamlit.io/cloud)
3. Connect repository
4. Add secrets (API keys)
5. Deploy!

### **Local Network**
```bash
streamlit run streamlit_app.py --server.address 0.0.0.0
```

### **Docker**
```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install -r streamlit_requirements.txt
CMD ["streamlit", "run", "streamlit_app.py"]
```

## 🐛 Troubleshooting

**Issue:** "Error initializing system"
- **Fix:** Check API keys in `.env` file

**Issue:** "Slow loading"
- **Fix:** First load is slow (building vector store), subsequent loads are fast

**Issue:** "ModuleNotFoundError"
- **Fix:** Run `pip install -r streamlit_requirements.txt`

**Issue:** "Empty responses"
- **Fix:** Ensure internet connection for web search fallback

## 📝 Notes

- **API Costs:** Uses OpenAI and Tavily APIs (pay per use)
- **Privacy:** Questions sent to OpenAI/Tavily - don't share sensitive data
- **Rate Limits:** Respect API rate limits (built-in retry logic)
- **Cache:** Vector store cached - delete `.streamlit/cache` to refresh

## 🎓 Learning Resources

- [Streamlit Docs](https://docs.streamlit.io)
- [LangGraph Guide](https://langchain-ai.github.io/langgraph/)
- [OpenAI API](https://platform.openai.com/docs)

## 🤝 Contributing

Suggestions for improvements:
1. Add conversation export (PDF/JSON)
2. Multi-language support
3. Voice input
4. Document upload feature
5. Customizable system prompts

---

**Enjoy your Adaptive RAG Assistant!** 🎉
