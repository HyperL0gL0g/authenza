const form = document.getElementById('form');

form.addEventListener('submit', function(e) {
    e.preventDefault();

    const formdata = new FormData();
    let data = new FormData();
    const input = document.getElementById('logo');
    const name1 = document.getElementById('uname').value;
    const email1 = document.getElementById('email').value;
    const password1 = document.getElementById('password').value;

    data.append("logo", input.files[0]);
    data.append("email", email1);
    data.append("name", name1);
    data.append("password", password1);



    var json;
    fetch(config.base_url + '/org/register', { // Your POST endpoint
        method: 'POST',
        mode: 'cors',
        body: data
    }).then(
        response => response.json() // if the response is a JSON object
    ).then(
        data => {
            if ("error" in data)
                swal({
                    title: "ERROR",

                    text: data['error'],
                });
            else {
                swal({
                    title: "SUCCESS",

                    text: "Registration Successful!",
                }).then(isConfirm => { window.location = "./org_login.html"; });
            }
        }
        //alert("SUCCESFULLY REGISTERED ORGANISATION = " +  name1) // Handle the success response object
    ).catch(
        error => console.log(error) // Handle the error response object
    );
    // data.forEach((value,key) => {
    // console.log(value)
    //});

});