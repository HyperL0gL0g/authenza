function handler() {
    document.querySelector('.modal').style.display = "flex";
}

function handler2() {
    document.querySelector('.modal').style.display = "none";
}

window.onload = function() {
    var mb = document.getElementById("b1");
    var cl = document.getElementById("cl");
    cl.addEventListener("click", handler2);
}

document.getElementById('login-form').addEventListener('submit', function(e) {
    e.preventDefault();

    //const formdata1 = new FormData();
    let data1 = new FormData();
    const name2 = document.getElementById('your_email').value;
    //const name1=document.getElementById('your_pass').value;
    const password2 = document.getElementById('your_pass').value;
    const api_key = config.api_key;
    const client_id = config.client_id;
    //const token = localStorage.getItem('user_login_token');
    user_login(api_key, client_id, name2, password2);


});


function user_login(api_key, client_id, name2, password) {
    var json_data_user_login = {
        "api_key": api_key,
        "client_id": client_id,
        "email": name2,
        "password": password
    };
    var data_user_login = JSON.stringify(json_data_user_login);
    console.log(data_user_login);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/login";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader('Access-Control-Allow-Origin', '*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            //alert(json.token);
            if (xhr.status == 200) {
                if (json['two_factor']) {
                    window.localStorage.setItem('user_login_token', json.token);
                    handler();
                    user_two_factor_check_login(api_key, client_id, json.token);
                } else {
                    window.location = "./login_dash.html";
                    window.localStorage.setItem('user_login_token', json.token);
                }
            } else if (xhr.status == 403) {
                swal("Error", json['error'], "error").then(function() {
                    window.location = "./Email_verify1.html";
                });
                //window.location = "./Email_verify1.html";
            } else {
                swal("Error", json['error'], "error");
            }
            //hideLoader();
        }
    }
    xhr.send(data_user_login);
}


function user_two_factor_check_login(apikey, clientid, token) {

    var json_data_user_two_factor_active_check = {
        "api_key": apikey,
        "client_id": clientid,
        "token": token
    };
    var data_user_two_factor_active_check = JSON.stringify(json_data_user_two_factor_active_check);

    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/two_factor/check_login";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            if (xhr.status == 200 && json['token']) {
                //alert(json.token)
                window.localStorage.setItem('user_login_token', json.token);
                //setTimeout(5000)
                window.location = "./login_dash.html";
            } else {
                setTimeout(user_two_factor_check_login(apikey, clientid, token), 5000);
            }
        }
    }
    xhr.send(data_user_two_factor_active_check);
}