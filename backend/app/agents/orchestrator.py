import json
from typing import List, Dict, Any
from app.ai.gemini_service import gemini_client
from app.schemas.domain import AgentStep

class AgentOrchestrator:
    """
    Autonomous Agent reasoning engine that breaks down complex user goals
    into actionable multi-step workflows with tool execution planning.
    """
    async def create_plan(self, goal: str, context: str = "") -> List[AgentStep]:
        prompt = f"""
You are the Autonomous Agent Engine for AI Personal OS.
The user wants to accomplish the following goal:
"{goal}"

Additional Context:
{context}

Break down this goal into a strict list of sequential, concrete steps (3 to 6 steps).
For each step, specify:
1. "action": clear description of what needs to be done.
2. "tool": one of ["SEARCH_MEMORY", "CREATE_TASK", "CALENDAR_SCHEDULE", "SUMMARIZE_DOC", "EXECUTE_TOOL", "NOTIFY_USER"].

Respond ONLY with a valid JSON array of objects:
[
  {{"step_number": 1, "action": "...", "tool": "SEARCH_MEMORY"}},
  {{"step_number": 2, "action": "...", "tool": "CREATE_TASK"}}
]
"""
        response_text = await gemini_client.generate_response(prompt)
        try:
            # Clean possible markdown formatting
            clean_json = response_text.strip()
            if clean_json.startswith("```json"):
                clean_json = clean_json[7:]
            if clean_json.startswith("```"):
                clean_json = clean_json[3:]
            if clean_json.endswith("```"):
                clean_json = clean_json[:-3]
            
            data = json.loads(clean_json.strip())
            steps = []
            for item in data:
                steps.append(AgentStep(
                    step_number=item.get("step_number", len(steps) + 1),
                    action=item.get("action", ""),
                    tool=item.get("tool", "EXECUTE_TOOL"),
                    status="PENDING"
                ))
            return steps
        except Exception:
            # Fallback default plan if JSON parsing failed
            return [
                AgentStep(step_number=1, action=f"Analyze requirements for: {goal}", tool="SEARCH_MEMORY", status="PENDING"),
                AgentStep(step_number=2, action="Create operational priority task", tool="CREATE_TASK", status="PENDING"),
                AgentStep(step_number=3, action="Review progress and complete workflow", tool="NOTIFY_USER", status="PENDING")
            ]

agent_orchestrator = AgentOrchestrator()
