![Authenza](https://github.com/logicinfinite/authenza/blob/master/images/authenza.png?raw=true)
<br><br>
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)   <a href='https://github.com/logicinfinite' target='_blank'><img src='https://img.shields.io/github/followers/logicinfinite.svg?label=Folow&style=social'></a>
</a><br><br>


#  :octocat: About:

**Authenza** is the  name of the platform(web+mobile) which focuses on providing Two - Factor Authentication Services to organisations facing privacy issues and to organisations who want to upgrade from single-layer authorization to multi-layer authorization. 

# Motivation:

After the  "Internet Boom",  the number of internet users has been increasing exponentially ([look here](https://www.internetlivestats.com/)) , and with that the data stored in the cloud
has also been increasing exponentially.In the Modern Age , privacy of the user's data  is of utmost importance.Privacy is one of those fields where there is no scope of any compromise.
Gone are those days where a single layer of security was enough to ensure proper security .
Two - Facctor Authentication is the modern and advanced way of ensuring complete privacy of data.With two factor authentication the user is the sole owner of his/her data .
Two-Factor Authentication (2FA) is sometimes called multiple factor authentication. In simple terms, it adds an extra layer of security to every online platform you access. The first layer is generally a combination of a username and password. Adding one more step of authenticating your identity makes it harder for an attacker to access your data. 
This drastically reduces the chances of fraud, data loss, or identity theft.

Most 2FA systems are designed to ascertain one of three factors

* Something you know,
* something you have, or
* some part of your body.

![pictorial description](https://secureswissdata.com/wp-content/uploads/2016/11/2fa_3_small.jpg) ![pictorial description](https://secureswissdata.com/wp-content/uploads/2016/11/2fa_1_small.jpg) ![pictorial description](https://secureswissdata.com/wp-content/uploads/2016/11/2fa_2_small.jpg) 

The second layer could ask for the name of your cat, a secret code sent to your phone, or a fingerprint scan. Or, it can include combination of more than one or even all three of them. That’s the reason for calling it sometimes multiple factor authentication.

Regardless of the nature of the second layer, it serves as a vital barrier to your account.

# Why 2-fa ?

Passwords have been the mainstream form of authentication since the start of the digital revolution. But, this security measure is far from infallible. Here are some worrying facts about this traditional security measure:

* 90% of passwords can be cracked in less than six hours.
* Two-thirds of people use the same password everywhere.
* Sophisticated cyber attackers have the power to test billions of passwords every second.
* The vulnerability of passwords is the main reason for requiring and using 2FA.






**Authenza** is a simplified implementation of an Identity as a Service(IDaaS) product. IDaas or Identity as a service is a SaaS-based IAM offering that allows organizations to manage authentication and access controls to provide secure access to their growing number of software and SaaS applications. **Authenza** implements Bio-metric Multi-Factor Authentication using your regular Android device, making authentication and authorization experience more simpler and easily adaptable.

## :dart: Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

##  :dart: Prerequisites

**Authenza** requires [ **Docker**](https://docs.docker.com/), [**Android Studio**](https://developer.android.com/studio) and an Android device (with fingerprint scanner) running Android Version 9.0+ .

## :dart: Getting the project.

```sh
$ git clone https://github.com/gdsoumya/authenza.git
or 
Download and extract the Zip-File
```
##  :dart: Setting up the .env file
Create a **.env** file at the root of the project, it is used by docker to get the required environment variables to start up the different containers. Here is the template for the **.env** file. Replace the values as needed.
```
DB_USER=<db_user_name>
DB_PASSWORD=<db_password>
DB_NAME=<db_name>
SMTP_SERVER=<smtp_server>
SMTP_PORT=<smtp_port>
EMAIL_ID=<email_address>
EMAIL_PASSWORD=<email_password>
API_KEY=<api_key_generated_from_org_dashboard>
CLIENT_ID=<client_id_generated_from_org_dashboard>
BASE_URL=<url_for_server>
```
An example of the **.env** file would look like :
```
DB_USER=postgres
DB_PASSWORD=12345678
DB_NAME=authenza
SMTP_SERVER=smtp.gmail.com
SMTP_PORT=587
EMAIL_ID=<gmail_id>
EMAIL_PASSWORD=<gmail_password>
API_KEY=<api_key>
CLIENT_ID=<client_id>
BASE_URL=http://localhost
```
If you are using Gmail as the email service you may have to turn on [**Less Secure Application Access**](https://support.google.com/accounts/answer/6010255?hl=en).<br>
At first you may put any random value for the **API_KEY** and **CLIENT_ID**, later on after creating an API_KEY from the org dashboard you can change the values and restart docker-compose.

##  :dart: Setting up the Android APK
Before you can build the android APK you need to change the base url in the source code, find the java files
```
app/src/main/java/com/example/fingerprint_protection/scannerActivity.java
app/src/main/java/com/example/fingerprint_protection/fingerPrint_Auth.java
```
And replace **http://localhost** with you own **BASE_URL**. You can then build the apk and run it on your android device (version 9.0+).
## :dart: Starting the Server
To start the server
```sh
$ cd authenza
$ docker-compose up
```
A Nginx Server will be exposed, it will listen on **0.0.0.0:80**, you can access the server using **http://0.0.0.0** or using **http://BASE_URL** using your browser. The following are the root paths for **Organization** and **Org-Client** UIs respectively :
```
http://BASE_URL/org
http://BASE_URL/client
```
A simplified architecture for the setup can be seen below.
![Authenza Deplotment Diagram](https://github.com/gdsoumya/authenza/blob/master/images/deployment.png?raw=true)
### Points to Remember
The **Org-Client** UI will not function until correct **API_KEY** and **CLIENT_ID** is supplied, to do this you need to start the server once and register a Org account and create an API Key. You can then stop the previous server and put the details in the **.env** file and restart the server using :
```
$ docker-compose up
```
## :dart:  Contributors
-   **Soumya Ghosh Dastidar** : Backend
-   **Niraj Singh** : Android
-   **Abhinav Ghosh** : Front-End
-   **Bivas Ranjan Das** : Front-End
-   **Kaustav Ghosh** : Front-End
