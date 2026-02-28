from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.auth import get_current_user


def _make_app():
    """Create a minimal app with a protected endpoint for auth testing."""
    test_app = FastAPI()

    @test_app.get("/protected")
    async def protected(user: dict = __import__("fastapi").Depends(get_current_user)):
        return {"email": user.get("email")}

    return test_app


def test_missing_auth_header_returns_401():
    app = _make_app()
    client = TestClient(app)
    response = client.get("/protected")
    assert response.status_code == 401


def test_invalid_token_returns_401():
    app = _make_app()
    client = TestClient(app)
    response = client.get("/protected", headers={"Authorization": "Bearer invalid-token"})
    assert response.status_code == 401


def test_valid_auth_override_returns_user():
    app = _make_app()
    mock_user = {"email": "test@example.com", "sub": "12345"}
    app.dependency_overrides[get_current_user] = lambda: mock_user
    client = TestClient(app)
    response = client.get("/protected")
    assert response.status_code == 200
    assert response.json() == {"email": "test@example.com"}
    app.dependency_overrides.clear()
