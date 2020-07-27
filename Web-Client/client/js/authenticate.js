window.onload = function() {
    var mb = document.getElementById("b1");
    var cl = document.getElementById("cl");
    cl.addEventListener("click", handler2);
}


function handler() {
    document.querySelector('.modal').style.display = "flex";
}

function handler2() {
    user_two_factor_cancel_reg()
    document.querySelector('.modal').style.display = "none";
}

function user_two_factor_enable() {
    const api_key = config.api_key;
    const client_id = config.client_id;

    const token = window.localStorage.getItem('user_login_token');

    var json_data_user_two_factor_enable = {
        "client_id": client_id,
        "api_key": api_key,
        "token": token
    };
    var data_user_two_factor_enable = JSON.stringify(json_data_user_two_factor_enable);
    console.log(data_user_two_factor_enable);
    // showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/two_factor/enable";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            if (xhr.status == 200) {
                console.log(json['qr']);
                var url = config.base_url + "" + json['qr'];
                document.getElementById("qrcode").setAttribute('src', url);
                handler();
                user_two_factor_active_check(api_key, client_id, token);
            } else {
                swal("Error", json['error'], "error");

            }
        }
    }
    xhr.send(data_user_two_factor_enable);
}

function user_two_factor_active_check(apikey, clientid, token) {

    var json_data_user_two_factor_active_check = {
        "api_key": apikey,
        "client_id": clientid,
        "token": token
    };
    var data_user_two_factor_active_check = JSON.stringify(json_data_user_two_factor_active_check);

    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/two_factor/active_check";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            if (xhr.status == 200 && json['two_factor']) {
                handler2();
                swal("Success", "2FA Activated", "success");
            } else {
                console.log(json);
                setTimeout(user_two_factor_active_check(apikey, clientid, token), 5000);
            }
        }
    }
    xhr.send(data_user_two_factor_active_check);
}

function verify_session() {
    const api_key = config.api_key;
    const client_id = config.client_id;

    const token = window.localStorage.getItem('user_login_token');

    var data = {
        "client_id": client_id,
        "api_key": api_key,
        "token": token
    };
    data = JSON.stringify(data);
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/token_verify";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            if (xhr.status == 200) {
                swal("Success", "Session Valid", "success");
            } else {
                swal("Error", "Session Invalid", "error");
            }
        }
    }
    xhr.send(data);
}

function user_two_factor_cancel_reg() {
    const api_key = config.api_key;
    const client_id = config.client_id;
    const token = window.localStorage.getItem('user_login_token');
    var json_data_user_two_cancel_reg = {
        "api_key": api_key,
        "client_id": client_id,
        "token": token
    };
    var data_user_two_cancel_reg = JSON.stringify(json_data_user_two_cancel_reg);

    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/two_factor/cancel_reg";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            if (xhr.status == 200) {
                console.log(json);
            }
        }
    }
    xhr.send(data_user_two_cancel_reg);
}


function parseJwt(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    var test = JSON.parse(jsonPayload)
    document.getElementById('username').text = test['name'];
    console.log(JSON.parse(jsonPayload));
};

parseJwt(window.localStorage.getItem('user_login_token'));