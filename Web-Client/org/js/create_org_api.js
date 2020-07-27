document.getElementById('myform').addEventListener('submit', function(e) {
    e.preventDefault();
    console.log("Hello");
    //const formdata1 = new FormData();
    const name2 = document.getElementById('textarea').value;

    create_api(name2);
});


function create_api(name2) {
    var token = window.localStorage.getItem('org_token');
    console.log(token, "HELLOOO")
    var json_data_user_register = {
        "description": name2,
        "token": token
    };
    var data_user_register = JSON.stringify(json_data_user_register);
    console.log(data_user_register);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/org/create_api_key";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader('Access-Control-Allow-Origin', '*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            alert("API CREATED");
            window.location = "./index_api.html";
            //hideLoader();
        }
    }
    xhr.send(data_user_register);
}