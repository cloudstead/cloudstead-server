String.prototype.trim = String.prototype.trim || function trim() { return this.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); };

App.RegistrationController = Ember.ObjectController.extend({
	tos: false,
	actions: {
		doNewAccount: function () {
			var validate = this.validateSignup(
				this.get('firstName'),
				this.get('lastName'),
				this.get('email'),
				this.get('mobilePhoneCountryCode'),
				this.get('mobilePhone'),
				this.get('password'),
				this.get('password2'),
				this.get('tos')
			);

			if ( validate.firstName || validate.lastName || validate.email ||
								validate.mobilePhoneCountryCode || validate.mobilePhone ||
								validate.password || validate.password2 || validate.tos) {
				this.set('requestMessages',
						App.RequestMessagesObject.create({
							json: {
								"status": 'error',
								"api_token" : null,
								"errors": {
									"firstName": validate.firstName,
									"lastName": validate.lastName,
									"email": validate.email,
									"mobilePhoneCountryCode": validate.mobilePhoneCountryCode,
									"mobilePhone": validate.mobilePhone,
									"password": validate.password,
									"password2": validate.password2,
									"tos": validate.tos
								}
							}
						})
					);
				return false;
			}

			// todo: ensure minimum password length. perhaps some generic validation framework can be applied?
			// instead of doing validation imperatively, let's do declarative validation, like how the backend API works...
			var result = Api.register_admin({
				firstName: this.get('firstName'),
				lastName: this.get('lastName'),
				email: this.get('email'),
				mobilePhoneCountryCode: this.get('mobilePhoneCountryCode'),
				mobilePhone: this.get('mobilePhone'),
				password: this.get('password'),
				tos: !!this.get('tos')
			});

			if (result.status === 'success') {

				var ckDeviceId = checkCookie("deviceId");
				var ckDeviceName = checkCookie("deviceName");

				if ((!ckDeviceId) || (!ckDeviceName)){
					setCookie("deviceId", generateDeviceId(), 365);
					setCookie("deviceName", getDeviceName(), 365);
				}

				result = Api.login_admin({
					name: this.get('email'),
					password: this.get('password'),
					deviceId: getCookie("deviceId"),
					deviceName: getCookie("deviceName")
				});

				if ((result.status === 'success') && (result.twofactor)){
					this.send('closeModal');
					this.set('model',{
						username: this.get('email'),
						deviceId: getCookie("deviceId"),
						deviceName: getCookie("deviceName"),
						isRegister: true
					});
					this.send('openModal','twoFactorVerification', this.get('model') );
				}else{
					// TODO should not happen, but nevertheless handle this
				}

			}
			else if (result.status === 'error') {
				this.set('requestMessages',
					App.RequestMessagesObject.create({
						json: result
					})
				);
			}
		},
		close: function() {
			return this.send('closeModal');
		}
	},
	validateSignup: function(firstName, lastName, email, mobilePhoneCountryCode, mobilePhone, password, password2, tos){

		var response = {
			"firstName": null, "lastName": null, "email": null,
			"mobilePhoneCountryCode": null, "mobilePhone": null,
			"password":null, "password2":null, "tos": null
		};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var pattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

		if (!firstName || (firstName.trim() === '')){
			response.firstName = error_msg.field_required;
		}
		if (!lastName || (lastName.trim() === '')){
			response.lastName = error_msg.field_required;
		}

		if (!email || (email.trim() === '')){
			response.email = error_msg.field_required;
		} else if(!pattern.test(email)){
			response.email = error_msg.email_invalid;
		}

		if (!mobilePhoneCountryCode || (mobilePhoneCountryCode.trim() === '')) {
			response.mobilePhoneCountryCode = error_msg.field_required;
		}

		if (!mobilePhone || (mobilePhone.trim() === '')) {
			response.mobilePhone = error_msg.field_required;
		}

		if (!password || (password.trim() === '')){
			response.password = error_msg.field_required;
		} else if(password.length < 8) {
			response.password = error_msg.password_short;
		}

		if (!password2 || (password2.trim() === '')){
			response.password2 = error_msg.field_required;
		}

		if (password !== password2) {
			response.password2 = error_msg.password_mismatch;
		}

		if (!tos || ((''+tos).trim() === '')) {
			response.tos = error_msg.field_required;
		}

		return response;
	},
	requestMessages:'',
	firstName:'',
	lastName:'',
	email:'',
	mobilePhoneCountryCode:'',
	mobilePhone:'',
	password:'',
	password2:'',
});


