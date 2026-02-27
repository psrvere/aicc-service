from datetime import date, datetime
from enum import Enum
from typing import Optional

from pydantic import BaseModel


class DealStage(str, Enum):
    New = "New"
    Contacted = "Contacted"
    Qualified = "Qualified"
    Proposal = "Proposal"
    Negotiation = "Negotiation"
    Won = "Won"
    Lost = "Lost"
    NotInterested = "NotInterested"


class Disposition(str, Enum):
    Connected = "Connected"
    NoAnswer = "NoAnswer"
    Voicemail = "Voicemail"
    Callback = "Callback"
    NotInterested = "NotInterested"
    WrongNumber = "WrongNumber"


# --- Contact models ---


class ContactCreate(BaseModel):
    name: str
    phone: str
    contact_person: Optional[str] = None
    city: Optional[str] = None
    industry: Optional[str] = None
    source: Optional[str] = None
    deal_stage: DealStage = DealStage.New
    notes: Optional[str] = None
    next_follow_up: Optional[date] = None


class ContactUpdate(BaseModel):
    name: Optional[str] = None
    phone: Optional[str] = None
    contact_person: Optional[str] = None
    city: Optional[str] = None
    industry: Optional[str] = None
    source: Optional[str] = None
    deal_stage: Optional[DealStage] = None
    notes: Optional[str] = None
    next_follow_up: Optional[date] = None
    last_call_summary: Optional[str] = None
    recording_link: Optional[str] = None


class Contact(BaseModel):
    id: str
    name: str
    contact_person: Optional[str] = None
    phone: str
    city: Optional[str] = None
    industry: Optional[str] = None
    source: Optional[str] = None
    deal_stage: DealStage = DealStage.New
    last_called: Optional[datetime] = None
    next_follow_up: Optional[date] = None
    call_count: int = 0
    last_call_summary: Optional[str] = None
    recording_link: Optional[str] = None
    notes: Optional[str] = None


# --- Call log models ---


class CallLogCreate(BaseModel):
    contact_id: str
    duration_seconds: int
    disposition: Disposition
    summary: Optional[str] = None
    deal_stage: Optional[DealStage] = None
    recording_url: Optional[str] = None
    transcript: Optional[str] = None
    next_follow_up: Optional[date] = None


class CallLog(BaseModel):
    id: str
    contact_id: str
    timestamp: datetime
    duration_seconds: int
    disposition: Disposition
    summary: Optional[str] = None
    deal_stage: DealStage
    recording_url: Optional[str] = None
    transcript: Optional[str] = None


# --- Call plan ---


class CallPlanItem(BaseModel):
    id: str
    name: str
    phone: str
    contact_person: Optional[str] = None
    deal_stage: DealStage
    next_follow_up: Optional[date] = None
    call_count: int
    last_call_summary: Optional[str] = None
    reason: str


# --- Dashboard ---


class DashboardStats(BaseModel):
    calls_today: int
    connected_today: int
    conversion_rate: float
    streak: int
    pipeline: dict[str, int]


# --- AI / Recordings ---


class AISummary(BaseModel):
    summary: str
    recommended_deal_stage: DealStage
    next_action: str


class TranscriptionResponse(BaseModel):
    text: str


class RecordingUploadResponse(BaseModel):
    url: str
