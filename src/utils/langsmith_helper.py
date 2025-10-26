"""
LangSmith integration helper utilities
"""

import os
from typing import Dict, Any


def get_langsmith_status() -> Dict[str, Any]:
    """
    Get LangSmith configuration status

    Returns:
        Dictionary with LangSmith status and configuration
    """
    from config.settings import (
        LANGSMITH_ENABLED,
        LANGSMITH_API_KEY,
        LANGSMITH_PROJECT,
        ENVIRONMENT
    )

    is_configured = bool(LANGSMITH_API_KEY and LANGSMITH_ENABLED)

    return {
        "enabled": LANGSMITH_ENABLED,
        "configured": is_configured,
        "project": LANGSMITH_PROJECT,
        "environment": ENVIRONMENT,
        "has_api_key": bool(LANGSMITH_API_KEY),
        "dashboard_url": f"https://smith.langchain.com/o/default/projects/p/{LANGSMITH_PROJECT}" if is_configured else None
    }


def display_langsmith_info() -> str:
    """
    Generate display text for LangSmith status

    Returns:
        Formatted status message
    """
    status = get_langsmith_status()

    if status["configured"]:
        return f"""
✅ **LangSmith Observability: ENABLED**
- Project: `{status['project']}`
- Environment: `{status['environment']}`
- Dashboard: [View Traces]({status['dashboard_url']})

All queries are being traced for evaluation and monitoring.
"""
    elif status["enabled"] and not status["has_api_key"]:
        return """
⚠️ **LangSmith: Enabled but not configured**
- Missing API key in .env file
- Add LANGCHAIN_API_KEY to enable tracing
"""
    else:
        return """
ℹ️ **LangSmith: Disabled**
- Set LANGCHAIN_TRACING_V2=true in .env to enable
- Sign up at https://smith.langchain.com
"""


def get_trace_url(run_id: str = None) -> str:
    """
    Generate LangSmith trace URL for a specific run

    Args:
        run_id: LangSmith run ID

    Returns:
        URL to view trace in LangSmith dashboard
    """
    from config.settings import LANGSMITH_PROJECT

    if run_id:
        return f"https://smith.langchain.com/o/default/projects/p/{LANGSMITH_PROJECT}/r/{run_id}"
    return f"https://smith.langchain.com/o/default/projects/p/{LANGSMITH_PROJECT}"
