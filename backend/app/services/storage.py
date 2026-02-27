import uuid

from supabase import create_client

from app.config import settings

BUCKET = "recordings"


def get_storage_service() -> "StorageService":
    return StorageService()


class StorageService:
    def __init__(self):
        self._client = create_client(settings.supabase_url, settings.supabase_key)
        self._ensure_bucket()

    def _ensure_bucket(self):
        try:
            self._client.storage.get_bucket(BUCKET)
        except Exception:
            self._client.storage.create_bucket(BUCKET, options={"public": True})

    def upload(self, file_data: bytes, filename: str, content_type: str) -> str:
        ext = filename.rsplit(".", 1)[-1] if "." in filename else "bin"
        path = f"{uuid.uuid4()}.{ext}"
        storage = self._client.storage.from_(BUCKET)
        storage.upload(path, file_data, {"content-type": content_type})
        return storage.get_public_url(path)
