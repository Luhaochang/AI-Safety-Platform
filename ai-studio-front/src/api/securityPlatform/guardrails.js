/**
 * Guardrails API – real llm-guard integration
 * Proxied via Vite  /rag-api  →  http://localhost:8765
 */
import axios from 'axios';

export function getGuardrailsStatus() {
    return axios.get('/rag-api/guardrails/status', { timeout: 5000 });
}

export function scanInput(text, config = {}) {
    return axios.post('/rag-api/guardrails/scan-input',
        { text, config }, { timeout: 30000 });
}

export function scanOutput(prompt, output, context = '', config = {}) {
    return axios.post('/rag-api/guardrails/scan-output',
        { prompt, output, context, config }, { timeout: 30000 });
}
