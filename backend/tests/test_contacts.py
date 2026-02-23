def _make_contact(**overrides):
    contact = {
        "id": "uuid-1", "name": "Alice", "phone": "123",
        "business": None, "city": None, "industry": None,
        "deal_stage": "New", "last_called": None, "call_count": 0,
        "last_call_summary": None, "recording_link": None,
        "next_follow_up": None, "notes": None,
        "created_at": "2026-01-01T00:00:00",
    }
    contact.update(overrides)
    return contact


def test_list_contacts_returns_data(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(),
        _make_contact(id="uuid-2", name="Bob"),
    ]
    response = client.get("/api/contacts")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert data[0]["name"] == "Alice"


def test_list_contacts_empty(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = []
    response = client.get("/api/contacts")
    assert response.status_code == 200
    assert response.json() == []


def test_list_contacts_filter_by_deal_stage(client, mock_sheets):
    mock_sheets.get_all_contacts.return_value = [
        _make_contact(id="uuid-1", deal_stage="New"),
        _make_contact(id="uuid-2", deal_stage="Won"),
        _make_contact(id="uuid-3", deal_stage="New"),
    ]
    response = client.get("/api/contacts?deal_stage=New")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert all(c["deal_stage"] == "New" for c in data)


def test_get_contact_found(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = _make_contact()
    response = client.get("/api/contacts/uuid-1")
    assert response.status_code == 200
    assert response.json()["name"] == "Alice"


def test_get_contact_not_found(client, mock_sheets):
    mock_sheets.get_contact_by_id.return_value = None
    response = client.get("/api/contacts/nonexistent")
    assert response.status_code == 404


def test_create_contact(client, mock_sheets):
    mock_sheets.create_contact.return_value = _make_contact(name="Carol", phone="789")
    response = client.post("/api/contacts", json={"name": "Carol", "phone": "789"})
    assert response.status_code == 201
    assert response.json()["name"] == "Carol"
    mock_sheets.create_contact.assert_called_once()


def test_create_contact_missing_required_field(client, mock_sheets):
    response = client.post("/api/contacts", json={"name": "Carol"})
    assert response.status_code == 422


def test_update_contact(client, mock_sheets):
    mock_sheets.update_contact.return_value = _make_contact(name="Alice Updated")
    response = client.put("/api/contacts/uuid-1", json={"name": "Alice Updated"})
    assert response.status_code == 200
    assert response.json()["name"] == "Alice Updated"


def test_update_contact_not_found(client, mock_sheets):
    mock_sheets.update_contact.side_effect = ValueError("Contact nonexistent not found")
    response = client.put("/api/contacts/nonexistent", json={"name": "X"})
    assert response.status_code == 404


def test_delete_contact(client, mock_sheets):
    response = client.delete("/api/contacts/uuid-1")
    assert response.status_code == 204
    mock_sheets.delete_contact.assert_called_once_with("uuid-1")


def test_delete_contact_not_found(client, mock_sheets):
    mock_sheets.delete_contact.side_effect = ValueError("Contact nonexistent not found")
    response = client.delete("/api/contacts/nonexistent")
    assert response.status_code == 404


def test_auth_required(unauthed_client):
    response = unauthed_client.get("/api/contacts")
    assert response.status_code == 401
