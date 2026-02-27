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


def test_call_plan_includes_new_uncalled(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="uuid-1", deal_stage="New", call_count=0),
    ]
    response = client.get("/api/callplan/today")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["id"] == "uuid-1"
    assert data[0]["reason"] == "New contact"


def test_call_plan_includes_follow_up_due(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(
            id="uuid-2", deal_stage="Contacted", call_count=3,
            next_follow_up="2026-02-22",
        ),
    ]
    response = client.get("/api/callplan/today")
    data = response.json()
    assert len(data) == 1
    assert data[0]["reason"] == "Follow-up due"


def test_call_plan_excludes_won(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="uuid-1", deal_stage="Won", call_count=0),
    ]
    response = client.get("/api/callplan/today")
    assert response.json() == []


def test_call_plan_excludes_lost(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="uuid-1", deal_stage="Lost", call_count=0),
    ]
    response = client.get("/api/callplan/today")
    assert response.json() == []


def test_call_plan_excludes_not_interested(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="uuid-1", deal_stage="NotInterested", call_count=0),
    ]
    response = client.get("/api/callplan/today")
    assert response.json() == []


def test_call_plan_excludes_new_already_called(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="uuid-1", deal_stage="New", call_count=2),
    ]
    response = client.get("/api/callplan/today")
    assert response.json() == []


def test_call_plan_excludes_future_follow_up(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(
            id="uuid-1", deal_stage="Contacted", call_count=1,
            next_follow_up="2099-12-31",
        ),
    ]
    response = client.get("/api/callplan/today")
    assert response.json() == []


def test_call_plan_sorts_follow_ups_before_new(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="new-1", deal_stage="New", call_count=0),
        _make_contact(
            id="follow-1", deal_stage="Contacted", call_count=2,
            next_follow_up="2026-02-20",
        ),
    ]
    response = client.get("/api/callplan/today")
    data = response.json()
    assert len(data) == 2
    assert data[0]["id"] == "follow-1"
    assert data[1]["id"] == "new-1"