App.LoginController = Ember.ObjectController.extend({
	actions: {
		doLogin: function () {

			// data check

			var validate = this.validateLogin(this.get('name'),this.get('password'));

			if ( (validate.email) || (validate.password)){
					this.set(
						'requestMessages',
						App.RequestMessagesObject.create({
							json: {
								"status": 'error',
								"api_token" : null,
								"errors": {
									"name": validate.name,
									"password": validate.password
								}
							}
						})
					);
				return false;
			}

			// validation ok, check device cookies
			var ckDeviceId = checkCookie("deviceId");
			var ckDeviceName = checkCookie("deviceName");

			if ((!ckDeviceId) || (!ckDeviceName)){
				setCookie("deviceId", generateDeviceId(), 365);
				setCookie("deviceName", getDeviceName(), 365);
			}

			var result = Api.login_admin({
				name: this.get('name'),
				password: this.get('password'),
				deviceId: getCookie("deviceId"),
				deviceName: getCookie("deviceName")
			});

			if ( (result.status === 'success') && (result.api_token)) {
				window.location.href = window.location.protocol + '//' +
										window.location.host + '/' +
										'#/adminHome';
			}
			else if (result.status === 'error') {
				this.set('requestMessages',
					App.RequestMessagesObject.create({
						json: result
					})
				);
			}else if( (result.status === 'success') && (result.twofactor === true) ){
				this.send('closeModal');
				this.set('model',{
					username: this.get('name'),
					deviceId: getCookie("deviceId"),
					deviceName: getCookie("deviceName"),
					isRegister: false
				});
				this.send('openModal','twoFactorVerification', this.get('model') );
			}
		},
		close: function() {
				return this.send('closeModal');
			}
	},
	validateLogin: function(username, password){

		var response = {"name": null, "password":null};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var pattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

		if ((username.trim() === '') || (!username)){
			response.name = error_msg.field_required;
		}else if(!pattern.test(username)){
			response.name = error_msg.email_invalid;
		}

		if ((password.trim() === '') || (!password)){
			response.password = error_msg.field_required;
		}
		return response;
	},
	name:'',
	password:'',
	requestMessages:''
});

App.AdminHomeController = Ember.ObjectController.extend({
	cloudosInstances: function(){
		var result = Api.list_cloudos_instances();
		try {
			var uuid = result[0]["uuid"];
			return Api.list_cloudos_instances();
		}catch(e){
			return null;
		}

	}.property(),
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

			var status = Api.new_cloud_os(cloudOsRequest);
			if (status) {
				this.send('openModal','cloudOsCreation', this.get('model') );
			} else {
				alert(locate(Em.I18n.translations, status.errorMessageKey));
			}
		},
		deleteInstance: function(instanceName){
			var r = confirm("Are you sure you want to delete cloudstead "+instanceName+" ?");
			if (r === true) {
				result = Api.delete_cloudos_instance(instanceName);

					if (result) {
						alert('Cloudstead ' + instanceName + 'is successfully deleted');
						location.reload();
					} else {
						console.log('not deleted');
					}

			} else {
					console.log('action canceled');
			}
		},
		close: function() {
			return this.send('closeModal');
		},
		addMoreClouds:function(){
			this.set('isAddCloudsEnabled',!this.get('isAddCloudsEnabled'));
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

		if ((cloudOsName.trim() === '') || (!cloudOsName)){
			response.cloudOsName = error_msg.field_required;
		}else if (cloudOsName.length < 4){
			response.cloudOsName = error_msg.cloudosname_short;
		}
		if ($.inArray(cloudOsName.trim().toLowerCase(), reserved) > -1){
			response.cloudOsName = error_msg.cloudos_reserverd;
		}

		return response;
	},
	isAddCloudsEnabled:function(){
		if (this.get('cloudosInstances')){
			return false;
		}
		else{
			return true;
		}
	}.property('cloudosInstances')
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
//	api_token: sessionStorage.getItem('api_token'),
//	active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
//	actions: {
//		'select_app': function (app_name) {
//			this.transitionTo('app', App.app_model(app_name));
//		}
//	}
});

