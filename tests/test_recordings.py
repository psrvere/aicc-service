from io import BytesIO
from unittest.mock import MagicMock, patch

from app.main import app
from app.services.groq_service import get_groq_service
from app.services.storage import get_storage_service


def test_upload_recording(client, mock_sheets):
    mock_storage = MagicMock()
    mock_storage.upload.return_value = "https://storage.example.com/recordings/test.mp3"
    app.dependency_overrides[get_storage_service] = lambda: mock_storage

    response = client.post(
        "/api/recordings/upload",
        files={"file": ("test.mp3", BytesIO(b"fake-audio"), "audio/mpeg")},
    )
    assert response.status_code == 200
    assert response.json()["url"] == "https://storage.example.com/recordings/test.mp3"
    mock_storage.upload.assert_called_once()


def test_transcribe_recording(client, mock_sheets):
    mock_groq = MagicMock()
    mock_groq.transcribe.return_value = "Hello, this is a test."
    app.dependency_overrides[get_groq_service] = lambda: mock_groq

    with patch("app.routers.recordings.httpx") as mock_httpx:
        mock_response = MagicMock()
        mock_response.content = b"fake-audio-bytes"
        mock_response.raise_for_status = MagicMock()
        mock_httpx.get.return_value = mock_response

        response = client.post(
            "/api/recordings/transcribe",
            json={"recording_url": "https://storage.example.com/audio.mp3"},
        )
        assert response.status_code == 200
        assert response.json()["text"] == "Hello, this is a test."


def test_summarize_recording(client, mock_sheets):
    mock_groq = MagicMock()
    mock_groq.summarize.return_value = {
        "summary": "Productive call about pricing.",
        "recommended_deal_stage": "Proposal",
        "next_action": "Send quote by Friday",
    }
    app.dependency_overrides[get_groq_service] = lambda: mock_groq

    mock_sheets.get_contact_by_id.return_value = {
        "id": "uuid-1", "name": "Alice", "phone": "123",
        "industry": "Tech", "deal_stage": "Qualified",
    }

    response = client.post(
        "/api/recordings/summarize",
        json={
            "contact_id": "uuid-1",
            "transcript": "Hi Alice, let's talk pricing...",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["summary"] == "Productive call about pricing."
    assert data["recommended_deal_stage"] == "Proposal"


def test_summarize_contact_not_found(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = None
    response = client.post(
        "/api/recordings/summarize",
        json={
            "contact_id": "nonexistent",
            "transcript": "test",
        },
    )
    assert response.status_code == 404
