from datetime import date

from fastapi import APIRouter, Depends

from app.auth import get_current_user
from app.services.sheets import SheetsService, get_sheets_service

router = APIRouter(prefix="/api/callplan", tags=["call_plan"])

EXCLUDED_STAGES = {"Won", "Lost", "NotInterested"}


@router.get("/today")
async def todays_call_plan(
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    contacts = sheets.get_all_contacts()
    today = date.today().isoformat()

    follow_ups = []
    new_contacts = []

    for c in contacts:
        if c.get("deal_stage") in EXCLUDED_STAGES:
            continue

        # Follow-up due today or earlier
        if c.get("next_follow_up") and c["next_follow_up"] <= today:
            follow_ups.append(_to_plan_item(c, "Follow-up due"))
        # New contact never called
        elif c.get("deal_stage") == "New" and c.get("call_count", 0) == 0:
            new_contacts.append(_to_plan_item(c, "New contact"))

    return follow_ups + new_contacts


def _to_plan_item(contact: dict, reason: str) -> dict:
    return {
        "id": contact["id"],
        "name": contact["name"],
        "phone": contact["phone"],
        "contact_person": contact.get("contact_person"),
        "deal_stage": contact.get("deal_stage"),
        "next_follow_up": contact.get("next_follow_up"),
        "call_count": contact.get("call_count", 0),
        "last_call_summary": contact.get("last_call_summary"),
        "reason": reason,
    }
