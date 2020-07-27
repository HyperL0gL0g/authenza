document.getElementById('login-form').addEventListener('submit', function(e) {
    e.preventDefault();

    let data1 = new FormData();
    const name2 = document.getElementById('your_email').value;

    const api_key = config.api_key;
    const client_id = config.client_id;

    forgot_password(api_key, client_id, name2);
});


function forgot_password(api_key, client_id, name2) {
    var json_data_user_forgot_password = {
        "api_key": api_key,
        "client_id": client_id,
        "email": name2
    };

    var data_user_forgot_password = JSON.stringify(json_data_user_forgot_password);
    console.log(data_user_forgot_password);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/user/forgot_password";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            if (xhr.status == 200) {
                alert("SUCCESS")
                window.location = "./PASSWORD_RESET.html";
            } else if (xhr.status >= 400) {
                console.log("error");
            }
        }
    }
    xhr.send(data_user_forgot_password);
}