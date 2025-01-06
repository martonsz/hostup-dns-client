import logging
import time
from model.authentication import Authentication

from pathlib import Path

logger = logging.getLogger(__name__)

EXPIRE_THRESHOLD = 300
global_authentication = None

def save_jwt(authentication: Authentication, file_path: Path) -> None:
    global global_authentication

    if file_path.exists():
        file_path.unlink()
    with open(file_path, "w") as outfile:
        outfile.write(authentication.model_dump_json())
    global_authentication = authentication
    logging.debug(f"JWT saved to file {file_path}. Expires in {authentication._time_until_epoch()}")


def load_jwt(file_path: Path) -> Authentication:
    global global_authentication

    if global_authentication and global_authentication.seconds_until_expiration() >= EXPIRE_THRESHOLD:
        return global_authentication

    if not file_path.exists():
        return None

    with open(file_path, "r") as f:
        auth = Authentication.model_validate_json(f.read())
        logging.debug(f"JWT loaded from file {file_path}. Expires in {auth._time_until_epoch()}")
        if (time_left := auth.seconds_until_expiration()) >= EXPIRE_THRESHOLD:
            global_authentication = auth
            return auth
        else:
            logging.debug(f"Cached JWT expires in {time_left}. Deleting JWT file {file_path}")
            file_path.unlink()
            global_authentication = None
            return None

def delete_jwt(file_path: Path) -> None:
    global global_authentication
    global_authentication = None
    if file_path.exists():
        file_path.unlink()
        logging.debug(f"Deleted JWT file {file_path}")
    else:
        logging.debug(f"JWT file {file_path} not found. Skipping delete")
