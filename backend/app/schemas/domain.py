from pydantic import BaseModel, Field
from typing import List, Optional, Any, Dict
from datetime import datetime
from uuid import UUID

# Chat Schemas
class ChatMessage(BaseModel):
    role: str = "user" # user, assistant, system
    content: str

class ChatRequest(BaseModel):
    message: str
    include_memory: bool = True
    session_id: Optional[str] = None

class ChatResponse(BaseModel):
    reply: str
    recalled_memories: List[str] = []
    agent_suggestions: List[str] = []

# Memory Schemas
class MemoryCreate(BaseModel):
    category: str = "FACT" # PREFERENCE, WORK, GOAL, HABIT, FACT
    content: str
    importance_score: float = 0.5

class MemoryRead(BaseModel):
    id: UUID
    category: str
    content: str
    importance_score: float
    created_at: datetime

    class Config:
        from_attributes = True

# Task Schemas
class TaskCreate(BaseModel):
    title: str
    description: Optional[str] = None
    priority: str = "MEDIUM" # CRITICAL, HIGH, MEDIUM, LOW
    due_date: Optional[datetime] = None
    category: str = "General"

class TaskRead(BaseModel):
    id: UUID
    title: str
    description: Optional[str] = None
    priority: str
    is_completed: bool
    due_date: Optional[datetime] = None
    category: str
    created_at: datetime

    class Config:
        from_attributes = True

# Note Schemas
class NoteCreate(BaseModel):
    title: str
    content: str
    tags: Optional[List[str]] = []
    is_pinned: bool = False

class NoteRead(BaseModel):
    id: UUID
    title: str
    content: str
    tags: Optional[List[str]] = []
    is_pinned: bool
    created_at: datetime

    class Config:
        from_attributes = True

# Document RAG Schemas
class DocumentUploadRequest(BaseModel):
    title: str
    content: str

class DocumentSearchRequest(BaseModel):
    query: str
    top_k: int = 4

class DocumentSearchResult(BaseModel):
    document_title: str
    chunk_index: int
    content: str
    similarity_score: float

# Autonomous Agent Schemas
class AgentPlanRequest(BaseModel):
    goal: str

class AgentStep(BaseModel):
    step_number: int
    action: str
    tool: str
    status: str = "PENDING"
    result: Optional[str] = None

class AgentPlanResponse(BaseModel):
    plan_id: UUID
    goal: str
    status: str
    steps: List[AgentStep]
