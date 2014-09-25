

App.RegistrationController = Ember.ObjectController.extend({
	actions: {
		doNewAccount: function () {
			var validate = this.validateSignup(this.get('email'), this.get('password'), this.get('password2'));
		
			if ( (validate.username) || (validate.password) || (validate.password2)){
				this.set('requestMessages',
						App.RequestMessagesObject.create({
							json: {"status": 'error', "api_token" : null, 
								"errors": 
									{"username": validate.username,
									"password": validate.password,
									"password2": validate.password2}}
					  })
					);
				return false;
			}
			// todo: ensure minimum password length. perhaps some generic validation framework can be applied?
			// instead of doing validation imperatively, let's do declarative validation, like how the backend API works...
			var result = Api.register_admin({
				email: this.get('email'),
				password: this.get('password')
			});

			if ( (result.status == 'success') && (result.api_token)) {
				window.location.href = window.location.protocol + '//' +
										window.location.host + '/' +
										'#/adminHome';
			}
			else if (result.status == 'error') {
				this.set('requestMessages',
					App.RequestMessagesObject.create({
						json: result
					})
				);
			}
		}
	},
	validateSignup: function(username, password, password2){
		
		var response = {"username": null, "password":null, "password2":null};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var pattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		
		if ((username.trim() == '') || (!username)){
			response.username = error_msg.field_required;
		}else if(!pattern.test(username)){
			response.username = error_msg.email_invalid;
		}

		if ((password.trim() == '') || (!password)){
			response.password = error_msg.field_required;
		}else if(password.length < 8) {
			response.password = error_msg.password_short;
		}

		if ((password2.trim() == '') || (!password2)){
			response.password2 = error_msg.field_required;
		}

		if (password != password2) {
			response.password2 = error_msg.password_mismatch;
		}
		return response;
	}
});


App.LoginController = Ember.ObjectController.extend({
	actions: {
		doLogin: function () {

			// data check
			var validate = this.validateLogin(this.get('email'),this.get('password'));

			if ( (validate.username) || (validate.password)){
				  this.set('requestMessages',
						App.RequestMessagesObject.create({
							json: {"status": 'error', "api_token" : null, 
								"errors": 
									{"username": validate.username,
									"password": validate.password}}
						})
					);
				return false;
			}

			var result = Api.login_admin({
				email: this.get('email'),
				password: this.get('password')
			});

			if ( (result.status == 'success') && (result.api_token)) {
				window.location.href = window.location.protocol + '//' +
 										window.location.host + '/' +
 										'#/adminHome';
			}
			else if (result.status == 'error') {
				this.set('requestMessages',
					App.RequestMessagesObject.create({
						json: result
					})
				);
			}
		}
	},
	validateLogin: function(username, password){

		var response = {"username": null, "password":null};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var pattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

		if ((username.trim() == '') || (!username)){
			response.username = error_msg.field_required;
		}else if(!pattern.test(username)){
			response.username = error_msg.email_invalid;
		}

		if ((password.trim() == '') || (!password)){
			response.password = error_msg.field_required;
		}
		return response;
	}
});

App.AdminHomeController = Ember.ObjectController.extend({
	cloudosInstances: Api.list_cloudos_instances(),
	actions: {
		doNewCloudOs: function () {
			var cloudOsRequest = this.get('cloudOsRequest');
			var validate = this.validateName(cloudOsRequest.name);
			if (validate.cloudOsName) {
				this.set('requestMessages',
						App.RequestMessagesObject.create({
							json: {"status": 'error', "api_token" : null, 
								"errors": 
									{"cloudOsName": validate.cloudOsName}}
						})
					);
				return false;
			}
			$('#cloudOsSubmit').hide();
			$('#cloudOsCreating').show();
			var status = Api.new_cloud_os(cloudOsRequest);
			if (status) {
				var name = cloudOsRequest.name;
				this.transitionToRoute('cloudOsStatus', { cloudos_name: name });
			} else {
				alert(locate(Em.I18n.translations, status.errorMessageKey));
			}
		}
	},
	validateName: function(cloudOsName){
		
		var response = {"cloudOsName":null};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var reserved = ["www", "wwws", "http", "https",
						"mail", "email", "mailbox", "mbox", "smtp", "imap", "pop", "pop3",
						"corp", "blog", "news",
						"mobile", "root", "postmaster", "admin",
						"cloudos", "upcloud", "cloudstead"];

		if ((cloudOsName.trim() == '') || (!cloudOsName)){
			response.cloudOsName = error_msg.field_required;
		}else if (cloudOsName.length < 4){
			response.cloudOsName = error_msg.cloudosname_short;
		}
		if ($.inArray(cloudOsName.trim().toLowerCase(), reserved) > -1){
			response.cloudOsName = error_msg.cloudos_reserverd;
		}

		return response;
	}
});

App.CloudOsStatusController = Ember.ObjectController.extend({
	actions: {
		doRelaunchCloudOs: function () {
			var cloudOsRequest = { name: this.get('name') };
			var status = Api.new_cloud_os(cloudOsRequest);
			
			if (status) {
				this.transitionToRoute('cloudOsStatus', cloudOsRequest.name);
			} else {
				alert(locate(Em.I18n.translations, status.errorMessageKey));
			}
		}
	}
});

App.ApplicationController = Ember.ObjectController.extend({
//  api_token: sessionStorage.getItem('api_token'),
//  active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
//  actions: {
//	  'select_app': function (app_name) {
//		  this.transitionTo('app', App.app_model(app_name));
//	  }
//  }
});

App.IndexController = Ember.ObjectController.extend({
//  active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
//  username: get_username()
});

//App.LoginController = Ember.ObjectController.extend({
//active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
//username: get_username(),
//password: '',
//actions: {
//  doLogin: function () {
//	  var creds = {
//		  'name': this.get('username'),
//		  'password': this.get('password')
//	  };
//	  var api_token = Api.login_account(creds);
////	  alert('doLogin received API token: '+api_token);
//	  if (api_token) {
//		  window.location.replace('/index.html');
//	  } else {
//		  // populate error
//	  }
//  },
//
//  clearStorage: function () {
//	  sessionStorage.clear();
//	  localStorage.clear();
//  }
//}
//});

//App.SettingsController = Ember.ObjectController.extend({
//actions: {
//  changePassword: function () {
//	  var newPassword = this.get('new_password');
//	  var confirmPassword = this.get('new_password2');
//	  if (newPassword.length == 0) {
//		  msg_alert('password_mismatch');
//		  return;
//	  }
//	  if (Api.change_password(this.get('current_password'), newPassword)) {
//		  this.transitionTo('settings')
//	  } else {
//		  console.log('error changing password');
//	  }
//  }
//}
//});