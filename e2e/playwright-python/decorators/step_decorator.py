import functools
from enum import Enum

import allure


def step(step_name: str):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            formatted = _format_step_name(step_name, args[1:], kwargs)
            with allure.step(formatted):
                return func(*args, **kwargs)
        return wrapper
    return decorator


def _format_step_name(template: str, args: tuple, kwargs: dict) -> str:
    result = template
    for value in args:
        placeholder_start = result.find("{")
        if placeholder_start == -1:
            break
        placeholder_end = result.find("}", placeholder_start)
        if placeholder_end == -1:
            break
        result = result[:placeholder_start] + _format_value(value) + result[placeholder_end + 1:]
    return result


def _format_value(value) -> str:
    if isinstance(value, Enum):
        return str(value.value)
    if isinstance(value, list):
        return f"[{', '.join(_format_value(v) for v in value)}]"
    if isinstance(value, dict):
        entries = ", ".join(f"{k}: {_format_value(v)}" for k, v in value.items())
        return f"{{{entries}}}"
    if hasattr(value, "__dataclass_fields__"):
        entries = ", ".join(
            f"{k}: {_format_value(getattr(value, k))}" for k in value.__dataclass_fields__
        )
        return f"{{{entries}}}"
    return str(value)
