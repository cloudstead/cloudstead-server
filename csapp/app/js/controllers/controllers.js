String.prototype.trim = String.prototype.trim || function trim() { return this.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); };

App.RegistrationController = Ember.ObjectController.extend({
	tos: false,
	triggered_focus: false,
	actions: {

		changeInput: function() {
			$('input').trigger('focus');
		},
		doNewAccount: function () {
			this.set("mobilePhoneCountryCode", this.get("mobilePhoneCountry").code);
			var validate = this.validateSignup(
				this.get('firstName'),
				this.get('lastName'),
				this.get('email'),
				this.get('mobilePhoneCountryCode'),
				this.get('mobilePhone'),
				this.get('password'),
				this.get('password2'),
				this.get('tos'),
				this.get('activationCode')
			);

			// triger focus event, alter flag and then re-call this action.
			// this is done to ensure that ember registers the autocompleted text in the input elements.
			// TODO: refactor this.
			if (!this.triggered_focus) {
				$('input').trigger('focus');
				this.triggered_focus = true;
				this.send('doNewAccount');
			}else{
				this.triggered_focus = false;

				if ( validate.firstName || validate.lastName || validate.email ||
									validate.mobilePhoneCountryCode || validate.mobilePhone ||
									validate.password || validate.password2 || validate.tos ||
									validate.activationCode) {
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
										"tos": validate.tos,
										"activationCode": validate.activationCode
									}
								}
							})
						);
					return false;
				}

				// todo: ensure minimum password length. perhaps some generic validation framework can be applied?
				// instead of doing validation imperatively, let's do declarative validation, like how the backend API works...
				var result = Api.register_admin({
					name: this.get('email'),
					firstName: this.get('firstName'),
					lastName: this.get('lastName'),
					admin: false,
					suspended: false,
					twoFactor: false,
					email: this.get('email'),
					emailVerified: false,
					mobilePhoneCountryCode: this.get('mobilePhoneCountryCode'),
					mobilePhone: this.get('mobilePhone'),
					maxCloudsteads: 1,
					password: this.get('password'),
					tos: !!this.get('tos'),
					activationCode: this.get('activationCode'),
					accountName: this.get('email'),
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
			}
		},
		close: function() {
			return this.transitionToRoute('index');
		}
	},
	validateSignup: function(firstName, lastName, email, mobilePhoneCountryCode, mobilePhone, password, password2, tos, activationCode){

		var response = {
			"firstName": null, "lastName": null, "email": null,
			"mobilePhoneCountryCode": null, "mobilePhone": null,
			"password":null, "password2":null, "tos": null, "activationCode": null
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

		if (!activationCode || (activationCode.trim() === '')) {
			response.activationCode = error_msg.field_required;
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
	countryList: Countries.list
});


App.LoginController = Ember.ObjectController.extend({
	actions: {
		doLogin: function () {

			// data check

			var validate = LoginValidator.validate(this.get('name'),this.get('password'));

			if (validate.hasFailed()){
				this._handleLoginError(validate.errors);
				return false;
			}

			// validation ok, check device cookies
			this._generateDeviceCookies();

			var result = Api.login_admin({
				name: this.get('name'),
				password: this.get('password'),
				deviceId: getCookie("deviceId"),
				deviceName: getCookie("deviceName")
			});

			if ( (result.status === 'success') && (result.api_token)) {
				if (this.get('previousTransition')) {
					this._retryPreviousTransition();
				}
				else {
					this.transitionToRoute('adminHome');
				}
			}
			else if (result.status === 'error') {
				var error_msg = locate(Em.I18n.translations, 'errors');
				this.set('requestMessages',
					App.RequestMessagesObject.create({
							json: {
								"status": 'error',
								"errors": {
									"name": error_msg.bad_credentials,
									"password": error_msg.bad_credentials,
								}
							}
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
				return this.transitionToRoute('index');
		},

		doForgotPassword: function() {
			var validate = EmailValidator.validate(this.get('name'));

			if (validate.hasFailed()){
				this.set(
					'requestMessages',
					App.RequestMessagesObject.create({
						json: {
							"status": 'error',
							"api_token" : null,
							"errors": {
								"name": validate.errors.email
							}
						}
					})
				);
				return false;
			}
			else{
				Api.forgot_password(this.get("name"));

				this.set(
					'notificationForgotPassword',
					Ember.I18n.translations.notifications.forgot_password_email_sent
				);
			}
		}
	},

	_handleLoginError: function(validationErrors) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: {
					"status": 'error',
					"api_token" : null,
					"errors": validationErrors
				}
			})
		);
	},

	_generateDeviceCookies: function() {
		var ckDeviceId = checkCookie("deviceId");
			var ckDeviceName = checkCookie("deviceName");

			if ((!ckDeviceId) || (!ckDeviceName)){
				setCookie("deviceId", generateDeviceId(), 365);
				setCookie("deviceName", getDeviceName(), 365);
			}
	},

	previousTransition: null,

	_retryPreviousTransition: function() {
		var previousTransition = this.get('previousTransition');
		this.set('previousTransition', null);
		previousTransition.retry();
	},

	name:'',
	password:'',
	requestMessages: null,
	notificationForgotPassword: null
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
	authStatus: null,

	refreshAuthStatus: function() {
		this.set('authStatus', sessionStorage.getItem('api_token'));
	}
});

App.IndexController = Ember.ObjectController.extend({
//	active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
//	username: get_username()
	actions: {
		doRegister: function(){
			this.transitionToRoute('registration');
		},

		doLogin: function(){
			this.transitionToRoute('login');
		}
	}
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
			this.send('closeModal');
			return this.transitionToRoute('index');
		},
		verifyFactor: function(){

			var data = {
				name: this.get('model')["username"],
				secondFactor: this.get('verifyCode'),
				deviceId: this.get('model')["deviceId"],
				deviceName: this.get('model')["deviceName"]
			};

			var validationError = Validator.validateTwoFactorVerificationCode(data.secondFactor);

			if (validationError.verificationCode){
				this._handleVerificationCodeError(validationError);
			}
			else{
				this.send('_validateSecondFactorResponse', Api.send_second_factor(data));
			}
		},
		_validateSecondFactorResponse: function(response) {
			if (response.status === 'success'){
				if (response.api_token) {
					this.send('closeModal');
					this.transitionToRoute('adminHome');
				}
			}else{
				// TODO display error messages, requires error message from API.
			}
		}
	},

	isFirst: function(){
		return this.get('model')["isRegister"];
	}.property('model'),

	_handleVerificationCodeError: function(validationError) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: {
					"status": 'error',
					"api_token" : null,
					"errors": {
						"verifyCode": validationError.verificationCode
					}
				}
			})
		);
	}
});

