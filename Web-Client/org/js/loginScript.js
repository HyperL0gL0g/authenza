document.getElementById('form').addEventListener('submit', function(e) {
    e.preventDefault();

    let data1 = new FormData();
    const name2 = document.getElementById('email').value;

    const password2 = document.getElementById('password').value;

    org_login(name2, password2);

});


function org_login(email, password) {
    var json_data_org_login = {
        "email": email,
        "password": password
    };
    var data_org_login = JSON.stringify(json_data_org_login);
    console.log(data_org_login);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/org/login";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader('Access-Control-Allow-Origin', '*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
            var json = JSON.parse(xhr.responseText);
            window.token = json['token'];
            console.log(json);
            window.localStorage.setItem('org_token', json.token);
            alert("SUCCESFULLY LOGIN BY - " + email);
            //hideLoader();
            //alert("org_token="+window.localStorage.getItem('org_token'));
            window.location = "./index_dash.html";
        } else if (xhr.status == 401) {
            swal({
                title: "OOOPS",
                icon: "error",
                text: "error-UNAUTHORIZED ACCESS",
            });


        } else if (xhr.status == 400) {
            swal({
                icon: "error",
                title: "OOOPS",
                text: "ERROR-2FA has been already initialized please complete it first or wait for it to expire",
            });

        }
    }
    xhr.send(data_org_login);
}