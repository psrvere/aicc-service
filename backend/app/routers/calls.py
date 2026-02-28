from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException

from app.auth import get_current_user
from app.models import CallLogCreate
from app.services.sheets import SheetsService, get_sheets_service

router = APIRouter(prefix="/api/calls", tags=["calls"])

FOLLOW_UP_RULES = {
    "Callback": timedelta(days=1),
    "NoAnswer": timedelta(days=3),
    "Voicemail": timedelta(days=7),
}

CLEAR_FOLLOW_UP_DISPOSITIONS = {"NotInterested", "WrongNumber"}


@router.post("/log", status_code=201)
async def log_call(
    call: CallLogCreate,
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    contact = sheets.get_contact_by_id(call.contact_id)
    if contact is None:
        raise HTTPException(status_code=404, detail="Contact not found")

    # Build call log data
    log_data = {
        "contact_id": call.contact_id,
        "contact_name": contact.get("name") or "",
        "duration_seconds": call.duration_seconds,
        "disposition": call.disposition.value,
        "summary": call.summary or "",
        "deal_stage": call.deal_stage.value if call.deal_stage else contact.get("deal_stage") or "",
        "recording_url": call.recording_url or "",
    }
    call_log = sheets.append_call_log(log_data)

    # Compute follow-up
    disposition = call.disposition.value
    if disposition in FOLLOW_UP_RULES:
        next_follow_up = (date.today() + FOLLOW_UP_RULES[disposition]).isoformat()
    elif disposition in CLEAR_FOLLOW_UP_DISPOSITIONS:
        next_follow_up = ""
    elif disposition == "Connected" and call.next_follow_up:
        next_follow_up = call.next_follow_up.isoformat()
    else:
        next_follow_up = None

    # Update contact
    update_data = {
        "call_count": contact.get("call_count", 0) + 1,
        "last_called": date.today().isoformat(),
    }
    if call.summary:
        update_data["last_call_summary"] = call.summary
    if next_follow_up is not None:
        update_data["next_follow_up"] = next_follow_up

    try:
        sheets.update_contact(call.contact_id, update_data)
    except ValueError:
        raise HTTPException(status_code=404, detail="Contact not found")

    return call_log
