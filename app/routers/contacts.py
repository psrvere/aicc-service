from fastapi import APIRouter, Depends, HTTPException

from app.auth import get_current_user
from app.models import ContactCreate, ContactUpdate
from app.services.sheets import SheetsService, get_sheets_service

router = APIRouter(prefix="/api/contacts", tags=["contacts"])


@router.get("")
async def list_contacts(
    deal_stage: str | None = None,
    city: str | None = None,
    industry: str | None = None,
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    contacts = sheets.get_all_contacts()
    if deal_stage:
        contacts = [c for c in contacts if c.get("deal_stage") == deal_stage]
    if city:
        contacts = [c for c in contacts if c.get("city") == city]
    if industry:
        contacts = [c for c in contacts if c.get("industry") == industry]
    return contacts


@router.get("/{contact_id}")
async def get_contact(
    contact_id: str,
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    contact = sheets.get_contact_by_id(contact_id)
    if contact is None:
        raise HTTPException(status_code=404, detail="Contact not found")
    return contact


@router.post("", status_code=201)
async def create_contact(
    contact: ContactCreate,
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    data = contact.model_dump()
    if data.get("next_follow_up"):
        data["next_follow_up"] = data["next_follow_up"].isoformat()
    if data.get("deal_stage"):
        data["deal_stage"] = data["deal_stage"].value
    return sheets.create_contact(data)


@router.put("/{contact_id}")
async def update_contact(
    contact_id: str,
    contact: ContactUpdate,
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    data = contact.model_dump(exclude_unset=True)
    if "next_follow_up" in data and data["next_follow_up"]:
        data["next_follow_up"] = data["next_follow_up"].isoformat()
    if "deal_stage" in data and data["deal_stage"]:
        data["deal_stage"] = data["deal_stage"].value
    try:
        return sheets.update_contact(contact_id, data)
    except ValueError:
        raise HTTPException(status_code=404, detail="Contact not found")


@router.delete("/{contact_id}", status_code=204)
async def delete_contact(
    contact_id: str,
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    try:
        sheets.delete_contact(contact_id)
    except ValueError:
        raise HTTPException(status_code=404, detail="Contact not found")
