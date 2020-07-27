from app import app
import util.util as util
import services.org as org
import services.user as user
import services.two_factor as two_fa


if __name__ == '__main__':
    state = util.init()
    if state is not None:
        app.register_blueprint(org.org_bp,url_prefix='/org')
        app.register_blueprint(user.user_bp,url_prefix='/user')
        app.register_blueprint(two_fa.two_fa_bp,url_prefix='/user/two_factor')
        app.run(host='0.0.0.0', port=8080, debug=True)
