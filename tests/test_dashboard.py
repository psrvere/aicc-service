from datetime import date


def _make_call_log(disposition="Connected", timestamp=None):
    return {
        "id": "log-1", "contact_id": "uuid-1",
        "contact_name": None, "timestamp": timestamp or f"{date.today().isoformat()}T10:00:00",
        "duration_seconds": 60, "disposition": disposition,
        "summary": None, "recording_url": None,
        "deal_stage": "New", "deal_stage_after": None,
    }


def _make_contact(deal_stage="New"):
    return {
        "id": "uuid-1", "name": "Alice", "contact_person": None,
        "phone": "123", "city": None, "industry": None,
        "source": None, "deal_stage": deal_stage, "last_called": None,
        "next_follow_up": None, "call_count": 0,
        "last_call_summary": None, "recording_link": None,
        "notes": None,
    }


def _today_only_side_effect(logs):
    """Return a side_effect that returns logs only for today's date."""
    today = date.today().isoformat()

    def mock_logs_by_date(date_str):
        if date_str == today:
            return logs
        return []

    return mock_logs_by_date


def test_calls_today_count(client, mock_sheets):
    today = date.today().isoformat()
    logs = [
        _make_call_log(timestamp=f"{today}T09:00:00"),
        _make_call_log(disposition="NoAnswer", timestamp=f"{today}T10:00:00"),
        _make_call_log(timestamp=f"{today}T11:00:00"),
    ]
    mock_sheets.get_call_logs_by_date.side_effect = _today_only_side_effect(logs)
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    assert response.status_code == 200
    assert response.json()["calls_today"] == 3


def test_connected_today_count(client, mock_sheets):
    today = date.today().isoformat()
    logs = [
        _make_call_log(disposition="Connected", timestamp=f"{today}T09:00:00"),
        _make_call_log(disposition="NoAnswer", timestamp=f"{today}T10:00:00"),
        _make_call_log(disposition="Connected", timestamp=f"{today}T11:00:00"),
    ]
    mock_sheets.get_call_logs_by_date.side_effect = _today_only_side_effect(logs)
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    assert response.json()["connected_today"] == 2


def test_conversion_rate(client, mock_sheets):
    today = date.today().isoformat()
    logs = [
        _make_call_log(disposition="Connected", timestamp=f"{today}T09:00:00"),
        _make_call_log(disposition="NoAnswer", timestamp=f"{today}T10:00:00"),
        _make_call_log(disposition="Connected", timestamp=f"{today}T11:00:00"),
        _make_call_log(disposition="Voicemail", timestamp=f"{today}T12:00:00"),
    ]
    mock_sheets.get_call_logs_by_date.side_effect = _today_only_side_effect(logs)
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    assert response.json()["conversion_rate"] == 0.5


def test_conversion_rate_division_by_zero(client, mock_sheets):
    mock_sheets.get_call_logs_by_date.return_value = []
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    assert response.json()["conversion_rate"] == 0.0


def test_streak_consecutive_days(client, mock_sheets):
    from datetime import timedelta

    today = date.today()
    all_logs = []
    # Build 3 consecutive days of calls (today, yesterday, day before)
    for i in range(3):
        d = (today - timedelta(days=i)).isoformat()
        all_logs.append(_make_call_log(timestamp=f"{d}T10:00:00"))

    def mock_logs_by_date(date_str):
        return [l for l in all_logs if l["timestamp"].startswith(date_str)]

    mock_sheets.get_call_logs_by_date.side_effect = mock_logs_by_date
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    assert response.json()["streak"] == 3


def test_streak_broken(client, mock_sheets):
    from datetime import timedelta

    today = date.today()
    # Today has calls, yesterday doesn't
    all_logs = [_make_call_log(timestamp=f"{today.isoformat()}T10:00:00")]

    def mock_logs_by_date(date_str):
        return [l for l in all_logs if l["timestamp"].startswith(date_str)]

    mock_sheets.get_call_logs_by_date.side_effect = mock_logs_by_date
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    assert response.json()["streak"] == 1


def test_pipeline_distribution(client, mock_sheets):
    mock_sheets.get_call_logs_by_date.return_value = []
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(deal_stage="New"),
        _make_contact(deal_stage="New"),
        _make_contact(deal_stage="Qualified"),
        _make_contact(deal_stage="Won"),
    ]

    response = client.get("/api/dashboard/stats")
    pipeline = response.json()["pipeline"]
    assert pipeline["New"] == 2
    assert pipeline["Qualified"] == 1
    assert pipeline["Won"] == 1


def test_empty_data(client, mock_sheets):
    mock_sheets.get_call_logs_by_date.return_value = []
    mock_sheets.get_all_contacts.return_value = []

    response = client.get("/api/dashboard/stats")
    data = response.json()
    assert data["calls_today"] == 0
    assert data["connected_today"] == 0
    assert data["conversion_rate"] == 0.0
    assert data["streak"] == 0
    assert data["pipeline"] == {}
