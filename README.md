# Code Intelligence RAG System

A modular Retrieval-Augmented Generation (RAG) system for intelligent Java codebase analysis using Streamlit, LangGraph, and OpenAI.

## 🏗️ Architecture

This application follows a clean, modular architecture with clear separation of concerns:

```
src/
├── config/           # Configuration and settings
│   ├── __init__.py
│   └── settings.py   # Centralized configuration
├── models/           # Data models and state schemas
│   ├── __init__.py
│   └── state.py      # GraphState definition
├── services/         # Business logic services
│   ├── __init__.py
│   ├── document_loader.py   # Document loading and chunking
│   ├── vectorstore.py       # Vector store management
│   └── llm.py              # LLM interactions
├── utils/            # Utility functions
│   ├── __init__.py
│   ├── java_parser.py       # Java code metadata extraction
│   └── relationship_filter.py  # Relationship-based filtering
├── workflow/         # LangGraph workflow components
│   ├── __init__.py
│   ├── nodes.py      # Workflow nodes
│   ├── routing.py    # Conditional routing logic
│   └── builder.py    # Workflow graph builder
├── ui/              # Streamlit UI components
│   ├── __init__.py
│   ├── styles.py     # CSS styles
│   └── components.py # Reusable UI components
├── core/            # Core initialization logic
│   ├── __init__.py
│   └── initializer.py
└── app.py           # Main application entry point
```

## ✨ Features

- **Semantic Code Search**: Vector-based similarity search using FAISS
- **Relationship-Aware Filtering**: Hybrid Lite approach combining vector search with code relationships
- **Adaptive Query Processing**: Quality checks and automatic query rewriting
- **Self-Reflection**: Answer quality validation
- **Interactive UI**: Clean Streamlit interface with file references and code snippets

## 🚀 Getting Started

### Prerequisites

- Python 3.9+
- OpenAI API key

### Installation

1. Clone the repository:
```bash
cd IntelligentCodeInsights
```

2. Create a virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. Install dependencies:
```bash
pip install -r requirements.txt
```

4. Create a `.env` file:
```bash
OPENAI_API_KEY=your_api_key_here
```

### Running the Application

```bash
streamlit run src/app.py
```

Or from the root directory:
```bash
cd src
python -m streamlit run app.py
```

## 📖 Usage

1. **Configure Path**: Enter your Java project path in the sidebar
2. **Load Codebase**: Click "Load/Reload Codebase" to index your code
3. **Ask Questions**: Type natural language questions about your codebase
   - "How does authentication work?"
   - "What calls the User class?"
   - "Explain the payment processing flow"

## 🔧 Configuration

All configuration settings are centralized in [`src/config/settings.py`](src/config/settings.py):

- OpenAI model and embedding settings
- Vector store parameters
- Chunk size and overlap
- Retry limits
- UI preferences

## 🧩 Module Details

### Services
- **DocumentLoaderService**: Loads and splits Java code files
- **VectorStoreService**: Manages FAISS vector store and retrieval
- **LLMService**: Handles LLM interactions

### Workflow
- **Query Quality Node**: Validates and potentially rewrites queries
- **Retrieve Node**: Fetches relevant code chunks
- **Grade Documents Node**: Scores relevance of retrieved code
- **Generate Node**: Creates answers from relevant code
- **Self-Reflection Node**: Validates answer quality

### Utils
- **Java Parser**: Extracts imports, classes, methods, and calls
- **Relationship Filter**: Filters documents by code relationships

## 📊 Technology Stack

- **Frontend**: Streamlit
- **Workflow**: LangGraph
- **Vector Store**: FAISS
- **Embeddings**: OpenAI text-embedding-3-large
- **LLM**: OpenAI GPT-4o
- **Code Processing**: LangChain

## 🏅 Best Practices Implemented

- ✅ Modular architecture with separation of concerns
- ✅ Type hints throughout the codebase
- ✅ Docstrings for all functions and classes
- ✅ Centralized configuration
- ✅ Clear naming conventions
- ✅ Reusable components
- ✅ Error handling
- ✅ Caching for performance

## 📝 License

This project is provided as-is for educational and commercial use.

## 🤝 Contributing

Contributions are welcome! Please ensure code follows the established architectural patterns and includes appropriate documentation.

## 📧 Support

For issues and questions, please refer to the architecture documentation in `ARCHITECTURE_DIAGRAM.md`.
