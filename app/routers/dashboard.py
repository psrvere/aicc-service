from datetime import date, timedelta

from fastapi import APIRouter, Depends

from app.auth import get_current_user
from app.services.sheets import SheetsService, get_sheets_service

router = APIRouter(prefix="/api/dashboard", tags=["dashboard"])


@router.get("/stats")
async def get_stats(
    sheets: SheetsService = Depends(get_sheets_service),
    user: dict = Depends(get_current_user),
):
    today = date.today()
    today_str = today.isoformat()

    # Today's call logs
    todays_logs = sheets.get_call_logs_by_date(today_str)
    calls_today = len(todays_logs)
    connected_today = sum(
        1 for log in todays_logs if log.get("disposition") == "Connected"
    )
    conversion_rate = connected_today / calls_today if calls_today > 0 else 0.0

    # Streak: consecutive days with >= 1 call, ending today
    streak = 0
    check_date = today
    while True:
        logs = sheets.get_call_logs_by_date(check_date.isoformat())
        if not logs:
            break
        streak += 1
        check_date -= timedelta(days=1)

    # Pipeline: contact count per deal stage
    contacts = sheets.get_all_contacts()
    pipeline: dict[str, int] = {}
    for c in contacts:
        stage = c.get("deal_stage")
        if stage:
            pipeline[stage] = pipeline.get(stage, 0) + 1

    return {
        "calls_today": calls_today,
        "connected_today": connected_today,
        "conversion_rate": conversion_rate,
        "streak": streak,
        "pipeline": pipeline,
    }
