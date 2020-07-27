from flask import Flask, request, jsonify, url_for, copy_current_request_context
from util.init_db import initDB
import psycopg2.errors
import psycopg2
import uuid
import jwt
import time
import pyqrcode
import os
from werkzeug.utils import secure_filename
import threading
from app import app
from flask_bcrypt import Bcrypt
from flask_mail import Mail, Message

#--- GLOBAL--#
bcrypt = Bcrypt(app)
mail = Mail(app)
_secret = ""
_conn = None
_cur = None
baseAddr = "" # change if executing from different dir
ext = {'png', 'jpg', 'jpeg'}

#---UTIL---#

def conn():
    return _conn

def cur():
    return _cur

def secret():
    return _secret

def sendMail(sender, subject, body, recipients):
    msg = Message(subject, sender=sender +
                   f" <{os.getenv('EMAIL')}>", recipients=recipients)
    msg.body = body
    mail.send(msg)


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ext


def init():
    global _secret, _conn, _cur
    file = open(baseAddr+"keys.txt", "r")
    data = file.readlines()
    _secret = data[0].split()[1]
    _conn=initDB()
    if _conn is None:
        print("\nXX SERVER STARTUP FAILED - RESTART SERVER MANUALLY XX")
        return None
    _cur = _conn.cursor()
    return (_conn,_cur,_secret)

def setInit(co,cu,se):
    global _secret, _conn, _cur
    _conn=co
    _cur=cu
    _secret=se

def getUUID():
    return str(uuid.uuid4()).replace("-", "")


def token_verify(token, tim):
    try:
        res = jwt.decode(token, _secret)
        if "complete" in res:
            if tim == 70 and (time.time()-res["issue_time"]) <= tim:
                return res
            if not res["complete"]:
                return False
        if tim == -1:
            return res
        elif (time.time()-res["issue_time"]) <= tim:
            return res
        else:
            False
    except:
        return False


def org_api_verifiy(id, key):
    _cur.execute("select * from org_api where org_id=%s and api_key=%s", (id, key))
    res = _cur.fetchall()
    if len(res) == 0:
        return False
    return res[0]

#---UTIL END---#