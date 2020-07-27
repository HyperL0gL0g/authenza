document.getElementById('form').addEventListener('submit', function(e) {
    e.preventDefault();

    //const formdata1 = new FormData();
    let data1 = new FormData();
    const name1 = document.getElementById('email').value;

    org_forgot_password(name1);

});


function org_forgot_password(name1) {
    var json_data_org_forgot = {

        "email": name1

    };
    var data_org_forgot = JSON.stringify(json_data_org_forgot);
    console.log(data_org_forgot);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/org/forgot_password";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader('Access-Control-Allow-Origin', '*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            if (xhr.status == 200) {
                window.location = "./reset_password.html";
            } else {
                swal("Error", json['error'], "error");
            }


            //hideLoader();
        }
    }
    xhr.send(data_org_forgot);
}