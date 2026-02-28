from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import call_plan, calls, contacts, dashboard, recordings

app = FastAPI(title="AICC Backend", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(contacts.router)
app.include_router(call_plan.router)
app.include_router(calls.router)
app.include_router(recordings.router)
app.include_router(dashboard.router)


@app.get("/health")
async def health():
    return {"status": "ok"}
