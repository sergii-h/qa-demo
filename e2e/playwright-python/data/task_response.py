from dataclasses import dataclass, asdict


@dataclass
class TaskResponse:
    id: str
    title: str
    description: str
    status: str
    priority: str

    def to_dict(self) -> dict:
        return asdict(self)
