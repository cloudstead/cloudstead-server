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

					DeviceCookieGenerator.generate();

					var loginData = {
						name: this.get('email'),
						password: this.get('password'),
						deviceId: getCookie("deviceId"),
						deviceName: getCookie("deviceName")
					};

					var loginCallbacks = {
						failedValidation: this._handleLoginValidationError,
						failedCredentials: this._handleLoginCredentialError,
						needsTwoFactor: this._showTwoFactorModal,
						success: this._handleSuccess
					};

					var loginService = new LoginService(loginData, loginCallbacks);

					loginService.handleResponse(this, loginService.login());

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

	_handleLoginValidationError: function(validationErrors) {
		// TODO implement this if needed.
	},

	_handleLoginCredentialError: function(validationErrors) {
		// TODO implement this if needed.
	},

	_handleSuccess: function(){
		// TODO implement this if needed.
	},

	_showTwoFactorModal: function() {
		this.send('closeModal');
		this.set('model',{
			username: this.get('email'),
			deviceId: getCookie("deviceId"),
			deviceName: getCookie("deviceName"),
			isRegister: true
		});
		this.send('openModal','twoFactorVerification', this.get('model') );
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
