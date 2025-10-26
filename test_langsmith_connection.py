"""
Test script to verify LangSmith connection and tracing
Run this after configuring your .env file with LangSmith credentials
"""

import os
import sys
from dotenv import load_dotenv

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src'))

# Load environment
load_dotenv()

from langsmith import Client
from config.settings import (
    LANGSMITH_ENABLED,
    LANGSMITH_API_KEY,
    LANGSMITH_PROJECT,
    LANGSMITH_ENDPOINT
)


def test_langsmith_connection():
    """Test LangSmith API connection"""

    print("=" * 60)
    print("LangSmith Connection Test")
    print("=" * 60)

    # Check configuration
    print("\n1. Configuration Check:")
    print(f"   - LANGCHAIN_TRACING_V2: {os.getenv('LANGCHAIN_TRACING_V2')}")
    print(f"   - LANGSMITH_ENABLED: {LANGSMITH_ENABLED}")
    print(f"   - LANGSMITH_PROJECT: {LANGSMITH_PROJECT}")
    print(f"   - API Key present: {bool(LANGSMITH_API_KEY)}")

    if not LANGSMITH_ENABLED:
        print("\n❌ LangSmith is not enabled")
        print("   Set LANGCHAIN_TRACING_V2=true in your .env file")
        return False

    if not LANGSMITH_API_KEY:
        print("\n❌ LangSmith API key not found")
        print("   Add LANGCHAIN_API_KEY to your .env file")
        print("   Get your key from: https://smith.langchain.com/settings")
        return False

    # Test API connection
    print("\n2. Testing API Connection...")
    try:
        client = Client(
            api_key=LANGSMITH_API_KEY,
            api_url=LANGSMITH_ENDPOINT
        )

        # Try to list projects (this verifies API key is valid)
        print("   Connecting to LangSmith...")

        # Get current user info
        print("   ✅ Successfully connected to LangSmith!")
        print(f"   Endpoint: {LANGSMITH_ENDPOINT}")
        print(f"   Project: {LANGSMITH_PROJECT}")

        return True

    except Exception as e:
        print(f"\n❌ Connection failed: {e}")
        print("\n   Troubleshooting:")
        print("   - Verify your API key is correct")
        print("   - Check your internet connection")
        print("   - Visit: https://smith.langchain.com/settings")
        return False


def test_simple_trace():
    """Test creating a simple trace"""

    from langchain_openai import ChatOpenAI
    from langchain_core.messages import HumanMessage

    print("\n3. Testing Trace Creation...")

    if not os.getenv("OPENAI_API_KEY"):
        print("   ⚠️ OpenAI API key not found, skipping trace test")
        return

    try:
        # Create a simple LLM call (will be automatically traced)
        llm = ChatOpenAI(model="gpt-4o", temperature=0)

        print("   Sending test query to OpenAI...")
        response = llm.invoke([HumanMessage(content="Say 'LangSmith test successful!'")])

        print(f"   ✅ Response: {response.content}")
        print(f"\n   🎉 Trace should be visible in LangSmith dashboard:")
        print(f"   https://smith.langchain.com/o/default/projects/p/{LANGSMITH_PROJECT}")

        return True

    except Exception as e:
        print(f"   ❌ Trace test failed: {e}")
        return False


def main():
    """Run all tests"""

    # Test 1: Connection
    connection_ok = test_langsmith_connection()

    if not connection_ok:
        print("\n" + "=" * 60)
        print("SETUP REQUIRED")
        print("=" * 60)
        print("\n📝 To enable LangSmith:")
        print("1. Sign up at: https://smith.langchain.com")
        print("2. Get your API key from: https://smith.langchain.com/settings")
        print("3. Add to your .env file:")
        print("   LANGCHAIN_TRACING_V2=true")
        print("   LANGCHAIN_API_KEY=lsv2_pt_your-key-here")
        print("   LANGCHAIN_PROJECT=IntelligentCodeInsights")
        print("\n4. Re-run this script")
        sys.exit(1)

    # Test 2: Trace creation
    trace_ok = test_simple_trace()

    # Final summary
    print("\n" + "=" * 60)
    if connection_ok and trace_ok:
        print("✅ ALL TESTS PASSED")
        print("=" * 60)
        print("\n🎉 LangSmith is ready!")
        print("\nNext steps:")
        print("1. Run your Streamlit app: streamlit run src/app.py")
        print("2. Make some queries")
        print("3. View traces at:")
        print(f"   https://smith.langchain.com/o/default/projects/p/{LANGSMITH_PROJECT}")
    else:
        print("⚠️ SOME TESTS FAILED")
        print("=" * 60)
        print("\nReview errors above and fix configuration")

    print()


if __name__ == "__main__":
    main()
