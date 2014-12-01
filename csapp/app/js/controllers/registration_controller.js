App.RegistrationController = App.CloudOSController.extend(App.CountriesMixin, {
	hasTriggeredFocus: false,
	actions: {

		changeInput: function() {
			$('input').trigger('focus');
		},

		doNewAccount: function () {
			this.set("mobilePhoneCountryCode", this.get("mobilePhoneCountry").code);

			// triger focus event, alter flag and then re-call this action.
			// this is done to ensure that ember registers the autocompleted text in the input elements.
			// TODO: refactor this.
			if (this.hasTriggeredFocus) {
				this._triggerUnfocus();

				var registrationService =
					new RegistrationService(this, this._registrationData(), this._registrationCallbacks());

				registrationService.perform();

			} else {
				this._triggerFocus();
				this.send('doNewAccount');
			}
		},

		close: function() {
			return this.transitionToRoute('index');
		}
	},

	_registrationData: function() {
		return {
			firstName: this.get('firstName'),
			lastName: this.get('lastName'),
			email: this.get('email'),
			mobilePhoneCountryCode: this.get('mobilePhoneCountryCode'),
			mobilePhone: this.get('mobilePhone'),
			password: this.get('password'),
			password2: this.get('password2'),
			tos: this.get('tos'),
			activationCode: this.get('activationCode')
		};
	},

	_registrationCallbacks: function() {
		var registrationCallbacks = new RegistrationCallbacks();

		registrationCallbacks.addFailedValidation(this._handleValidationError);
		registrationCallbacks.addRegistrationError(this._handleFailedRegistartion);
		registrationCallbacks.addSuccess(this._handleSuccessfulRegistartion);

		return registrationCallbacks;
	},

	_handleValidationError: function(validationErrors) {
		this._setRequestErrors(validationErrors);
	},

	_handleFailedRegistartion: function(registrationErrors) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: registrationErrors
			})
		);
	},

	_handleSuccessfulRegistartion: function() {
		var loginService = new LoginService(this, this._loginData(), this._loginCallbacks());
		loginService.perform();
	},

	_loginData: function() {
		DeviceCookieGenerator.generate();

		return new LoginData(
			this.get('email'),
			this.get('password'),
			getCookie("deviceId"),
			getCookie("deviceName")
		);
	},

	_loginCallbacks: function() {
		var loginCallbacks = new LoginCallbacks();

		loginCallbacks.addFailedValidation(this._handleValidationError);
		loginCallbacks.addFailedCredentials(this._handleLoginCredentialError);
		loginCallbacks.addNeedsTwoFactor(this._showTwoFactorModal);
		loginCallbacks.addSuccess(this._handleSuccessfulLogin);

		return loginCallbacks;
	},

	_handleLoginCredentialError: function(validationErrors) {
		// TODO implement this if needed.
	},

	_handleSuccessfulLogin: function(){
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

	_triggerFocus: function() {
		$('input').trigger('focus');
		this.hasTriggeredFocus = true;
	},

	_triggerUnfocus: function() {
		this.hasTriggeredFocus = false;
	},

	requestMessages: null,

	firstName: '',
	lastName: '',
	email: '',
	mobilePhoneCountryCode: '',
	mobilePhone: '',
	tos: false,
	password: '',
	password2: ''
});
