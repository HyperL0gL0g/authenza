from util.util import *
from flask import Blueprint

org_bp = Blueprint('org_bp', __name__)


@org_bp.route('/home')
def home():
    return getUUID()


@org_bp.route('/listing', methods=["POST"])
def org_listing():
    cur().execute("select * from organization")
    res = cur().fetchall()
    resp = {}
    for i in res:
        resp[i[0]] = [i[1], i[4]]
    return jsonify(resp), 200


@org_bp.route('/api_listing', methods=["POST"])
def org_api_listing():
    try:
        token = request.json["token"]
        res = token_verify(token, 3600)
        if res:
            cur().execute(
                "select * from org_api where org_id=%s", (res['id'],))
            res = cur().fetchall()
            response = []
            for i in res:
                response.append({
                    "client_id": i[0],
                    "api_key": i[1],
                    "description": i[2]
                })
            return jsonify(response), 200
        else:
            return jsonify(error="unauthorized access"), 401
    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/user_listing', methods=["POST"])
def org_user_listing():
    try:
        token = request.json["token"]
        res = token_verify(token, 3600)
        if res:
            cur().execute("select * from users where org_id=%s", (res['id'],))
            res = cur().fetchall()
            response = []
            for i in res:
                response.append({
                    "user_id": i[1],
                    "name": i[2],
                    "email": i[3],
                    "two_fact_enable": i[5]
                })
            return jsonify(response), 200
        else:
            return jsonify(error="unauthorized access"), 401
    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/register', methods=["POST"])
def org_register():
    try:
        name = request.form.get("name")
        email = request.form.get("email")
        try:
            file = request.files["logo"]
        except:
            file = ""
        # print(name,email,file)
        password = request.form.get("password")
        org_id = getUUID()
        cur().execute("select * from organization where email=%s", (email,))
        res = cur().fetchall()
        if len(res) != 0:
            return jsonify(error="Email Already Registered"), 400
        passd = bcrypt.generate_password_hash(password).decode('utf-8')
        if file != "" and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file_name = org_id+"."+filename.rsplit('.', 1)[1].lower()
            file.save(os.path.join(baseAddr, "static/logo/", file_name))
            file_name = url_for('static', filename="logo/"+file_name)
        else:
            file_name = ""
        cur().execute("insert into organization values(%s,%s,%s,%s,%s)",
                      (org_id, name, email, passd, file_name))
        conn().commit()
        response = jsonify(message="ORGANIZATION REGISTERED")
#                response.headers.add('Access-Control-Allow-Origin', '*')
        return response, 200

    except psycopg2.errors.UniqueViolation as e:
        return jsonify(error="Email Already Registered"), 400
    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/login', methods=["POST"])
def org_login():
    try:
        email = request.json["email"]
        password = request.json["password"]
        cur().execute("select * from organization where email=%s", (email,))
        res = cur().fetchall()
        if len(res) == 0:
            return jsonify(error="incorrect credentials"), 401
        res = res[0]
        if bcrypt.check_password_hash(res[3], password):
            jwt_token = jwt.encode({"id": res[0], "name": res[1], "email": res[2], "issue_time": time.time(
            )}, secret(), algorithm="HS256").decode('utf-8')
            return jsonify(token=jwt_token, valid_period="3600"), 200
        else:
            return jsonify(error="incorrect credentials"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/forgot_password', methods=["POST"])
def org_forgot_password():
    try:
        email = request.json["email"]
        cur().execute("select * from organization where email=%s", (email,))
        res = cur().fetchall()
        if len(res) == 0:
            return jsonify(error="email doesn't exist"), 400
        cur().execute(
            "delete from code_verify where org_id=%s and email=%s and type=%s", ('0', email, "PASS"))
        conn().commit()
        code = getUUID()[:7]
        tim = time.time()
        cur().execute("insert into code_verify values(%s,%s,%s,%s,%s)",
                      ('0', email, code, tim, "PASS"))
        conn().commit()

        @copy_current_request_context
        def work():
            sendMail("Authenza", "PASSWORD RESET", "CODE : " +
                     code+"\nCode Valid for 5 mins", [email])
        thread1 = threading.Thread(target=work)
        thread1.start()
        return jsonify(message="RESET CODE SENT CHECK EMAIL", valid_period="300"), 200

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/reset_password', methods=["POST"])
def org_reset_password():
    try:
        code = request.json["code"]
        email = request.json["email"]
        password = request.json["password"]
        cur().execute("select * from code_verify where org_id=%s and code=%s and email=%s and type=%s",
                      ('0', code, email, "PASS"))
        res = cur().fetchall()
        if len(res) == 0:
            return jsonify(error="wrong data"), 400
        res = res[0]
        cur().execute(
            "delete from code_verify where org_id=%s and code=%s and type=%s", ('0', code, "PASS"))
        if float(time.time())-float(res[3]) > 360:
            conn().commit()
            return jsonify(error="code expired"), 400
        passd = bcrypt.generate_password_hash(password).decode('utf-8')
        cur().execute(
            "update organization set password=%s where email=%s", (passd, email))
        conn().commit()
        return jsonify(message="success"), 200

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/self_reset_password', methods=["POST"])
def org_self_reset_password():
    try:
        old_password = request.json["old_password"]
        token = request.json["token"]
        new_password = request.json["new_password"]
        res1 = token_verify(token, 3600)
        if res1:
            cur().execute("select * from organization where email=%s",
                          (res1["email"],))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="incorrect credentials"), 401
            res = res[0]
            if bcrypt.check_password_hash(res[3], old_password):
                passd = bcrypt.generate_password_hash(
                    new_password).decode('utf-8')
                cur().execute(
                    "update organization set password=%s where email=%s", (passd, res1["email"]))
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


@org_bp.route('/create_api_key', methods=["POST"])
def org_api_gen():
    try:
        token = request.json["token"]
        description = request.json["description"]
        res = token_verify(token, 3600)
        if res:
            api_key = getUUID()
            cur().execute("insert into org_api values(%s,%s,%s,%s)",
                          (res['id'], api_key, description, res["name"]))
            conn().commit()
            return jsonify(client_id=res['id'], api_key=api_key), 200
        else:
            return jsonify(error="unauthorized access"), 401

    except KeyError as e:
        return jsonify(error="missing data"), 400
    except Exception as e:
        print(e)
        conn().rollback()
        return jsonify(error="malformed data"), 400


@org_bp.route('/force_user_password_reset', methods=["POST"])
def force_user_password_reset():
    try:
        token = request.json["token"]
        email = request.json["email"]
        password = request.json["password"]
        res1 = token_verify(token, 3600)
        if res1:
            cur().execute(
                "select * from users where org_id=%s and email=%s", (res1["id"], email))
            res = cur().fetchall()
            if len(res) == 0:
                return jsonify(error="no such user"), 400
            res = res[0]
            passd = bcrypt.generate_password_hash(password).decode('utf-8')
            cur().execute(
                "update users set password=%s where org_id=%s and email=%s", (passd, res1["id"], email))
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
