App.LoginController = App.CloudOSController.extend({
	actions: {
		doLogin: function() {
			var loginService = new LoginService(this._loginData(), this._loginCallbacks());

			loginService.handleResponse(this, loginService.login());
		},

		close: function() {
				return this.transitionToRoute('index');
		},

		doForgotPassword: function() {
			var forgotPasswordService =
				new ForgotPasswordService(this.get('email'), this._forgotPasswordCallbacks());

			forgotPasswordService.handleResponse(this, forgotPasswordService.forget());
		}
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
		loginCallbacks.addSuccess(this._transitionToNextRoute);

		return loginCallbacks;
	},

	_forgotPasswordCallbacks: function() {
		var forgotPasswordCallbacks = new ForgotPasswordCallbacks();

		forgotPasswordCallbacks.addFailedValidation(this._handleValidationError);
		forgotPasswordCallbacks.addSuccess(this._displayForgotPasswordNotification);

		return forgotPasswordCallbacks;
	},

	_handleValidationError: function(validationErrors) {
		this._setRequestErrors(validationErrors);
	},

	_handleLoginCredentialError: function(credentialErrors) {
		this._setRequestErrors(credentialErrors);
	},

	_transitionToNextRoute: function(){
		var previousTransition = this.get('previousTransition');

		if (Ember.isNone(previousTransition)){
			this.transitionToRoute('adminHome');
		}
		else{
			this._retryPreviousTransition();
		}
	},

	_showTwoFactorModal: function() {
		this.set('model',{
			username: this.get('email'),
			deviceId: getCookie("deviceId"),
			deviceName: getCookie("deviceName"),
			isRegister: false
		});
		this.send('openModal','twoFactorVerification', this.get('model') );
	},

	_displayForgotPasswordNotification: function() {
		this.set(
			'notificationForgotPassword',
			Ember.I18n.translations.notifications.forgot_password_email_sent
		);
	},

	_retryPreviousTransition: function() {
		var previousTransition = this.get('previousTransition');
		this.set('previousTransition', null);
		previousTransition.retry();
	},

	notificationForgotPassword: null,
	previousTransition: null
});
