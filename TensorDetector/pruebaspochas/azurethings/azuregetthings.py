import azure.storage.common
from azure.storage.common import CloudStorageAccount

STORAGE_ACCOUNT_NAME = 'ficherosmaquinistas'
STORAGE_ACCOUNT_KEY  = 'JKGDYu80C4HWg6DxUyA8mWYouPVAHV9tlB8MO6Xcv5sFKR7KVr+Onw7PLwP7KjMqhdPKTCWFk59NM4m+t/lcGQ=='

account = CloudStorageAccount(STORAGE_ACCOUNT_NAME,STORAGE_ACCOUNT_KEY)

file_service = account.create_file_service()

files = list(file_service.list_directories_and_files('shareficherosmaquinistas'))
for file in files:
    print(file.name)
    ff = file_service.get_file_to_text('shareficherosmaquinistas',None,file.name)
    print(ff.name)
    print(ff.content)
