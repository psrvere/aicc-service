import httpx
from fastapi import APIRouter, Depends, HTTPException, UploadFile

from app.auth import get_current_user
from app.services.groq_service import GroqService, get_groq_service
from app.services.sheets import SheetsService, get_sheets_service
from app.services.storage import StorageService, get_storage_service
from pydantic import BaseModel

router = APIRouter(prefix="/api/recordings", tags=["recordings"])


class TranscribeRequest(BaseModel):
    recording_url: str


class SummarizeRequest(BaseModel):
    contact_id: str
    transcript: str


@router.post("/upload")
async def upload_recording(
    file: UploadFile,
    storage: StorageService = Depends(get_storage_service),
    user: dict = Depends(get_current_user),
):
    data = await file.read()
    url = storage.upload(data, file.filename, file.content_type or "audio/mpeg")
    return {"url": url}


@router.post("/transcribe")
async def transcribe_recording(
    request: TranscribeRequest,
    groq: GroqService = Depends(get_groq_service),
    user: dict = Depends(get_current_user),
):
    response = httpx.get(request.recording_url)
    response.raise_for_status()
    filename = request.recording_url.rsplit("/", 1)[-1] or "audio.mp3"
    text = groq.transcribe(response.content, filename)
    return {"text": text}


@router.post("/summarize")
async def summarize_recording(
    request: SummarizeRequest,
    groq: GroqService = Depends(get_groq_service),
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    contact = sheets.get_contact_by_id(request.contact_id)
    if contact is None:
        raise HTTPException(status_code=404, detail="Contact not found")

    result = groq.summarize(
        transcript=request.transcript,
        contact_name=contact.get("contact_person") or contact.get("name", ""),
        business=contact.get("name"),
        industry=contact.get("industry"),
        deal_stage=contact.get("deal_stage", "New"),
    )
    return result
