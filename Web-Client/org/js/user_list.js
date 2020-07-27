window.onload = function() {
    var t = document.getElementById('table');
    var json_data_user_login = {
        "token": window.localStorage.getItem('org_token')
    };
    var table = t.innerHTML + "<tbody>";
    var data_user_login = JSON.stringify(json_data_user_login);
    console.log(data_user_login);
    //showLoader();
    xhr = new XMLHttpRequest();
    var url = config.base_url + "/org/user_listing";

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.setRequestHeader('Access-Control-Allow-Origin', '*');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status >= 200) {
            var json = JSON.parse(xhr.responseText);
            console.log(json);
            //alert(json.token);
            if (xhr.status == 200) {
                for (var i = 0; i < json.length; i++) {
                    var tr = "<tr>";
                    tr += "<td>" + (i + 1) + "</td>";
                    tr += "<td>" + json[i]['user_id'] + "</td>";
                    tr += "<td>" + json[i]['name'] + "</td>";
                    tr += "<td>" + json[i]['email'] + "</td>";
                    tr += "<td>" + json[i]['two_fact_enable'] + "</td>";
                    tr += "</tr>";
                    table += tr;
                }
                table += "</tbody>"
                t.innerHTML = table;
            } else {
                console.log(json);
            }
            //hideLoader();
        }
    }
    xhr.send(data_user_login);

}