function msg_alert (message) {
	alert(Em.I18n.translations['alerts'][message]);
}

function add_api_auth (xhr) {
	var token = sessionStorage.getItem('api_token');
	xhr.setRequestHeader(Api.API_TOKEN, token);
}

function show_loading(){
	$('.loading_animation').removeClass("hide");
}

function hide_loading(){
	$('.loading_animation').addClass("hide");
}

API_RESPONSE_STATUS = {
	success: 'success',
	error: 'error',
	isSuccess: function(responseStatus) {
		return responseStatus === this.success;
	},
	isError: function(responseStatus) {
		return responseStatus === this.error;
	}
};

Api = {
	'API_TOKEN': 'x-cloudstead-api-key',

	'json_safe_parse': function (j) {
		return j ? JSON.parse(j) : null;
	},

	_get: function (url) {
		var results = null;
		show_loading();
		Ember.$.ajax({
			type: 'GET',
			url: url,
			async: false,
			beforeSend: add_api_auth,
			success: function (response, status, jqXHR) {
				results = response;
			},
			complete: function (jqXHR, status, error) {
				hide_loading();
			}
		});
		return results;
	},

	_update: function (method, url, data) {
		var result = null;
		show_loading();
		Ember.$.ajax({
			type: method,
			url: url,
			async: false,
			contentType: 'application/json',
			data: JSON.stringify(data),
			beforeSend: add_api_auth,
			success: function (response, status, jqXHR) {
				result = response;
			},
			error: function (jqXHR, status, error) {
				console.log('setup error: status='+status+', error='+error+', url='+url);
				result = jqXHR;
			},
			complete: function (jqXHR, status, error) {
				result.status = status;
				hide_loading();
			}
		});
		return result;
	},

	_post: function(url, data) { return Api._update('POST', url, data); },
	_put:  function(url, data) { return Api._update('PUT', url, data); },

	_delete: function (url) {
		var ok = false;
		show_loading();
		Ember.$.ajax({
			type: 'DELETE',
			url: url,
			async: false,
			beforeSend: add_api_auth,
			'success': function (accounts, status, jqXHR) {
				ok = true;
			},
			'error': function (jqXHR, status, error) {
				alert('error deleting '+url+': '+error);
			},
			complete: function (jqXHR, status, error) {
				hide_loading();
			}
		});
		return ok;
	},

	register_admin: function (reg) {
		return this._put('/api/admins/' + encodeURIComponent(reg.email), reg);
	},

	login_admin: function (login) {
		return this._post('/api/admins', login);
	},

	send_second_factor: function(data){
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},
	list_cloudos_instances: function () {
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return instances;
	},
	get_admin_profile: function (uuid) {
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},
	update_admin_profile: function (data) {
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},
	new_cloud_os: function (cloudOsRequest) {
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},

	cloud_os_launch_status: function (name) {
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},
	delete_cloudos_instance: function (name) {
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},

	forgot_password: function(email){
		show_loading();

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
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},

	reset_password: function(token, password){
		var data = {
			token: token,
			password: password
		};

		return this._post('/api/auth/reset_password', data);
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
