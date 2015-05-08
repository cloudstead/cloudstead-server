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

var CloudsteadProgressbar = function(sel, context){
	var self = this;

	this.context = context;
	this.selector = $(sel);
	this.bar = $("#cs-progress-bar");
	this.progressText = $("#cs-progress-text");
	this.currentPercentage = 0; 
	this.timeoutId = -1; 

	this.start = function(){
		console.log("started");
		self.currentPercentage = 0; 
		self.show();
		self.doProgress();
	};

	this.doProgress = function(){

		if(self.currentPercentage < 80){
			self.timeoutId = setTimeout(function(){
				console.log(self.currentPercentage, " % = 2s");
				self.currentPercentage += 5;

				self.renderValues(self.currentPercentage);

				if(self.currentPercentage <= 100){
					self.doProgress();
				}
			}, 2000);
		}
		else if (self.currentPercentage >= 80 && self.currentPercentage < 98){
			self.timeoutId = setTimeout(function(){
				console.log(self.currentPercentage, " % = 5s");
				self.currentPercentage += 2;

				self.renderValues(self.currentPercentage);

				if(self.currentPercentage <= 100){
					self.doProgress();
				}
			}, 5000);
		}
		else{
			//DO NOTHING
		}
	};

	this.done = function(callback){
		self.currentPercentage = 100;
		self.renderValues(self.currentPercentage);
		clearTimeout(self.timeoutId);
		setTimeout(function(){ 
			self.hide();
			alertify.success("New cloudstead created.");
			if(callback){
				callback();
			}
		}, 2000);
	};

	this.doneWithError = function(){
		self.currentPercentage = 0;
		self.renderValues(self.currentPercentage);
		clearTimeout(self.timeoutId);	
		self.hide();
		alertify.error('Error on create new instance.');
	};
	
	this.hide = function(){
		self.selector.addClass('hide');
	};

	this.show = function(){
		self.selector.removeClass('hide');
	};

	this.renderValues = function(percentage){
		self.bar.css('width', percentage + "%");
		self.progressText.html(percentage + "% complete");		
	};
};

API_ROOT='/cloudstead';

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
			url: API_ROOT + url,
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
			url: API_ROOT + url,
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
			url: API_ROOT + url,
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
			'url': API_ROOT + '/api/admins',
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
			'url':API_ROOT + '/api/cloudos',
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
			'url': API_ROOT + '/api/admins/' + uuid,
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
		return this._post('/api/admins/' + data.uuid, data);
	},

	get_cloudstead_regions: function() {
		return this._get('/api/options/regions');
	},

	get_cloudstead_editions: function() {
		return this._get('/api/options/editions');
	},

	get_cloudstead_bundles: function() {
		return this._get('/api/options/bundles');
	},

	new_cloud_os: function (cloudOsRequest, callback) {
		var cloudsteadProgressbar = new CloudsteadProgressbar("#progress-bar-block");
		cloudsteadProgressbar.start();
		Ember.$.ajax({
			'type': 'PUT',
			'url': API_ROOT + '/api/cloudos/' + cloudOsRequest.name,
			'contentType': 'application/json',
			'beforeSend': add_api_auth,
			'data': JSON.stringify(cloudOsRequest),
			'success': function (data, status, jqXHR) {
				cloudsteadProgressbar.done(callback);
			},
			'error': function (jqXHR, status, error) {
				result = jqXHR.responseJSON[0];
				cloudsteadProgressbar.doneWithError();
			},
			'complete': function(jqXHR, status, error) {
				
			}
		});
	},

	launch_cloud_os: function (cloudos_name) {
		show_loading();
		$(".launch-loading-for-small").removeClass("invisible").addClass('visible');
		var result = null;
		Ember.$.ajax({
			'type': 'POST',
			'url': API_ROOT + '/api/cloudos/' + cloudos_name + '/launch',
			'contentType': 'application/json',
			'async': false,
			'beforeSend': add_api_auth,
			'success': function (data, status, jqXHR) {
				result = data;
			},
			'error': function (jqXHR, status, error) {
				$(".launch-loading-for-small").removeClass("visible").addClass('invisible');
				console.log('new_cloud_os error: result='+result+', error='+error);
				result = jqXHR.responseJSON[0];
			},
			'complete': function(jqXHR, status, error) {
				hide_loading();
			}
		});
		return result;
	},

	cloud_os_launch_status: function (name) {

		var result = null;
		Ember.$.ajax({
			'type': 'GET',
			'url': API_ROOT + '/api/cloudos/' + name + '/status',
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
			}
		});
		return result;
	},
	delete_cloudos_instance: function (name) {
		show_loading();

		var result = null;
		Ember.$.ajax({
			'type': 'DELETE',
			'url': API_ROOT + '/api/cloudos/' + name,
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
	cloud_os_details: function (name) {

		var result = null;
		Ember.$.ajax({
			'type': 'GET',
			'url': API_ROOT + '/api/cloudos/' + name,
			'contentType': 'application/json',
			'async': false,
			'beforeSend': add_api_auth,
			'success': function (data, status, jqXHR) {
				result = data;
			},
			'error': function (jqXHR, status, error) {
				console.log('cloud_os_details error: result='+result+', error='+error);
			},
			'complete': function(jqXHR, status, error) {
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
			'url': API_ROOT + '/api/auth/forgot_password',
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

	activate_account: function(account_token) {
		// return this._get('/api/auth/activate/' + account_token);
		show_loading();

		var result;
		Ember.$.ajax({
			'type': 'GET',
			'url': API_ROOT + '/api/auth/activate/' + account_token,
			'contentType': 'application/json',
			'async': false,
			'complete': function(jqXHR, status, error) {
				result = {statusCode: jqXHR.status};
				hide_loading();
			}
		});
		return result;
	},
};
