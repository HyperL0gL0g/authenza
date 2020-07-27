import uuid


def getUUID():
    return str(uuid.uuid4()).replace("-", "")


def genKey():
    file = open("keys.txt", "w")
    secret = getUUID()
    file.write("SECRET: {}".format(secret))
    file.close()


if __name__ == '__main__':
    genKey()
