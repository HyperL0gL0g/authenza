eval "echo \"$(cat /var/www/data/client/js/config_template.js)\"" > /var/www/data/client/js/config.js
eval "echo \"$(cat /var/www/data/org/js/config_template.js)\"" > /var/www/data/org/js/config.js
/usr/sbin/nginx -g "daemon off;"