App.ResetPasswordController = Ember.ObjectController.extend({
	actions:{
		doResetPassword: function () {
			var token = this.get('model').token;
			var delayInSeconds = 3;

			var validation =
				PasswordValidator.validate(this.get("password"), this.get("passwordConfirm"));

			if (validation.hasFailed()){
				this._handleChangeAccountPasswordErrors(validation.errors);
			}
			else{
				Api.reset_password(token, this.get('password'));

				this._setResetNotificationTo(this._delayMessage(delayInSeconds));

				this._delayedTransitionTo("login", delayInSeconds);
			}

		}
	},

	_handleChangeAccountPasswordErrors: function(errors) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: {
					"status": 'error',
					"api_token" : null,
					"errors": errors
				}
			})
		);
	},

	_delayedTransitionTo: function(routeName, delayInSeconds){
		TIMER_STEP_IN_SECONDS = 1;
		var passedInSeconds = 0;
		var self = this;

		var interval = setInterval(
			function() {
				passedInSeconds += 1;
				self._setResetNotificationTo(
					self._delayMessage(parseInt(delayInSeconds - passedInSeconds, 10))
				);

				if (passedInSeconds >= delayInSeconds){
					clearInterval(interval);
					self.transitionToRoute(routeName);
				}
			},
			Timer.s2ms(TIMER_STEP_IN_SECONDS)
		);
	},

	_delayMessage: function(delayInSeconds) {
		return Ember.I18n.translations.notifications.reset_password_successful +
			" " + delayInSeconds + "s.";
	},

	_setResetNotificationTo: function(message){
		this.set("notificationResetPassword", message);
	}
});
