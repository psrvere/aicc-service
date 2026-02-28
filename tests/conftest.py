from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

from app.auth import get_current_user
from app.main import app
from app.services.sheets import get_sheets_service


@pytest.fixture
def mock_sheets():
    return MagicMock()


@pytest.fixture
def client(mock_sheets):
    app.dependency_overrides[get_current_user] = lambda: {
        "email": "test@example.com",
        "sub": "12345",
    }
    app.dependency_overrides[get_sheets_service] = lambda: mock_sheets
    yield TestClient(app)
    app.dependency_overrides.clear()


@pytest.fixture
def unauthed_client():
    app.dependency_overrides[get_sheets_service] = lambda: MagicMock()
    yield TestClient(app)
    app.dependency_overrides.clear()
