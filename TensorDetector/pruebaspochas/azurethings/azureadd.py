import azure.storage.common
from azure.storage.common import CloudStorageAccount

STORAGE_ACCOUNT_NAME = 'ficherosmaquinistas'
STORAGE_ACCOUNT_KEY  = 'JKGDYu80C4HWg6DxUyA8mWYouPVAHV9tlB8MO6Xcv5sFKR7KVr+Onw7PLwP7KjMqhdPKTCWFk59NM4m+t/lcGQ=='

account = CloudStorageAccount(STORAGE_ACCOUNT_NAME,STORAGE_ACCOUNT_KEY)

file_service = account.create_file_service()

file_service.create_file_from_text(
    "shareficherosmaquinistas",
    None,
    'filename1.txt',
    'contenido1 \n contenido 2\n blalaalalala'
)

file_service.create_file_from_text(
    "shareficherosmaquinistas",
    None,
    'filename2.txt',
    'contenido2'
)


file_service.create_file_from_text(
    "shareficherosmaquinistas",
    None,
    'filename3.txt',
    'contenido4'
)
