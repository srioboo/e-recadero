"""
skill: extract_data
Extrae entidades y datos estructurados de texto no estructurado,
siguiendo un JSON Schema proporcionado por el llamador.

Uso:
    from skills.implementations.extract_data import run

    schema = {
        "company": {"type": "string"},
        "founding_year": {"type": "integer"},
        "employees": {"type": "integer"},
        "headquarters": {"type": "string"},
        "products": {"type": "array", "items": {"type": "string"}}
    }

    result = run(
        text="Anthropic fue fundada en 2021 por Dario Amodei...",
        schema=schema,
    )
    # result = {
    #   "data": {"company": "Anthropic", "founding_year": 2021, ...},
    #   "confidence": 0.92,
    #   "missing_fields": []
    # }
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from runtime.llm_client import get_client  # noqa: E402


_SYSTEM = """
Eres un extractor de información estructurada.
Devuelves únicamente JSON válido, sin texto adicional ni backticks.
Si un campo no está presente en el texto, usa null.
Nunca inventes datos que no estén en el texto original.
""".strip()

_PROMPT = """
Extrae la siguiente información del texto. Sigue el schema exactamente.
Si un campo no aparece en el texto, usa null. No inventes datos.

Schema esperado:
{schema}

Texto:
\"\"\"
{text}
\"\"\"

Devuelve SOLO este JSON:
{{
  "data": {{ ...campos del schema... }},
  "confidence": <float entre 0.0 y 1.0>,
  "missing_fields": ["campo1", "campo2"]
}}

confidence: proporción de campos no-null sobre el total de campos del schema.
missing_fields: lista de campos que quedaron como null.
""".strip()


def run(
    text: str,
    schema: dict,
    agent_id: str = "default",
) -> dict:
    """
    Extrae datos estructurados de texto libre.

    Args:
        text:     Texto del que extraer información.
        schema:   Diccionario que describe los campos a extraer.
                  Claves = nombre del campo, valor = {"type": ..., "description": ...}
        agent_id: ID del agente para seleccionar modelo.

    Returns:
        dict con claves:
          - data (dict): campos extraídos según el schema
          - confidence (float): proporción de campos encontrados
          - missing_fields (list): campos con valor null
    """
    if not text.strip():
        raise ValueError("El texto no puede estar vacío.")
    if not schema:
        raise ValueError("El schema no puede estar vacío.")

    prompt = _PROMPT.format(
        text=text.strip(),
        schema=json.dumps(schema, ensure_ascii=False, indent=2),
    )

    client = get_client(agent_id)

    for attempt in range(3):
        raw = client.complete(
            prompt=prompt,
            system=_SYSTEM,
            response_format={"type": "json_object"},
        )

        if isinstance(raw, dict):
            result = raw
        else:
            try:
                clean = raw.strip().lstrip("```json").rstrip("```").strip()
                result = json.loads(clean)
            except json.JSONDecodeError as e:
                if attempt < 2:
                    continue
                raise RuntimeError(f"JSON inválido tras 3 intentos: {e}") from e

        if "data" not in result:
            if attempt < 2:
                continue
            raise RuntimeError("La respuesta no contiene la clave 'data'.")

        # Calcular confianza si el LLM no la proporcionó
        if "confidence" not in result:
            total = len(schema)
            found = sum(
                1 for v in result["data"].values() if v is not None
            )
            result["confidence"] = round(found / total, 2) if total else 0.0

        result.setdefault("missing_fields", [
            k for k, v in result["data"].items() if v is None
        ])

        return result

    raise RuntimeError("No se pudo extraer datos tras 3 intentos.")


if __name__ == "__main__":
    sample_text = """
    OpenAI es una empresa de investigación en inteligencia artificial fundada
    en diciembre de 2015 en San Francisco, California. Fue cofundada por
    Sam Altman, Greg Brockman, Ilya Sutskever y Elon Musk, entre otros.
    La empresa es conocida por desarrollar la serie de modelos GPT y el
    asistente ChatGPT, lanzado en noviembre de 2022. Actualmente cuenta
    con más de 1.000 empleados.
    """

    sample_schema = {
        "company_name":   {"type": "string",  "description": "Nombre de la empresa"},
        "founding_year":  {"type": "integer", "description": "Año de fundación"},
        "headquarters":   {"type": "string",  "description": "Ciudad sede"},
        "founders":       {"type": "array",   "description": "Lista de cofundadores"},
        "main_products":  {"type": "array",   "description": "Productos principales"},
        "employee_count": {"type": "integer", "description": "Número de empleados"},
        "stock_ticker":   {"type": "string",  "description": "Símbolo bursátil"},
    }

    result = run(sample_text, sample_schema)
    print(json.dumps(result, ensure_ascii=False, indent=2))
