function msg_alert (message) {
	alert(Em.I18n.translations['alerts'][message]);
}

function add_api_auth (xhr) {
	var token = sessionStorage.getItem('api_token');
	xhr.setRequestHeader(Api.API_TOKEN, token);
}

ErrorResponseMessages = {
	"{err.email.notUnique}": {
		errorType: "email",
		message: "This email has already been taken."
	},
	"{err.mobilePhone.notUnique}": {
		errorType: "mobilePhone",
		message: "This phone number has already been registered."
	}
};

Api = {
	'API_TOKEN': 'x-cloudstead-api-key',

	'json_safe_parse': function (j) {
		return j ? JSON.parse(j) : null;
	},

	register_admin: function (reg) {

		var result = {
			"status": null,
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
			'type': 'PUT',
			'url':'/api/admins/' + encodeURIComponent(reg.email),
			'contentType': 'application/json',
			'data': JSON.stringify(reg),
			'async': false,
			'success': function (admin, status, jqXHR) {
				if (admin && admin.uuid) {
					result.status = status;
				}
			},
			'error': function (jqXHR, status, error) {
				result.status = status;
				result.errors.username = error;

				errors = jqXHR.responseJSON;

				if (errors !== undefined){
					errors.forEach(function(error){
						var errorMessage = error.message;
						if(ErrorResponseMessages[errorMessage] !== undefined){
							result.errors[ErrorResponseMessages[errorMessage].errorType] =
								ErrorResponseMessages[errorMessage].message;
						}
					});
				}
			}
		});
		return result;
	},

	login_admin: function (login) {

		var result = {
			"status": null,
			"api_token": null,
			"errors": {
				"name":null,
				"password":null
			}
		};

		sessionStorage.removeItem('api_token');
		Ember.$.ajax({
			'type': 'POST',
			'url':'/api/admins',
			'contentType': 'application/json',
			'data': JSON.stringify(login),
			'async': false,
			'success': function (admin, status, jqXHR) {

				if (admin.account && admin.account.uuid) {

					sessionStorage.setItem('api_token', admin.sessionId);
					sessionStorage.setItem('active_admin', JSON.stringify(admin.account));

					result.status = status;
					result.api_token = sessionStorage.getItem('api_token');
				}

				if (admin.sessionId === '2-factor'){
					result.status = 'success';
					result["twofactor"] = true;
				}
			},
			'error': function (jqXHR, status, error) {
				// TODO : fill out errors differently, according to server response
				result.status = status;
				result.errors.username = error;
			}
		});
		return result;
	},
	send_second_factor: function(data){

		var result = {"status": null,
					"api_token": null,
					"errors": {"verifyCode":null}};

		Ember.$.ajax({
			'type': 'POST',
			'url':'/api/admins/',
			'contentType': 'application/json',
			'async': false,
			'data': JSON.stringify(data),
			'beforeSend': add_api_auth,
			'success': function (data, status, jqXHR) {
				sessionStorage.setItem('api_token', data.sessionId);
				sessionStorage.setItem('active_admin', JSON.stringify(data.account));

				result.status = status;
				result.api_token = sessionStorage.getItem('api_token');
			},
			'error': function (jqXHR, status, error) {
				console.log('login error: status='+status+', error='+error);
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
	get_admin_profile: function (uuid) {
		var result;
		Ember.$.ajax({
			'type': 'GET',
			'url':'/api/admins/' + uuid,
			'contentType': 'application/json',
			'async': false,
			'beforeSend': add_api_auth,
			'success': function (data, status, jqXHR) {
				result = data;
			},
			'error': function (jqXHR, status, error) {
				console.log('login error: status='+status+', error='+error);
			}
		});
		return result;
	},
	update_admin_profile: function (data) {
		var result;
		Ember.$.ajax({
			'type': 'POST',
			'url':'/api/admins/' + data.uuid,
			'contentType': 'application/json',
			'async': false,
			'data': JSON.stringify(data),
			'beforeSend': add_api_auth,
			'success': function (data, status, jqXHR) {
				result = data;
			},
			'error': function (jqXHR, status, error) {
				console.log('login error: status='+status+', error='+error);
			}
		});
		return result;
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
	},
	delete_cloudos_instance: function (name) {
		var result = null;
		Ember.$.ajax({
			'type': 'DELETE',
			'url':'/api/cloudos/' + name,
			'contentType': 'application/json',
			'async': false,
			'beforeSend': add_api_auth,
			'success': function (data, status, jqXHR) {
				result = data;
			},
			'error': function (jqXHR, status, error) {
				console.log('delete_cloudos_instance error: result='+result+', error='+error);
			}
		});
		return result;
	},

	forgot_password: function(email){

		var result = {
			"status": null,
			"errors": {
				"forgotPassword":null
			}
		};

		Ember.$.ajax({
			'type': 'POST',
			'url':'/api/auth/forgot_password',
			'contentType': 'application/json',
			'async': false,
			'data': email,
			'success': function (data, status, jqXHR) {
				result.status = status;
			},
			'error': function (jqXHR, status, error) {
				result.status = status;
				result.errors.forgotPassword = error;
				console.log('Forgot password error: status='+status+', error='+error);
			}
		});
		return result;
	},

	reset_password: function(token, password){

		var result = {
			"status": null,
			"errors": {
				"resetPassword":null
			}
		};

		var data = {
			token: token,
			password: password
		};

		Ember.$.ajax({
			'type': 'POST',
			'url':'/api/auth/reset_password',
			'contentType': 'application/json',
			'async': false,
			'data': JSON.stringify(data),
			'success': function (data, status, jqXHR) {
				result.status = status;
			},
			'error': function (jqXHR, status, error) {
				result.status = status;
				result.errors.resetPassword = error;
				console.log('Reset password error: status='+status+', error='+error);
			}
		});
		return result;
	},

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
