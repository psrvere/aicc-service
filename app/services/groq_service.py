import json
from io import BytesIO

from groq import Groq

from app.config import settings

WHISPER_MODEL = "whisper-large-v3"
LLM_MODEL = "llama-3.3-70b-versatile"

SUMMARIZE_SYSTEM_PROMPT = """You are a sales call analyst. Analyze this transcript and return JSON:
{"summary": "2-3 sentences", "recommended_deal_stage": "<enum>", "next_action": "<specific step>"}

Valid deal stages: New, Contacted, Qualified, Proposal, Negotiation, Won, Lost, NotInterested

Return ONLY valid JSON, no other text."""


def get_groq_service() -> "GroqService":
    return GroqService()


class GroqService:
    def __init__(self):
        self._client = Groq(api_key=settings.groq_api_key)

    def transcribe(self, audio_data: bytes, filename: str) -> str:
        audio_file = BytesIO(audio_data)
        audio_file.name = filename
        response = self._client.audio.transcriptions.create(
            model=WHISPER_MODEL,
            file=audio_file,
        )
        return response.text

    def summarize(
        self,
        transcript: str,
        contact_name: str,
        business: str | None,
        industry: str | None,
        deal_stage: str,
    ) -> dict:
        user_prompt = (
            f"Contact: {contact_name} at {business or 'N/A'} "
            f"({industry or 'N/A'}), current stage: {deal_stage}\n"
            f"Transcript: {transcript}"
        )

        response = self._client.chat.completions.create(
            model=LLM_MODEL,
            messages=[
                {"role": "system", "content": SUMMARIZE_SYSTEM_PROMPT},
                {"role": "user", "content": user_prompt},
            ],
            temperature=0.3,
        )

        content = response.choices[0].message.content
        try:
            return json.loads(content)
        except (json.JSONDecodeError, TypeError):
            return {
                "summary": content,
                "recommended_deal_stage": deal_stage,
                "next_action": "Review transcript manually",
            }
