"""
runtime/llm_client.py
Abstracción de LLM usando LiteLLM — el resto del sistema no importa
ningún SDK de proveedor directamente.

Instalación: pip install litellm
"""
from __future__ import annotations
import os
import yaml
import litellm
from pathlib import Path


_CONFIG_PATH = Path(__file__).parent.parent / "config" / "models.yaml"
_ENV = os.getenv("AGENT_ENV", "development")


def _load_model_config(agent_id: str) -> dict:
    with open(_CONFIG_PATH) as f:
        config = yaml.safe_load(f)

    defaults = config.get("defaults", {})
    env_config = config.get("environments", {}).get(_ENV, {})
    agent_config = env_config.get(agent_id, {})

    return {**defaults, **agent_config}


class LLMClient:
    def __init__(self, agent_id: str):
        self.cfg = _load_model_config(agent_id)

    def complete(
        self,
        prompt: str,
        system: str | None = None,
        response_format: dict | None = None,
    ) -> dict | str:
        messages = []
        if system:
            messages.append({"role": "system", "content": system})
        messages.append({"role": "user", "content": prompt})

        kwargs: dict = {
            "model": f"{self.cfg['provider']}/{self.cfg['model']}",
            "messages": messages,
            "temperature": self.cfg.get("temperature", 0.2),
            "max_tokens": self.cfg.get("max_tokens", 4096),
        }
        if response_format:
            kwargs["response_format"] = response_format

        response = litellm.completion(**kwargs)
        content = response.choices[0].message.content

        if response_format and response_format.get("type") == "json_object":
            import json
            return json.loads(content)

        return content


def get_client(agent_id: str = "default") -> LLMClient:
    return LLMClient(agent_id)
