# .agents

Definiciones de agentes, skills, tools y orquestación del proyecto.
Agnóstico de proveedor de IA — el modelo se configura en `config/models.yaml`.

## Estructura

```
.agents/
├── agents/          # definición de cada agente y sus system prompts
├── skills/          # capacidades reutilizables (contrato + implementación)
├── tools/           # herramientas externas y servidores MCP
├── orchestration/   # lógica de flujo y grafos de agentes
├── runtime/         # abstracción de LLM (LiteLLM / OpenAI-compatible)
├── config/          # modelos, entornos y parámetros globales
├── evals/           # benchmarks y tests por agente y skill
└── memory/          # esquemas de memoria corto/largo plazo
```

## Convención de nombres

- Definiciones: `*.yaml`
- Prompts: `*.md`
- Implementaciones: `*.py` / `*.ts`
- Tests: `*.eval.yaml`

## Cambiar de modelo

Edita `config/models.yaml` y ajusta el `provider` y `model` de cada agente.
El resto del sistema no necesita modificarse.
