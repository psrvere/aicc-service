from unittest.mock import MagicMock, patch

import pytest

CONTACT_HEADERS = [
    "id", "name", "contact_person", "phone", "city", "industry",
    "source", "deal_stage", "last_called", "next_follow_up", "call_count",
    "last_call_summary", "recording_link", "notes",
]

CALL_LOG_HEADERS = [
    "id", "contact_id", "contact_name", "timestamp", "duration_seconds",
    "disposition", "summary", "recording_url", "deal_stage", "deal_stage_after",
]


def _make_contact_row(**overrides):
    defaults = dict(zip(CONTACT_HEADERS, [
        "uuid-1", "Alice", "", "123", "", "",
        "", "New", "", "", "0", "", "", "",
    ]))
    defaults.update(overrides)
    return [str(defaults[h]) for h in CONTACT_HEADERS]


def _make_call_log_row(**overrides):
    defaults = dict(zip(CALL_LOG_HEADERS, [
        "log-1", "uuid-1", "", "2026-02-23T10:00:00", "120",
        "Connected", "", "", "New", "",
    ]))
    defaults.update(overrides)
    return [str(defaults[h]) for h in CALL_LOG_HEADERS]


@pytest.fixture
def sheets_service():
    with patch("app.services.sheets.gspread") as mock_gspread, \
         patch("app.services.sheets.settings") as mock_settings:
        mock_settings.google_service_account_json = '{"type": "service_account"}'
        mock_settings.spreadsheet_id = "test-sheet-id"

        mock_client = MagicMock()
        mock_gspread.service_account_from_dict.return_value = mock_client
        mock_spreadsheet = MagicMock()
        mock_client.open_by_key.return_value = mock_spreadsheet

        contacts_ws = MagicMock()
        call_logs_ws = MagicMock()
        mock_spreadsheet.worksheet.side_effect = lambda name: {
            "Contacts": contacts_ws,
            "CallLogs": call_logs_ws,
        }[name]

        from app.services.sheets import SheetsService
        service = SheetsService()

        yield service, contacts_ws, call_logs_ws


def test_get_all_contacts(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [
        CONTACT_HEADERS,
        _make_contact_row(call_count="5", source="IndiaMart"),
    ]
    result = service.get_all_contacts()
    assert len(result) == 1
    assert result[0]["name"] == "Alice"
    assert result[0]["call_count"] == 5
    assert result[0]["source"] == "IndiaMart"
    assert result[0]["last_called"] is None  # empty string → None


def test_get_all_contacts_empty(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [CONTACT_HEADERS]
    result = service.get_all_contacts()
    assert result == []


def test_get_contact_by_id_found(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [
        CONTACT_HEADERS,
        _make_contact_row(id="uuid-1"),
        _make_contact_row(id="uuid-2", name="Bob"),
    ]
    result = service.get_contact_by_id("uuid-2")
    assert result is not None
    assert result["name"] == "Bob"


def test_get_contact_by_id_not_found(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [
        CONTACT_HEADERS,
        _make_contact_row(id="uuid-1"),
    ]
    result = service.get_contact_by_id("nonexistent")
    assert result is None


def test_create_contact(sheets_service):
    service, contacts_ws, _ = sheets_service
    data = {"name": "Carol", "phone": "789", "contact_person": "Carol Smith"}
    result = service.create_contact(data)

    contacts_ws.append_row.assert_called_once()
    row = contacts_ws.append_row.call_args[0][0]
    assert row[1] == "Carol"        # name
    assert row[2] == "Carol Smith"  # contact_person
    assert row[3] == "789"          # phone
    assert row[10] == 0             # call_count default
    assert result["id"] is not None  # UUID generated
    assert result["name"] == "Carol"


def test_create_contact_sets_defaults(sheets_service):
    service, contacts_ws, _ = sheets_service
    data = {"name": "Dan", "phone": "000"}
    result = service.create_contact(data)

    row = contacts_ws.append_row.call_args[0][0]
    assert row[7] == "New"    # deal_stage default
    assert row[10] == 0       # call_count default
    assert result["deal_stage"] == "New"
    assert result["call_count"] == 0


def test_update_contact(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [
        CONTACT_HEADERS,
        _make_contact_row(id="uuid-1"),
    ]
    contacts_ws.row_values.return_value = _make_contact_row(id="uuid-1", name="Alice Updated")
    result = service.update_contact("uuid-1", {"name": "Alice Updated"})
    contacts_ws.update_cell.assert_called_once_with(2, 2, "Alice Updated")
    assert result["name"] == "Alice Updated"


def test_update_contact_not_found(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [CONTACT_HEADERS]
    with pytest.raises(ValueError, match="not found"):
        service.update_contact("nonexistent", {"name": "X"})


def test_delete_contact(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [
        CONTACT_HEADERS,
        _make_contact_row(id="uuid-1"),
    ]
    service.delete_contact("uuid-1")
    contacts_ws.delete_rows.assert_called_once_with(2)


def test_delete_contact_not_found(sheets_service):
    service, contacts_ws, _ = sheets_service
    contacts_ws.get_all_values.return_value = [CONTACT_HEADERS]
    with pytest.raises(ValueError, match="not found"):
        service.delete_contact("nonexistent")


def test_append_call_log(sheets_service):
    service, _, call_logs_ws = sheets_service
    data = {
        "contact_id": "uuid-1",
        "contact_name": "Alice",
        "duration_seconds": 180,
        "disposition": "Connected",
        "summary": "Good call",
        "deal_stage": "Qualified",
    }
    result = service.append_call_log(data)

    call_logs_ws.append_row.assert_called_once()
    row = call_logs_ws.append_row.call_args[0][0]
    assert row[1] == "uuid-1"       # contact_id
    assert row[2] == "Alice"        # contact_name
    assert row[4] == 180            # duration_seconds
    assert row[5] == "Connected"    # disposition
    assert result["id"] is not None  # UUID generated


def test_get_call_logs_for_contact(sheets_service):
    service, _, call_logs_ws = sheets_service
    call_logs_ws.get_all_values.return_value = [
        CALL_LOG_HEADERS,
        _make_call_log_row(contact_id="uuid-1"),
        _make_call_log_row(id="log-2", contact_id="uuid-2"),
        _make_call_log_row(id="log-3", contact_id="uuid-1"),
    ]
    result = service.get_call_logs_for_contact("uuid-1")
    assert len(result) == 2
    assert all(r["contact_id"] == "uuid-1" for r in result)


def test_get_call_logs_by_date(sheets_service):
    service, _, call_logs_ws = sheets_service
    call_logs_ws.get_all_values.return_value = [
        CALL_LOG_HEADERS,
        _make_call_log_row(timestamp="2026-02-23T10:00:00"),
        _make_call_log_row(id="log-2", timestamp="2026-02-22T15:00:00"),
        _make_call_log_row(id="log-3", timestamp="2026-02-23T14:30:00"),
    ]
    result = service.get_call_logs_by_date("2026-02-23")
    assert len(result) == 2
