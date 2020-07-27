from util.util import *
from flask import Blueprint

two_fa_bp = Blueprint('two_fa_bp', __name__)


@two_fa_bp.route('/home')
def home():
    return getUUID()


@two_fa_bp.route('/enable', methods=["POST"])
def user_enable_two_factor():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        token = request.json["token"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 3600)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            # print(res2)
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, res2["email"]))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="user has been removed"), 401
            res = res[0]
            if res[5]:
                return jsonify(error="two factor already enabled"), 400
            else:
                cur().execute(
                    "select * from user_2_factor where org_id=%s and u_id=%s", (client_id, res[1]))
                res1 = cur().fetchall()
                if len(res1) != 0:
                    res1 = res1[0]
                    if float(time.time())-float(res1[4]) > 300:
                        img = baseAddr+"static/images/"+res1[3]+".png"
                        img = os.path.join(app.root_path, img)
                        os.remove(img)
                        cur().execute(
                            "delete from user_2_factor where org_id=%s and u_id=%s", (client_id, res[1]))
                        conn().commit()
                    else:
                        return jsonify(error="2FA activation already initialized please complete it first or cancel it"), 400
                qr_id = getUUID()
                tim = time.time()
                jwt_token = jwt.encode({"client_id": client_id, "api_key": key, "u_id": res[1], "name": res[
                                       2], "email": res[3], "qr": qr_id, "issue_time": tim}, secret(), algorithm="HS256").decode('utf-8')
                qr = pyqrcode.create(jwt_token)
                qr.png(baseAddr+"static/images/"+qr_id+".png", scale=3)
                cur().execute("insert into user_2_factor values(%s,%s,%s,%s,%s)",
                              (client_id, res[1], "REG", qr_id, tim))
                conn().commit()
                return jsonify(qr=url_for('static', filename="images/"+qr_id+".png"), valid_period="300"), 200
        else:
            return jsonify(error="unauthorized access"), 401

    except psycopg2.errors.UniqueViolation as e:
        return jsonify(error="2FA has been already initialized please complete it first or cancel it"), 400
    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@two_fa_bp.route('/disable', methods=["POST"])
def user_disable_two_factor():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        token = request.json["token"]
        password = request.json["password"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 3600)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, res2["email"]))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="user has been removed"), 401
            res = res[0]
            if bcrypt.check_password_hash(res[4], password):
                if not res[5]:
                    return jsonify(error="two factor already disabled"), 400
                cur().execute("update users set two_fact=%s where org_id=%s and email=%s",
                              (False, client_id, res2["email"]))
                conn().commit()
                return jsonify(message="success"), 200
            else:
                return jsonify(error="incorrect password"), 401
        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@two_fa_bp.route('/confirm_reg', methods=["POST"])
def two_factor_confirm_reg():
    try:
        token = request.json["token"]
        res = token_verify(token, 300)
        if not res:
            return jsonify(error="token invalid"), 401
        client_id = res["client_id"]
        key = res["api_key"]
        res2 = org_api_verifiy(client_id, key)
        if not res2:
            return jsonify(error="unauthorized access"), 401
        cur().execute("select * from users where org_id=%s and id=%s",
                      (client_id, res["u_id"]))
        res2 = cur().fetchall()
        res2 = res2[0]
        if res2[5]:
            return jsonify(error="two factor already enabled"), 400
        cur().execute("select * from user_2_factor where org_id=%s and u_id=%s and qr=%s",
                      (client_id, res["u_id"], res["qr"]))
        res2 = cur().fetchall()
        if len(res2) == 0:
            return jsonify(error="two factor already enabled"), 400
        img = baseAddr+"static/images/"+res["qr"]+".png"
        img = os.path.join(app.root_path, img)
        os.remove(img)
        cur().execute("update users set two_fact=%s where org_id=%s and id=%s",
                      (True, client_id, res["u_id"]))
        cur().execute("delete from user_2_factor where org_id=%s and u_id=%s and qr=%s",
                      (client_id, res["u_id"], res["qr"]))
        conn().commit()
        return jsonify(message="success"), 200
    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@two_fa_bp.route('/active_check', methods=["POST"])
def two_factor_active_check():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        token = request.json["token"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 3600)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            # print(res2)
            cur().execute(
                "select * from users where org_id=%s and email=%s", (client_id, res2["email"]))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="user has been removed"), 401
            res = res[0]
            if res[5]:
                return jsonify(two_factor=True), 200
            else:
                return jsonify(two_factor=False), 200
        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@two_fa_bp.route('/confirm_login', methods=["POST"])
def two_factor_confirm_login():
    try:
        token = request.json["token"]
        res = token_verify(token, -1)
        if not res:
            return jsonify(error="token invalid"), 401
        client_id = res["client_id"]
        key = res["api_key"]
        res2 = org_api_verifiy(client_id, key)
        if not res2:
            return jsonify(error="unauthorized access"), 401
        cur().execute("select * from users where org_id=%s and id=%s",
                      (client_id, res["u_id"]))
        res2 = cur().fetchall()
        res2 = res2[0]
        if not res2[5]:
            return jsonify(error="two factor not enabled"), 400
        cur().execute("select * from user_2_factor where org_id=%s and u_id=%s and type=%s",
                      (client_id, res["u_id"], "LOG"))
        res2 = cur().fetchall()
        if len(res2) == 0:
            return jsonify(error="no 2FA request found"), 400
        res2 = res2[0]
        if float(time.time())-float(res2[4]) > 60:
            return jsonify(error="expired restart 2FA"), 400
        cur().execute("delete from user_2_factor where org_id=%s and u_id=%s and type=%s",
                      (client_id, res["u_id"], "LOG"))
        conn().commit()
        return jsonify(message="success"), 200
    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@two_fa_bp.route('/cancel_reg', methods=["POST"])
def two_factor_cancel_reg():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        token = request.json["token"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 3600)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            cur().execute("select * from user_2_factor where org_id=%s and u_id=%s and type=%s",
                          (client_id, res2["id"], "REG"))
            res1 = cur().fetchall()
            if len(res1) == 0:
                return jsonify(error="no active 2FA flow found"), 400
            res1 = res1[0]
            img = baseAddr+"static/images/"+res1[3]+".png"
            img = os.path.join(app.root_path, img)
            os.remove(img)
            cur().execute("delete from user_2_factor where org_id=%s and u_id=%s and type=%s",
                          (client_id, res2["id"], "REG"))
            conn().commit()
            return jsonify(message="success"), 200
        else:
            return jsonify(error="unauthorized access"), 401
    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@two_fa_bp.route('/check_login', methods=["POST"])
def two_factor_check_login():
    try:
        client_id = request.json["client_id"]
        key = request.json["api_key"]
        token = request.json["token"]
        res = org_api_verifiy(client_id, key)
        res2 = token_verify(token, 70)
        if res:
            if not res2:
                return jsonify(error="token invalid"), 401
            cur().execute("select * from user_2_factor where org_id=%s and u_id=%s and type=%s",
                          (client_id, res2["id"], "LOG"))
            res1 = cur().fetchall()
            if len(res1) == 0:
                res2['complete'] = True
                jwt_token = jwt.encode(
                    res2, secret(), algorithm="HS256").decode('utf-8')
                return jsonify(token=jwt_token, valid_period="3600"), 200
            return jsonify(message="2FA not complete"), 200
        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        print(e)
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400
