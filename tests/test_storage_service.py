from unittest.mock import MagicMock, patch

import pytest


@pytest.fixture
def storage_service():
    with patch("app.services.storage.settings") as mock_settings:
        mock_settings.supabase_url = "https://test.supabase.co"
        mock_settings.supabase_key = "test-key"

        with patch("app.services.storage.create_client") as mock_create:
            mock_supabase = MagicMock()
            mock_create.return_value = mock_supabase
            mock_storage = MagicMock()
            mock_supabase.storage.from_.return_value = mock_storage

            from app.services.storage import StorageService
            service = StorageService()

            yield service, mock_storage, mock_settings


def test_upload_returns_url(storage_service):
    service, mock_storage, mock_settings = storage_service
    mock_storage.upload.return_value = None
    mock_storage.get_public_url.return_value = "https://test.supabase.co/storage/v1/object/public/recordings/audio.mp3"

    url = service.upload(b"audio-bytes", "audio.mp3", "audio/mpeg")
    assert "audio.mp3" in url or "recordings" in url
    mock_storage.upload.assert_called_once()


def test_upload_generates_unique_path(storage_service):
    service, mock_storage, _ = storage_service
    mock_storage.upload.return_value = None
    mock_storage.get_public_url.return_value = "https://example.com/file.mp3"

    service.upload(b"data1", "call1.mp3", "audio/mpeg")
    service.upload(b"data2", "call2.mp3", "audio/mpeg")

    call1_path = mock_storage.upload.call_args_list[0][0][0]
    call2_path = mock_storage.upload.call_args_list[1][0][0]
    assert call1_path != call2_path


def test_upload_error_raises(storage_service):
    service, mock_storage, _ = storage_service
    mock_storage.upload.side_effect = Exception("Upload failed")

    with pytest.raises(Exception, match="Upload failed"):
        service.upload(b"data", "file.mp3", "audio/mpeg")
