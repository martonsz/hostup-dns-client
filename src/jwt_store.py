import logging
import time
from model.authentication import Authentication

from pathlib import Path

logger = logging.getLogger(__name__)

EXPIRE_THRESHOLD = 300

def save_jwt(authentication: Authentication, file_path: Path) -> None:
    if file_path.exists():
        file_path.unlink()
    with open(file_path, "w") as outfile:
        outfile.write(authentication.model_dump_json())
    logging.debug(f"JWT saved to file {file_path}. Expires in {authentication._time_until_epoch()}")


def load_jwt(file_path: Path) -> Authentication:
    if not file_path.exists():
        return None

    with open(file_path, "r") as f:
        auth = Authentication.model_validate_json(f.read())
        logging.debug(f"JWT loaded from file {file_path}. Expires in {auth._time_until_epoch()}")
        if (time_left := auth.seconds_until_expiration()) >= EXPIRE_THRESHOLD:
            return auth
        else:
            logging.debug(f"Cached JWT expires in {time_left}. Deleting JWT file {file_path}")
            file_path.unlink()
            return None
