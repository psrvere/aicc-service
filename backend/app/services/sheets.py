import json
import uuid
from datetime import UTC, datetime

import gspread

from app.config import settings

# Must match the actual Google Sheet column order exactly.
CONTACT_HEADERS = [
    "id", "name", "contact_person", "phone", "city", "industry",
    "source", "deal_stage", "last_called", "next_follow_up", "call_count",
    "last_call_summary", "recording_link", "notes",
]

CALL_LOG_HEADERS = [
    "id", "contact_id", "contact_name", "timestamp", "duration_seconds",
    "disposition", "summary", "recording_url", "deal_stage", "deal_stage_after",
]


def get_sheets_service() -> "SheetsService":
    return SheetsService()


class SheetsService:
    def __init__(self):
        creds = json.loads(settings.google_service_account_json)
        client = gspread.service_account_from_dict(creds)
        spreadsheet = client.open_by_key(settings.spreadsheet_id)
        self._contacts_ws = spreadsheet.worksheet("Contacts")
        self._call_logs_ws = spreadsheet.worksheet("CallLogs")

    @staticmethod
    def _normalize_contact(record: dict) -> dict:
        result = {}
        for key in CONTACT_HEADERS:
            val = record.get(key, "")
            if key == "id":
                result[key] = str(val) if val != "" else None
            elif key == "call_count":
                result[key] = int(val) if val != "" else 0
            elif val == "":
                result[key] = None
            else:
                result[key] = val
        return result

    @staticmethod
    def _normalize_call_log(record: dict) -> dict:
        result = {}
        for key in CALL_LOG_HEADERS:
            val = record.get(key, "")
            if key == "duration_seconds":
                result[key] = int(val) if val != "" else 0
            elif val == "":
                result[key] = None
            else:
                result[key] = val
        return result

    def get_all_contacts(self) -> list[dict]:
        rows = self._contacts_ws.get_all_values()
        if len(rows) <= 1:
            return []
        return [
            self._normalize_contact(dict(zip(CONTACT_HEADERS, row)))
            for row in rows[1:]
        ]

    def get_contact_by_id(self, contact_id: str) -> dict | None:
        for c in self.get_all_contacts():
            if c["id"] == contact_id:
                return c
        return None

    def _find_contact_row(self, contact_id: str) -> int | None:
        values = self._contacts_ws.get_all_values()
        for i, row in enumerate(values):
            if i == 0:
                continue
            if row[0] == contact_id:
                return i + 1  # gspread rows are 1-indexed
        return None

    def create_contact(self, data: dict) -> dict:
        contact_id = str(uuid.uuid4())
        row = [
            contact_id,
            data["name"],
            data.get("contact_person", ""),
            data["phone"],
            data.get("city", ""),
            data.get("industry", ""),
            data.get("source", ""),
            data.get("deal_stage", "New"),
            "",   # last_called
            data.get("next_follow_up", ""),
            0,    # call_count
            "",   # last_call_summary
            "",   # recording_link
            data.get("notes", ""),
        ]
        self._contacts_ws.append_row(row)
        return self._normalize_contact(dict(zip(CONTACT_HEADERS, row)))

    def update_contact(self, contact_id: str, data: dict) -> dict:
        row_num = self._find_contact_row(contact_id)
        if row_num is None:
            raise ValueError(f"Contact {contact_id} not found")

        for key, value in data.items():
            if key in CONTACT_HEADERS and value is not None:
                col = CONTACT_HEADERS.index(key) + 1
                self._contacts_ws.update_cell(row_num, col, value)

        row = self._contacts_ws.row_values(row_num)
        return self._normalize_contact(dict(zip(CONTACT_HEADERS, row)))

    def delete_contact(self, contact_id: str) -> None:
        row_num = self._find_contact_row(contact_id)
        if row_num is None:
            raise ValueError(f"Contact {contact_id} not found")
        self._contacts_ws.delete_rows(row_num)

    def append_call_log(self, data: dict) -> dict:
        log_id = str(uuid.uuid4())
        now = datetime.now(UTC).isoformat()
        row = [
            log_id,
            data["contact_id"],
            data.get("contact_name") or "",
            now,
            data["duration_seconds"],
            data["disposition"],
            data.get("summary") or "",
            data.get("recording_url") or "",
            data.get("deal_stage") or "",
            data.get("deal_stage_after") or "",
        ]
        self._call_logs_ws.append_row(row)
        return self._normalize_call_log(dict(zip(CALL_LOG_HEADERS, row)))

    def get_call_logs_for_contact(self, contact_id: str) -> list[dict]:
        rows = self._call_logs_ws.get_all_values()
        if len(rows) <= 1:
            return []
        return [
            self._normalize_call_log(dict(zip(CALL_LOG_HEADERS, row)))
            for row in rows[1:]
            if row[1] == contact_id
        ]

    def get_call_logs_by_date(self, date_str: str) -> list[dict]:
        rows = self._call_logs_ws.get_all_values()
        if len(rows) <= 1:
            return []
        return [
            self._normalize_call_log(dict(zip(CALL_LOG_HEADERS, row)))
            for row in rows[1:]
            if row[3].startswith(date_str)
        ]
