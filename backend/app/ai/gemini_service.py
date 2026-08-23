import httpx
from typing import List, Optional
from app.core.config import settings

class GeminiClient:
    def __init__(self):
        self.api_key = settings.GEMINI_API_KEY
        self.model = settings.GEMINI_MODEL
        self.base_url = "https://generativelanguage.googleapis.com/v1beta/models"

    async def generate_response(self, prompt: str, system_instruction: Optional[str] = None) -> str:
        if not self.api_key:
            return "Gemini API key not configured on backend. Operating in offline edge mode."

        url = f"{self.base_url}/{self.model}:generateContent?key={self.api_key}"
        payload = {
            "contents": [{"parts": [{"text": prompt}]}]
        }
        if system_instruction:
            payload["systemInstruction"] = {"parts": [{"text": system_instruction}]}

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(url, json=payload)
            if response.status_code == 200:
                data = response.json()
                try:
                    return data["candidates"][0]["content"]["parts"][0]["text"]
                except (KeyError, IndexError):
                    return "No response generated."
            else:
                return f"Gemini API Error: {response.status_code} - {response.text}"

    async def generate_embedding(self, text: str) -> List[float]:
        if not self.api_key:
            # Fallback zero-vector if key is not yet set
            return [0.0] * 768

        url = f"{self.base_url}/{settings.EMBEDDING_MODEL}:embedContent?key={self.api_key}"
        payload = {
            "content": {"parts": [{"text": text}]}
        }
        async with httpx.AsyncClient(timeout=15.0) as client:
            response = await client.post(url, json=payload)
            if response.status_code == 200:
                data = response.json()
                return data.get("embedding", {}).get("values", [0.0] * 768)
            return [0.0] * 768

gemini_client = GeminiClient()
