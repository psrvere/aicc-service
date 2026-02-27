import pytest
from pydantic import ValidationError

from app.models import (
    AISummary,
    CallLog,
    CallLogCreate,
    CallPlanItem,
    Contact,
    ContactCreate,
    ContactUpdate,
    DashboardStats,
    DealStage,
    Disposition,
    RecordingUploadResponse,
    TranscriptionResponse,
)


def test_deal_stage_has_8_values():
    assert len(DealStage) == 8
    expected = {
        "New", "Contacted", "Qualified", "Proposal",
        "Negotiation", "Won", "Lost", "NotInterested",
    }
    assert {s.value for s in DealStage} == expected


def test_disposition_has_6_values():
    assert len(Disposition) == 6
    expected = {
        "Connected", "NoAnswer", "Voicemail",
        "Callback", "NotInterested", "WrongNumber",
    }
    assert {d.value for d in Disposition} == expected


def test_contact_create_required_fields_only():
    c = ContactCreate(name="Alice", phone="123")
    assert c.name == "Alice"
    assert c.phone == "123"
    assert c.deal_stage == DealStage.New
    assert c.contact_person is None
    assert c.city is None
    assert c.industry is None
    assert c.source is None
    assert c.notes is None
    assert c.next_follow_up is None


def test_contact_create_all_fields():
    from datetime import date

    c = ContactCreate(
        name="Bob",
        phone="456",
        contact_person="Bob Smith",
        city="NYC",
        industry="Tech",
        source="IndiaMart",
        deal_stage=DealStage.Qualified,
        notes="VIP",
        next_follow_up=date(2026, 3, 1),
    )
    assert c.contact_person == "Bob Smith"
    assert c.source == "IndiaMart"
    assert c.deal_stage == DealStage.Qualified
    assert c.next_follow_up == date(2026, 3, 1)


def test_contact_create_missing_required_field():
    with pytest.raises(ValidationError):
        ContactCreate(name="Alice")  # missing phone


def test_contact_update_all_optional():
    u = ContactUpdate()
    assert u.name is None
    assert u.phone is None
    assert u.deal_stage is None


def test_contact_has_all_fields():
    c = Contact(
        id="uuid-1",
        name="Alice",
        phone="123",
        deal_stage=DealStage.New,
        call_count=0,
    )
    assert c.id == "uuid-1"
    assert c.call_count == 0
    assert c.last_called is None
    assert c.recording_link is None
    assert c.contact_person is None
    assert c.source is None


def test_call_log_create_required_fields():
    cl = CallLogCreate(
        contact_id="uuid-1",
        duration_seconds=120,
        disposition=Disposition.Connected,
    )
    assert cl.contact_id == "uuid-1"
    assert cl.duration_seconds == 120
    assert cl.summary is None
    assert cl.next_follow_up is None


def test_call_log_create_missing_required():
    with pytest.raises(ValidationError):
        CallLogCreate(contact_id="uuid-1")  # missing duration + disposition


def test_ai_summary_validation():
    s = AISummary(
        summary="Good call.",
        recommended_deal_stage=DealStage.Qualified,
        next_action="Send proposal",
    )
    assert s.recommended_deal_stage == DealStage.Qualified


def test_dashboard_stats_defaults():
    d = DashboardStats(
        calls_today=5,
        connected_today=3,
        conversion_rate=0.6,
        streak=2,
        pipeline={"New": 10, "Won": 2},
    )
    assert d.conversion_rate == 0.6
    assert d.pipeline["New"] == 10


def test_transcription_response():
    t = TranscriptionResponse(text="Hello world")
    assert t.text == "Hello world"


def test_recording_upload_response():
    r = RecordingUploadResponse(url="https://example.com/audio.mp3")
    assert r.url == "https://example.com/audio.mp3"
