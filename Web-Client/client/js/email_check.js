document.getElementById('login-form').addEventListener('submit', function(e) {
    e.preventDefault();

    //const formdata1 = new FormData();
    let data1 = new FormData();
    const name2 = document.getElementById('your_email').value;
    const code = document.getElementById('code').value;
    //const name1=document.getElementById('your_pass').value;
    const api_key = config.api_key;
    const client_id = config.client_id;
    //const token = localStorage.getItem('user_login_token');
    email_verify(api_key, client_id, name2, code);


});


function email_verify(api_key, client_id, name2, code) {
    var json_data_user_email_verify = {
        "api_key": api_key,
        "client_id": client_id,
        "email": name2,
        "code": code
    };
    console.log(json_data_user_email_verify);

    var data_user_email_verify = JSON.stringify(json_data_user_email_verify);
    console.log(data_user_email_verify);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/email_verify";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");

    //xhr.setRequestHeader('Access-Control-Allow-Origin','*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            if (xhr.status == 200) {
                alert("SUCCESS")
                window.location = "./USER_LOGIN.html";
            } else if (xhr.status >= 400) {
                console.log("error");
            }
        }
    }
    xhr.send(data_user_email_verify);
}