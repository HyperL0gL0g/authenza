document.getElementById('form').addEventListener('submit', function(e) {
    e.preventDefault();

    //const formdata1 = new FormData();
    let data1 = new FormData();
    const name2 = document.getElementById('email').value;
    const code = document.getElementById('code').value;
    const pass = document.getElementById('password').value;

    reset_password(name2, code, pass);


});


function reset_password(name2, code, pass) {
    var json_data_org_reset_password = {

        "email": name2,
        "code": code,
        "password": pass
    };

    var data_org_reset_password = JSON.stringify(json_data_org_reset_password);
    console.log(data_org_reset_password);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/org/reset_password";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");

    //xhr.setRequestHeader('Access-Control-Allow-Origin','*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            if (xhr.status == 200) {
                alert("SUCCESS")
                window.location = "./org_login.html";
            } else if (xhr.status >= 400) {
                console.log("error");
            }
        }
    }
    xhr.send(data_org_reset_password);
}