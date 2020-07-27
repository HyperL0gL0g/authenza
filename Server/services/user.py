from util.util import *
from flask import Blueprint

user_bp = Blueprint('user_bp', __name__)


@user_bp.route('/home')
def home():
    return getUUID()


@user_bp.route('/register', methods=["POST"])
def user_register():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        name = request.json["name"]
        email = request.json["email"]
        password = request.json["password"]
        res = org_api_verifiy(client_id, key)
        if res:
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, email))
            res = cur().fetchall()
            if len(res) != 0:
                return jsonify(error="Email Already Registered"), 400
            u_id = getUUID()
            passd = bcrypt.generate_password_hash(password).decode('utf-8')
            cur().execute("insert into users values(%s,%s,%s,%s,%s,%s)",
                          (client_id, u_id, name, email, passd, "false"))
            conn().commit()
            return jsonify(message="USER REGISTERED", email_verify="true"), 200
        else:
            return jsonify(error="unauthorized access"), 401

    except psycopg2.errors.UniqueViolation as e:
        return jsonify(error="Email Already Registered"), 400
    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/login', methods=["POST"])
def user_login():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        email = request.json["email"]
        password = request.json["password"]
        res = org_api_verifiy(client_id, key)
        if res:
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, email))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="incorrect credentials"), 401
            res = res[0]
            if bcrypt.check_password_hash(res[4], password):
                # print(res)
                if not res[6]:
                    return jsonify(error="EMAIL NOT VERIFIED", email=email), 403
                if res[5]:
                    cur().execute(
                        "select * from user_2_factor where org_id=%s and u_id=%s", (client_id, res[1]))
                    res1 = cur().fetchall()
                    if len(res1) != 0:
                        res1 = res1[0]
                        if float(time.time())-float(res1[4]) > 60:
                            cur().execute(
                                "delete from user_2_factor where org_id=%s and u_id=%s", (client_id, res[1]))
                            conn().commit()
                        else:
                            return jsonify(error="2FA has been already initialized please complete it first or wait for it to expire"), 400
                    tim = time.time()
                    cur().execute("insert into user_2_factor values(%s,%s,%s,%s,%s)",
                                  (client_id, res[1], "LOG", "", tim))
                    conn().commit()
                    jwt_token = jwt.encode({"id": res[1], "name": res[2], "email": res[3],
                                            "issue_time": tim, "complete": False}, secret(), algorithm="HS256").decode('utf-8')
                    return jsonify(token=jwt_token, two_factor=True, valid_period="60"), 200
                else:
                    jwt_token = jwt.encode({"id": res[1], "name": res[2], "email": res[3], "issue_time": time.time(
                    ), "complete": True}, secret(), algorithm="HS256").decode('utf-8')
                    return jsonify(token=jwt_token, valid_period="3600"), 200
            else:
                return jsonify(error="incorrect credentials"), 401
        else:
            return jsonify(error="unauthorized access"), 401

    except psycopg2.errors.UniqueViolation as e:
        return jsonify(error="2FA has been already initialized please complete it first or wait for it to expire"), 400
    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/init_email_verify', methods=["POST"])
def init_email_verify():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        email = request.json["email"]
        res1 = org_api_verifiy(client_id, key)
        if res1:
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, email))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="email doesn't exist"), 400
            cur().execute("delete from code_verify where org_id=%s and email=%s and type=%s",
                          (client_id, email, "EMAIL"))
            conn().commit()
            code = getUUID()[:7]
            tim = time.time()
            cur().execute("insert into code_verify values(%s,%s,%s,%s,%s)",
                          (client_id, email, code, tim, "EMAIL"))
            conn().commit()
            # print(res1)

            @copy_current_request_context
            def work():
                sendMail(res1[3], "EMAIL VERIFICATION", "CODE : " +
                         code+"\nCode Valid for 5 mins", [email])
            thread1 = threading.Thread(target=work)
            thread1.start()
            return jsonify(message="VERIFICATION CODE SENT CHECK EMAIL", valid_period="300"), 200

        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/email_verify', methods=["POST"])
def email_verify():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        code = request.json["code"]
        email = request.json["email"]
        res1 = org_api_verifiy(client_id, key)
        if res1:
            cur().execute("select * from code_verify where org_id=%s and code=%s and email=%s and type=%s",
                          (client_id, code, email, "EMAIL"))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="wrong code"), 400
            res = res[0]
            cur().execute(
                "delete from code_verify where org_id=%s and code=%s and type=%s", (client_id, code, "EMAIL"))
            if float(time.time())-float(res[3]) > 360:
                conn().commit()
                return jsonify(error="code expired"), 400
            cur().execute(
                "update users set verified=%s where org_id=%s and email=%s", (True, client_id, email))
            conn().commit()
            return jsonify(message="success"), 200

        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/forgot_password', methods=["POST"])
def forgot_password():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        email = request.json["email"]
        res1 = org_api_verifiy(client_id, key)
        if res1:
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, email))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="email doesn't exist"), 400
            res = res[0]
            if not res[6]:
                return jsonify(error="EMAIL NOT VERIFIED", email=email), 403
            cur().execute(
                "delete from code_verify where org_id=%s and email=%s and type=%s", (client_id, email, "PASS"))
            conn().commit()
            code = getUUID()[:7]
            tim = time.time()
            cur().execute("insert into code_verify values(%s,%s,%s,%s,%s)",
                          (client_id, email, code, tim, "PASS"))
            conn().commit()
            # print(res1)

            @copy_current_request_context
            def work():
                sendMail(res1[3], "PASSWORD RESET", "CODE : " +
                         code+"\nCode Valid for 5 mins", [email])
            thread1 = threading.Thread(target=work)
            thread1.start()
            return jsonify(message="RESET CODE SENT CHECK EMAIL", valid_period="300"), 200

        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/reset_password', methods=["POST"])
def reset_password():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        code = request.json["code"]
        email = request.json["email"]
        password = request.json["password"]
        res1 = org_api_verifiy(client_id, key)
        if res1:
            cur().execute("select * from code_verify where org_id=%s and code=%s and email=%s and type=%s",
                          (client_id, code, email, "PASS"))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="wrong code"), 400
            res = res[0]
            cur().execute(
                "delete from code_verify where org_id=%s and code=%s and type=%s", (client_id, code, "PASS"))
            if float(time.time())-float(res[3]) > 360:
                conn().commit()
                return jsonify(error="code expired"), 400
            passd = bcrypt.generate_password_hash(password).decode('utf-8')
            cur().execute(
                "update users set password=%s where org_id=%s and email=%s", (passd, client_id, email))
            conn().commit()
            return jsonify(message="success"), 200

        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/self_reset_password', methods=["POST"])
def user_self_reset_password():
    try:
        old_password = request.json["old_password"]
        token = request.json["token"]
        new_password = request.json["new_password"]
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 3600)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, res2["email"]))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="incorrect credentials"), 401
            res = res[0]
            if bcrypt.check_password_hash(res[4], old_password):
                passd = bcrypt.generate_password_hash(
                    new_password).decode('utf-8')
                cur().execute("update users set password=%s where org_id=%s and email=%s",
                              (passd, client_id, res2["email"]))
                conn().commit()
                return jsonify(message="success"), 200
            else:
                return jsonify(error="incorrect credentials"), 401
        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@user_bp.route('/token_verify', methods=["POST"])
def user_token_verify():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        token = request.json["token"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 3600)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            else:
                return jsonify(message="valid token"), 200
        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        return jsonify(error="malformed data"), 400
