import json
from unittest.mock import MagicMock, patch

import pytest


@pytest.fixture
def groq_service():
    with patch("app.services.groq_service.settings") as mock_settings:
        mock_settings.groq_api_key = "test-groq-key"

        with patch("app.services.groq_service.Groq") as mock_groq_cls:
            mock_client = MagicMock()
            mock_groq_cls.return_value = mock_client

            from app.services.groq_service import GroqService
            service = GroqService()

            yield service, mock_client


def test_transcribe_returns_text(groq_service):
    service, mock_client = groq_service
    mock_response = MagicMock()
    mock_response.text = "Hello, this is a test call."
    mock_client.audio.transcriptions.create.return_value = mock_response

    result = service.transcribe(b"audio-bytes", "audio.mp3")
    assert result == "Hello, this is a test call."


def test_transcribe_uses_whisper_model(groq_service):
    service, mock_client = groq_service
    mock_response = MagicMock()
    mock_response.text = "test"
    mock_client.audio.transcriptions.create.return_value = mock_response

    service.transcribe(b"data", "file.mp3")
    call_kwargs = mock_client.audio.transcriptions.create.call_args
    assert "whisper" in call_kwargs.kwargs.get("model", "").lower()


def test_summarize_returns_ai_summary(groq_service):
    service, mock_client = groq_service
    summary_json = json.dumps({
        "summary": "Good introductory call.",
        "recommended_deal_stage": "Contacted",
        "next_action": "Send product brochure",
    })
    mock_message = MagicMock()
    mock_message.content = summary_json
    mock_choice = MagicMock()
    mock_choice.message = mock_message
    mock_response = MagicMock()
    mock_response.choices = [mock_choice]
    mock_client.chat.completions.create.return_value = mock_response

    result = service.summarize(
        transcript="Hi, I'm calling about...",
        contact_name="Alice",
        business="Acme Corp",
        industry="Tech",
        deal_stage="New",
    )
    assert result["summary"] == "Good introductory call."
    assert result["recommended_deal_stage"] == "Contacted"
    assert result["next_action"] == "Send product brochure"


def test_summarize_prompt_includes_context(groq_service):
    service, mock_client = groq_service
    mock_message = MagicMock()
    mock_message.content = json.dumps({
        "summary": "s", "recommended_deal_stage": "New", "next_action": "a",
    })
    mock_choice = MagicMock()
    mock_choice.message = mock_message
    mock_response = MagicMock()
    mock_response.choices = [mock_choice]
    mock_client.chat.completions.create.return_value = mock_response

    service.summarize(
        transcript="test",
        contact_name="Bob",
        business="BigCo",
        industry="Finance",
        deal_stage="Qualified",
    )
    call_args = mock_client.chat.completions.create.call_args
    messages = call_args.kwargs.get("messages", [])
    user_msg = messages[-1]["content"]
    assert "Bob" in user_msg
    assert "BigCo" in user_msg
    assert "Finance" in user_msg
    assert "Qualified" in user_msg


def test_summarize_invalid_json_fallback(groq_service):
    service, mock_client = groq_service
    mock_message = MagicMock()
    mock_message.content = "This is not valid JSON"
    mock_choice = MagicMock()
    mock_choice.message = mock_message
    mock_response = MagicMock()
    mock_response.choices = [mock_choice]
    mock_client.chat.completions.create.return_value = mock_response

    result = service.summarize(
        transcript="test",
        contact_name="Alice",
        business="Acme",
        industry="Tech",
        deal_stage="New",
    )
    assert "summary" in result
    assert result["recommended_deal_stage"] == "New"
