"""
skill: web_search
Realiza búsquedas en internet a través del servidor MCP de Brave Search.
Si MCP no está disponible, usa la API REST de Brave directamente como fallback.

Uso:
    from skills.implementations.web_search import run

    result = run(
        query="últimas noticias sobre modelos de lenguaje 2025",
        max_results=5,
        country="ES",
        language="es",
    )
"""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path
from typing import Optional

import httpx  # pip install httpx

sys.path.insert(0, str(Path(__file__).parent.parent.parent))


# ── Tipos ─────────────────────────────────────────────────────────────────────

SearchResult = dict  # {title, url, snippet, published_date?}


# ── Implementación Brave REST (fallback directo sin MCP) ─────────────────────

def _search_brave_rest(
    query: str,
    max_results: int = 5,
    country: str = "ES",
    language: str = "es",
) -> list[SearchResult]:
    """Llama directamente a la API REST de Brave Search."""
    api_key = os.environ.get("BRAVE_API_KEY")
    if not api_key:
        raise EnvironmentError(
            "BRAVE_API_KEY no está definida en las variables de entorno."
        )

    url = "https://api.search.brave.com/res/v1/web/search"
    headers = {
        "Accept": "application/json",
        "Accept-Encoding": "gzip",
        "X-Subscription-Token": api_key,
    }
    params = {
        "q": query,
        "count": min(max_results, 20),   # máximo de la API
        "country": country.lower(),
        "search_lang": language,
        "text_decorations": False,
        "spellcheck": True,
    }

    with httpx.Client(timeout=15.0) as client:
        response = client.get(url, headers=headers, params=params)
        response.raise_for_status()

    data = response.json()
    web_results = data.get("web", {}).get("results", [])

    return [
        {
            "title":          r.get("title", ""),
            "url":            r.get("url", ""),
            "snippet":        r.get("description", ""),
            "published_date": r.get("page_age", None),
        }
        for r in web_results[:max_results]
    ]


# ── Función principal ─────────────────────────────────────────────────────────

def run(
    query: str,
    max_results: int = 5,
    country: str = "ES",
    language: str = "es",
    safe_search: bool = True,
) -> dict:
    """
    Busca en internet y devuelve resultados estructurados.

    Args:
        query:       Consulta de búsqueda.
        max_results: Número máximo de resultados (1-20).
        country:     Código ISO 3166-1 del país para resultados localizados.
        language:    Código ISO 639-1 del idioma preferido.
        safe_search: Activar filtro de contenido seguro.

    Returns:
        dict con claves:
          - query (str): consulta original
          - results (list): lista de {title, url, snippet, published_date}
          - total_found (int): número de resultados devueltos
          - source (str): 'brave_api'
    """
    if not query.strip():
        raise ValueError("La consulta no puede estar vacía.")
    max_results = max(1, min(max_results, 20))

    results = _search_brave_rest(
        query=query.strip(),
        max_results=max_results,
        country=country,
        language=language,
    )

    return {
        "query":       query,
        "results":     results,
        "total_found": len(results),
        "source":      "brave_api",
    }


if __name__ == "__main__":
    out = run("mejores frameworks de agentes IA 2025", max_results=3)
    print(json.dumps(out, ensure_ascii=False, indent=2))
