from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    google_client_id: str = ""
    google_service_account_json: str = ""
    spreadsheet_id: str = ""
    groq_api_key: str = ""
    supabase_url: str = ""
    supabase_key: str = ""
    api_key: str = ""

    model_config = {"env_file": ".env"}


settings = Settings()
