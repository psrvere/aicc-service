from datetime import date, timedelta


def _make_contact(**overrides):
    contact = {
        "id": "uuid-1", "name": "Alice", "contact_person": None,
        "phone": "123", "city": None, "industry": None,
        "source": None, "deal_stage": "New", "last_called": None,
        "next_follow_up": None, "call_count": 0,
        "last_call_summary": None, "recording_link": None,
        "notes": None,
    }
    contact.update(overrides)
    return contact


def test_log_call_creates_call_log(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 60,
        "disposition": "Connected", "summary": None,
        "deal_stage": "New", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact(call_count=1)

    response = client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 60,
        "disposition": "Connected",
    })
    assert response.status_code == 201
    mock_sheets.append_call_log.assert_called_once()


def test_log_call_updates_contact(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact(call_count=2)
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 60,
        "disposition": "Connected", "summary": None,
        "deal_stage": "New", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact(call_count=3)

    client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 60,
        "disposition": "Connected",
    })
    mock_sheets.update_contact.assert_called_once()
    update_data = mock_sheets.update_contact.call_args[0][1]
    assert update_data["call_count"] == 3


def test_log_call_contact_not_found(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = None
    response = client.post("/api/calls/log", json={
        "contact_id": "nonexistent",
        "duration_seconds": 60,
        "disposition": "Connected",
    })
    assert response.status_code == 404


def test_callback_sets_follow_up_tomorrow(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 30,
        "disposition": "Callback", "summary": None,
        "deal_stage": "New", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact()

    client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 30,
        "disposition": "Callback",
    })
    update_data = mock_sheets.update_contact.call_args[0][1]
    expected = (date.today() + timedelta(days=1)).isoformat()
    assert update_data["next_follow_up"] == expected


def test_no_answer_sets_follow_up_3_days(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 0,
        "disposition": "NoAnswer", "summary": None,
        "deal_stage": "New", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact()

    client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 0,
        "disposition": "NoAnswer",
    })
    update_data = mock_sheets.update_contact.call_args[0][1]
    expected = (date.today() + timedelta(days=3)).isoformat()
    assert update_data["next_follow_up"] == expected


def test_voicemail_sets_follow_up_7_days(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 15,
        "disposition": "Voicemail", "summary": None,
        "deal_stage": "New", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact()

    client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 15,
        "disposition": "Voicemail",
    })
    update_data = mock_sheets.update_contact.call_args[0][1]
    expected = (date.today() + timedelta(days=7)).isoformat()
    assert update_data["next_follow_up"] == expected


def test_not_interested_clears_follow_up(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact(
        next_follow_up="2026-03-01",
    )
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 10,
        "disposition": "NotInterested", "summary": None,
        "deal_stage": "NotInterested", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact()

    client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 10,
        "disposition": "NotInterested",
    })
    update_data = mock_sheets.update_contact.call_args[0][1]
    assert update_data["next_follow_up"] == ""


def test_log_call_update_contact_not_found(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 60,
        "disposition": "Connected", "summary": None,
        "deal_stage": "New", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.side_effect = ValueError("Contact uuid-1 not found")

    response = client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 60,
        "disposition": "Connected",
    })
    assert response.status_code == 404


def test_connected_uses_request_follow_up(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    mock_sheets.append_call_log.return_value = {
        "id": "log-1", "contact_id": "uuid-1",
        "timestamp": "2026-02-23T10:00:00", "duration_seconds": 120,
        "disposition": "Connected", "summary": None,
        "deal_stage": "Qualified", "recording_url": None, "deal_stage_after": None,
    }
    mock_sheets.update_contact.return_value = _make_contact()

    client.post("/api/calls/log", json={
        "contact_id": "uuid-1",
        "duration_seconds": 120,
        "disposition": "Connected",
        "next_follow_up": "2026-03-15",
    })
    update_data = mock_sheets.update_contact.call_args[0][1]
    assert update_data["next_follow_up"] == "2026-03-15"
