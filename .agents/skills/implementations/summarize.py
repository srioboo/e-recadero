"""
skill: summarize
Résume un texto usando el LLM configurado en runtime/llm_client.py.

Uso directo:
    from skills.implementations.summarize import run

    result = run(
        text="...",
        max_words=150,
        style="bullets",
        language="es",
        focus="aspectos técnicos",
    )
    # result = {"summary": "...", "bullets": [...], "word_count": 87}
"""
from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Literal

# Permite ejecutar el archivo directamente desde la raíz del proyecto
sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from runtime.llm_client import get_client  # noqa: E402


# ── Plantillas de prompt ──────────────────────────────────────────────────────

_SYSTEM = """
Eres un asistente especializado en síntesis de información.
Devuelves únicamente JSON válido, sin texto adicional, sin markdown, sin backticks.
""".strip()

_PROMPT_BULLETS = """
Resume el siguiente texto en viñetas concisas.
Idioma de salida: {language}
Máximo {max_words} palabras en total.
{focus_instruction}

Texto:
\"\"\"
{text}
\"\"\"

Responde SOLO con este JSON (sin nada más):
{{
  "summary": "resumen en 1-2 oraciones",
  "bullets": ["punto 1", "punto 2", "..."],
  "word_count": <número entero>
}}
""".strip()

_PROMPT_PARAGRAPH = """
Resume el siguiente texto en un párrafo fluido y cohesivo.
Idioma de salida: {language}
Máximo {max_words} palabras.
{focus_instruction}

Texto:
\"\"\"
{text}
\"\"\"

Responde SOLO con este JSON (sin nada más):
{{
  "summary": "párrafo de resumen completo",
  "bullets": [],
  "word_count": <número entero>
}}
""".strip()

_PROMPT_HEADLINE = """
Resume el siguiente texto en una sola frase titular (máx. 20 palabras)
y 3 bullets de apoyo.
Idioma de salida: {language}
{focus_instruction}

Texto:
\"\"\"
{text}
\"\"\"

Responde SOLO con este JSON (sin nada más):
{{
  "summary": "frase titular única",
  "bullets": ["bullet 1", "bullet 2", "bullet 3"],
  "word_count": <número entero>
}}
""".strip()

_TEMPLATES = {
    "bullets":   _PROMPT_BULLETS,
    "paragraph": _PROMPT_PARAGRAPH,
    "headline":  _PROMPT_HEADLINE,
}


# ── Función principal ─────────────────────────────────────────────────────────

def run(
    text: str,
    max_words: int = 150,
    style: Literal["bullets", "paragraph", "headline"] = "bullets",
    language: str = "es",
    focus: str | None = None,
    agent_id: str = "default",
) -> dict:
    """
    Resume un texto.

    Args:
        text:      Texto a resumir. Mínimo 50 caracteres.
        max_words: Máximo de palabras en el output (bullets + summary).
        style:     'bullets' | 'paragraph' | 'headline'
        language:  Código ISO 639-1 del idioma de salida. Default: 'es'.
        focus:     Aspecto concreto a priorizar, e.g. "implicaciones económicas".
        agent_id:  ID del agente para seleccionar el modelo en models.yaml.

    Returns:
        dict con claves: summary (str), bullets (list[str]), word_count (int)

    Raises:
        ValueError: Si el texto está vacío o es demasiado corto.
        RuntimeError: Si el LLM no devuelve JSON válido tras los reintentos.
    """
    # Validaciones
    text = text.strip()
    if len(text) < 50:
        raise ValueError(
            f"El texto es demasiado corto ({len(text)} caracteres). Mínimo 50."
        )
    if style not in _TEMPLATES:
        raise ValueError(f"style debe ser uno de: {list(_TEMPLATES.keys())}")

    focus_instruction = (
        f"Presta especial atención a: {focus}." if focus else ""
    )

    prompt = _TEMPLATES[style].format(
        text=text,
        max_words=max_words,
        language=language,
        focus_instruction=focus_instruction,
    )

    client = get_client(agent_id)

    # Reintentos ante JSON malformado
    last_error: Exception | None = None
    for attempt in range(3):
        raw = client.complete(
            prompt=prompt,
            system=_SYSTEM,
            response_format={"type": "json_object"},
        )

        # El cliente puede devolver dict directamente si usó json_object
        if isinstance(raw, dict):
            result = raw
        else:
            try:
                # Limpiar posibles backticks residuales
                clean = raw.strip().lstrip("```json").rstrip("```").strip()
                result = json.loads(clean)
            except json.JSONDecodeError as e:
                last_error = e
                if attempt < 2:
                    continue
                raise RuntimeError(
                    f"El LLM no devolvió JSON válido tras 3 intentos. "
                    f"Último error: {e}\nRespuesta recibida:\n{raw}"
                ) from e

        # Validar estructura mínima
        if "summary" not in result:
            last_error = ValueError("JSON sin clave 'summary'")
            continue

        result.setdefault("bullets", [])
        result.setdefault("word_count", len(result["summary"].split()))
        return result

    raise RuntimeError(
        f"No se pudo obtener un resumen válido. Último error: {last_error}"
    )


# ── Ejecución directa para pruebas ───────────────────────────────────────────

if __name__ == "__main__":
    sample = """
    La computación cuántica representa un cambio de paradigma en el procesamiento
    de información. A diferencia de los ordenadores clásicos que usan bits (0 o 1),
    los ordenadores cuánticos utilizan qubits que pueden existir en superposición
    de ambos estados simultáneamente. Esto les permite explorar múltiples soluciones
    a un problema al mismo tiempo. Empresas como IBM, Google y startups como IonQ
    están compitiendo por alcanzar la "supremacía cuántica" — el punto en que un
    ordenador cuántico supera a cualquier ordenador clásico en una tarea específica.
    Google afirmó haberlo logrado en 2019, aunque IBM lo disputó. Las aplicaciones
    prometidas incluyen criptografía, descubrimiento de fármacos y optimización
    logística, pero los expertos advierten que las aplicaciones prácticas masivas
    aún están a una o dos décadas de distancia.
    """

    for s in ("bullets", "paragraph", "headline"):
        print(f"\n--- style: {s} ---")
        try:
            out = run(sample, max_words=100, style=s, focus="competencia empresarial")
            print(json.dumps(out, ensure_ascii=False, indent=2))
        except Exception as exc:
            print(f"Error: {exc}")
