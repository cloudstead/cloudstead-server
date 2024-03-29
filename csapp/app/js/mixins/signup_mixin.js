App.SignupMixin = Ember.Mixin.create({
	hasTriggeredFocus: false,
	actions: {

		changeInput: function() {
			$('input').trigger('focus');
		},

		doNewAccount: function () {

			this.set('password2', this.get('password'));
			this.set('tos', true);
			console.log(">>>>>> ", this.get("mobilePhoneCountry"));
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
		var registrationCallbacks = new BasicServiceCallbacks();

		registrationCallbacks.addFailedValidation(this._handleValidationError);
		registrationCallbacks.addError(this._handleFailedRegistartion);
		registrationCallbacks.addSuccess(this._handleSuccessfulRegistartion);

		return registrationCallbacks;
	},

	_handleValidationError: function(validationErrors) {
		this._setRequestErrors(validationErrors);
	},

	_handleFailedRegistartion: function(registrationErrors) {
		this._setRequestMessage(registrationErrors);
	},

	_handleSuccessfulRegistartion: function() {
		var loginService = new LoginService(this, this._loginDataAfterRegistration(), this._loginCallbacksAfterRegistration());
		loginService.perform();
	},

	_loginDataAfterRegistration: function() {
		DeviceCookieGenerator.generate();

		return new LoginData(
			this.get('email'),
			this.get('password'),
			getCookie("deviceId"),
			getCookie("deviceName")
		);
	},

	_loginCallbacksAfterRegistration: function() {
		var loginCallbacks = new LoginCallbacks();

		loginCallbacks.addFailedValidation(this._handleValidationError);
		loginCallbacks.addError(this._handleSignupLoginCredentialError);
		loginCallbacks.addNeedsTwoFactor(this._showTwoFactorModal);
		loginCallbacks.addSuccess(this._handleSuccessfulLogin);

		return loginCallbacks;
	},

	_handleSignupLoginCredentialError: function(validationErrors) {
		// TODO implement this if needed.
		console.log("LoginCredentialError => ", validationErrors);
	},

	_handleSuccessfulLogin: function(){
		// TODO implement this if needed.
	},

	_showTwoFactorModal: function() {
		this.transitionToRoute('/verification/' + this.get('email'));
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
	mobilePhoneCountryCode: '1',
	mobilePhone: '',
	tos: false,
	password: '',
	password2: '',
});