App.IndexController = Ember.ObjectController.extend({
//	active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
//	username: get_username()
});

App.CloudOsCreationController = Ember.ObjectController.extend({
	actions:{
		backToAdmin:function(){
			this.send('closeModal');
			location.reload();
		}
	},
	getStatusData: function(){
		var self = this;
		var result = Api.cloud_os_launch_status( self.get('model')["cloudOsRequest"]["name"]);
		if (result){
			if (result.history){
				var last_status = result.history[result.history.length-1];
				$('#progressMeter').css('width', (result.history.length * 10) + '%');
			}

			var statusInterval = setInterval(function(){

				result = Api.cloud_os_launch_status(self.get('model')["cloudOsRequest"]["name"]);
				if (result.history){
					last_status = result.history[result.history.length-1];
					$('#progressMeter').css('width', (result.history.length * 10) + '%');
					self.set('statusMessage', swapStatusMessage(last_status["messageKey"]));

					if (result.history.length >= 10){
						self.set('isInProgress', false);
						window.clearInterval(statusInterval);
					}
				}
			}, 5000);
		}
		else{
			alert("Error fetching status history");
			this.send('closeModal');
			location.reload();
		}

		return result;
	},
	statusMessage : function(){
		var status = this.getStatusData();
		if (status.history){
			var last_status = status.history[status.history.length - 1];
			return swapStatusMessage(last_status["messageKey"]);
		}else{
			return null;
		}
	}.property(),
	isInProgress:true
});

App.AdminDetailsController = Ember.ObjectController.extend({
	actions:{
		updateAdminAccount: function(){
			sessionData = JSON.parse(sessionStorage.active_admin);

			var data = {
				email: this.get('email'),
				name: this.get('email'),
				accountName: this.get('email'),
				firstName: this.get('firstName'),
				lastName: this.get('lastName'),
				mobilePhoneCountryCode : this.get('mobilePhoneCountryCode'),
				mobilePhone: this.get('mobilePhone'),

				admin: sessionData.admin,
				uuid: sessionData.uuid,
				suspended: sessionData.suspended,
				twoFactor: sessionData.twoFactor,
				emailVerified: sessionData.emailVerified,
				tos: true
			};

			var result = Api.update_admin_profile(data);
			if(result){
				alert('Account updated successfully.');
				location.reload();
			}

			//TODO - error checking, nicer messaging, update password part
		}
	},
	tempProperty : "!!!"
});

App.TwoFactorVerificationController = Ember.ObjectController.extend({
	actions:{
		close: function() {
			return this.send('closeModal');
		},
		verifyFactor: function(){

			var data = {
				name: this.get('model')["username"],
				secondFactor: this.get('verifyCode'),
				deviceId: this.get('model')["deviceId"],
				deviceName: this.get('model')["deviceName"]
			};

			// TODO validate data

			var result = Api.send_second_factor(data);
			if (result.status === 'success'){
				if (result.api_token) {
					window.location.href = window.location.protocol + '//' +
											window.location.host + '/' +
											'#/adminHome';
				}
			}else{
				// TODO display error messages
			}
		}
	},
	isFirst: function(){
		return this.get('model')["isRegister"];
	}.property('model')
});
