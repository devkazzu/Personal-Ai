from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
import uuid

from app.db.session import get_db
from app.models.entities import Task, Note, Memory, DocumentChunk, AgentPlan
from app.schemas.domain import (
    ChatRequest, ChatResponse,
    TaskCreate, TaskRead,
    NoteCreate, NoteRead,
    MemoryCreate, MemoryRead,
    DocumentUploadRequest, DocumentSearchRequest, DocumentSearchResult,
    AgentPlanRequest, AgentPlanResponse, AgentStep
)
from app.ai.gemini_service import gemini_client
from app.agents.orchestrator import agent_orchestrator

api_router = APIRouter()

# --- AI Chat & Neural Reasoning ---
@api_router.post("/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest, db: AsyncSession = Depends(get_db)):
    recalled_memories_texts = []
    
    if request.include_memory:
        # Fetch top relevant memories
        result = await db.execute(select(Memory).order_by(Memory.importance_score.desc()).limit(5))
        memories = result.scalars().all()
        recalled_memories_texts = [f"[{m.category}] {m.content}" for m in memories]

    system_instruction = (
        "You are the central intelligence of AI Personal OS. "
        "Be concise, highly proactive, empathetic, and organized. "
        "Incorporate relevant memory context seamlessly into your responses."
    )
    
    prompt = f"User Memory Context:\n" + "\n".join(recalled_memories_texts) + f"\n\nUser Message: {request.message}"
    reply = await gemini_client.generate_response(prompt, system_instruction)
    
    return ChatResponse(
        reply=reply,
        recalled_memories=recalled_memories_texts,
        agent_suggestions=["Break into executable plan", "Log to Memory Vault", "Create priority task"]
    )

# --- Tasks API ---
@api_router.get("/tasks", response_model=List[TaskRead])
async def get_tasks(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Task).order_by(Task.created_at.desc()))
    return result.scalars().all()

@api_router.post("/tasks", response_model=TaskRead)
async def create_task(task_in: TaskCreate, db: AsyncSession = Depends(get_db)):
    task = Task(**task_in.model_dump())
    db.add(task)
    await db.commit()
    await db.refresh(task)
    return task

# --- Notes API ---
@api_router.get("/notes", response_model=List[NoteRead])
async def get_notes(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Note).order_by(Note.created_at.desc()))
    return result.scalars().all()

@api_router.post("/notes", response_model=NoteRead)
async def create_note(note_in: NoteCreate, db: AsyncSession = Depends(get_db)):
    note = Note(**note_in.model_dump())
    db.add(note)
    await db.commit()
    await db.refresh(note)
    return note

# --- Long-Term Memory Vault ---
@api_router.get("/memories", response_model=List[MemoryRead])
async def get_memories(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Memory).order_by(Memory.importance_score.desc()))
    return result.scalars().all()

@api_router.post("/memories", response_model=MemoryRead)
async def create_memory(mem_in: MemoryCreate, db: AsyncSession = Depends(get_db)):
    embedding = await gemini_client.generate_embedding(mem_in.content)
    memory = Memory(
        category=mem_in.category,
        content=mem_in.content,
        importance_score=mem_in.importance_score,
        embedding=embedding
    )
    db.add(memory)
    await db.commit()
    await db.refresh(memory)
    return memory

# --- Autonomous Agent Engine ---
@api_router.post("/agent/plan", response_model=AgentPlanResponse)
async def create_agent_plan(req: AgentPlanRequest, db: AsyncSession = Depends(get_db)):
    steps = await agent_orchestrator.create_plan(req.goal)
    plan = AgentPlan(
        goal=req.goal,
        status="IN_PROGRESS",
        steps=[s.model_dump() for s in steps]
    )
    db.add(plan)
    await db.commit()
    await db.refresh(plan)
    return AgentPlanResponse(
        plan_id=plan.id,
        goal=plan.goal,
        status=plan.status,
        steps=steps
    )
