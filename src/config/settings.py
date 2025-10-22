"""
Configuration settings for Code Intelligence RAG
"""

import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# OpenAI Configuration
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = "gpt-4o"
OPENAI_EMBEDDING_MODEL = "text-embedding-3-large"
TEMPERATURE = 0

# Application Settings
DEFAULT_JAVA_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "java_code")
APP_TITLE = "Code Intelligence RAG"
APP_ICON = "💻"

# Vector Store Settings
VECTOR_STORE_K = 4  # Number of chunks to retrieve
CHUNK_SIZE = 1500
CHUNK_OVERLAP = 300

# Code Splitting Separators
CODE_SEPARATORS = [
    "\nclass ",
    "\npublic class ",
    "\nprivate class ",
    "\nprotected class ",
    "\ninterface ",
    "\npublic interface ",
    "\npublic ",
    "\nprivate ",
    "\nprotected ",
    "\n\n",
    "\n",
    " "
]

# Workflow Settings
MAX_RETRY_COUNT = 2

# Relationship Keywords
RELATIONSHIP_KEYWORDS = ['calls', 'uses', 'depends', 'extends', 'implements', 'imports']

# UI Settings
LAYOUT = "wide"
INITIAL_SIDEBAR_STATE = "expanded"
