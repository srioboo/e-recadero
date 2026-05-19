"""
orchestration/graph.py
Grafo de agentes con LangGraph.
Lee las definiciones de .agents/agents/agents.yaml y construye el flujo.

Instalación: pip install langgraph
"""
from __future__ import annotations
from typing import TypedDict
from langgraph.graph import StateGraph, END

from runtime.llm_client import get_client
from skills.implementations import summarize


# --- Estado compartido entre nodos ---

class WorkflowState(TypedDict):
    task: str
    research_result: dict | None
    final_report: str | None
    error: str | None


# --- Nodos ---

def research_node(state: WorkflowState) -> WorkflowState:
    client = get_client("research_agent")
    result = client.complete(
        prompt=state["task"],
        response_format={"type": "json_object"},
    )
    return {**state, "research_result": result}


def writer_node(state: WorkflowState) -> WorkflowState:
    if not state.get("research_result"):
        return {**state, "error": "No hay resultado de investigación"}

    summary = summarize.run(
        text=str(state["research_result"]),
        max_words=300,
        style="paragraph",
    )
    client = get_client("writer_agent")
    report = client.complete(prompt=f"Redacta un informe basado en: {summary}")
    return {**state, "final_report": report}


def router(state: WorkflowState) -> str:
    if state.get("error"):
        return END
    if state.get("research_result") is None:
        return "research"
    return "writer"


# --- Construcción del grafo ---

def build_graph() -> StateGraph:
    graph = StateGraph(WorkflowState)

    graph.add_node("research", research_node)
    graph.add_node("writer", writer_node)

    graph.set_conditional_entry_point(router)
    graph.add_edge("research", "writer")
    graph.add_edge("writer", END)

    return graph.compile()


if __name__ == "__main__":
    workflow = build_graph()
    result = workflow.invoke({"task": "Investiga las tendencias de IA en 2025"})
    print(result["final_report"])
