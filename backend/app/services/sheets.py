import json
import uuid
from datetime import UTC, datetime

import gspread

from app.config import settings

CONTACT_HEADERS = [
    "id", "name", "phone", "business", "city", "industry",
    "deal_stage", "last_called", "call_count", "last_call_summary",
    "recording_link", "next_follow_up", "notes", "created_at",
]

CALL_LOG_HEADERS = [
    "id", "contact_id", "timestamp", "duration_seconds",
    "disposition", "summary", "deal_stage", "recording_url", "transcript",
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
            if key == "call_count":
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
        records = self._contacts_ws.get_all_records()
        return [self._normalize_contact(r) for r in records]

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
        now = datetime.now(UTC).isoformat()
        row = [
            contact_id,
            data["name"],
            data["phone"],
            data.get("business", ""),
            data.get("city", ""),
            data.get("industry", ""),
            data.get("deal_stage", "New"),
            "",   # last_called
            0,    # call_count
            "",   # last_call_summary
            "",   # recording_link
            data.get("next_follow_up", ""),
            data.get("notes", ""),
            now,  # created_at
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

        return self.get_contact_by_id(contact_id)

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
            now,
            data["duration_seconds"],
            data["disposition"],
            data.get("summary", ""),
            data.get("deal_stage", ""),
            data.get("recording_url", ""),
            data.get("transcript", ""),
        ]
        self._call_logs_ws.append_row(row)
        return self._normalize_call_log(dict(zip(CALL_LOG_HEADERS, row)))

    def get_call_logs_for_contact(self, contact_id: str) -> list[dict]:
        records = self._call_logs_ws.get_all_records()
        return [
            self._normalize_call_log(r) for r in records
            if r.get("contact_id") == contact_id
        ]

    def get_call_logs_by_date(self, date_str: str) -> list[dict]:
        records = self._call_logs_ws.get_all_records()
        return [
            self._normalize_call_log(r) for r in records
            if str(r.get("timestamp", "")).startswith(date_str)
        ]
