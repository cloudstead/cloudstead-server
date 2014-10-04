function msg_alert (message) {
    alert(Em.I18n.translations['alerts'][message]);
}

function add_api_auth (xhr) {
    var token = sessionStorage.getItem('api_token');
    xhr.setRequestHeader(Api.API_TOKEN, token);
}

Api = {
    'API_TOKEN': 'x-cloudstead-api-key',

    'json_safe_parse': function (j) {
        return j ? JSON.parse(j) : null;
    },

    register_admin: function (reg) {

    	var result = {"status": null,
				  "api_token": null,
				  "errors": {
                      "firstName":null,
                      "lastName":null,
                      "email":null,
                      "mobilePhoneCountryCode":null,
                      "mobilePhone":null,
                      "password":null,
                      "password2":null,
                      "tos":null}};

        sessionStorage.removeItem('api_token');
        Ember.$.ajax({
            'type': 'POST',
            'url':'/api/admins',
            'contentType': 'application/json',
            'data': JSON.stringify(reg),
            'async': false,
            'success': function (admin, status, jqXHR) {
//                alert('received status='+status+', data='+accountAccount);
                if (admin && admin.uuid) {
                    sessionStorage.setItem('api_token', admin.session);
                    sessionStorage.setItem('active_admin', JSON.stringify(admin));
                    
                    result.status = status;
                    result.api_token = sessionStorage.getItem('api_token');
                }
            },
            'error': function (jqXHR, status, error) {
                result.status = status;
                result.errors.username = error;
            }
        });
        return result;
    },

    login_admin: function (login) {

    	var result = {"status": null,
    				  "api_token": null,
    				  "errors": {"name":null,
    							 "password":null}};

        sessionStorage.removeItem('api_token');
        Ember.$.ajax({
            'type': 'PUT',
            'url':'/api/admins',
            'contentType': 'application/json',
            'data': JSON.stringify(login),
            'async': false,
            'success': function (admin, status, jqXHR) {
                if (admin && admin.uuid) {
                    sessionStorage.setItem('api_token', admin.session);
                    sessionStorage.setItem('active_admin', JSON.stringify(admin));

                    result.status = status;
                    result.api_token = sessionStorage.getItem('api_token');
                }
            },
            'error': function (jqXHR, status, error) {
            	// TODO : fill out errors differently, according to server response
                result.status = status
                result.errors.username = error;
            }
        });
        return result;
    },

    list_cloudos_instances: function () {
        instances = [];
        Ember.$.ajax({
            'type': 'GET',
            'url':'/api/cloudos',
            'contentType': 'application/json',
            'async': false,
            'beforeSend': add_api_auth,
            'success': function (data, status, jqXHR) {
                instances = data;
            },
            'error': function (jqXHR, status, error) {
                console.log('login error: status='+status+', error='+error);
            }
        });
        return instances;
    },

    new_cloud_os: function (cloudOsRequest) {
        var result = null;
        Ember.$.ajax({
            'type': 'PUT',
            'url':'/api/cloudos/' + cloudOsRequest.name,
            'contentType': 'application/json',
            'async': false,
            'beforeSend': add_api_auth,
            'data': JSON.stringify(cloudOsRequest),
            'success': function (data, status, jqXHR) {
                result = data;
            },
            'error': function (jqXHR, status, error) {
                console.log('new_cloud_os error: result='+result+', error='+error);
                result = null;
            }
        });
        return result;
    },

    cloud_os_launch_status: function (name) {
        var result = null;
        Ember.$.ajax({
            'type': 'GET',
            'url':'/api/cloudos/' + name + '/status',
            'contentType': 'application/json',
            'async': false,
            'beforeSend': add_api_auth,
            'success': function (data, status, jqXHR) {
                result = data;
            },
            'error': function (jqXHR, status, error) {
                console.log('cloud_os_launch_result error: result='+result+', error='+error);
            }
        });
        return result;
    }

//    'change_password': function (oldPassword, newPassword) {
//        api_token = sessionStorage.getItem('api_token');
//        var ok = false;
//        Ember.$.ajax({
//            'type': 'POST',
//            'url':'/api/accounts/' + api_token + '/password',
//            'contentType': 'application/json',
//            'data': JSON.stringify({
//                'oldPassword': oldPassword,
//                'newPassword': newPassword
//            }),
//            'async': false,
//            'success': function (account, status, jqXHR) {
//                alert('password successfully changed');
//                ok = true;
//            },
//            'error': function (jqXHR, status, error) {
//                alert('error changing password: '+error);
//                console.log('reg error: status='+status+', error='+error);
//            }
//        });
//        return ok;
//    }
//
};
