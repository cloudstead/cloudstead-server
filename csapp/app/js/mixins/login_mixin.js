App.LoginMixin = Ember.Mixin.create({
	content: {},
	actions: {
		doLogin: function() {
			var self = this;
			var loginService = new LoginService(self, self._loginData(), self._loginCallbacks());
			loginService.perform();
		},

		close: function() {
				return this.transitionToRoute('index');
		},

		doForgotPassword: function() {
			var forgotPasswordService =
				new ForgotPasswordService(this, this.get('email'), this._forgotPasswordCallbacks());

			forgotPasswordService.perform();
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
		loginCallbacks.addError(this._handleLoginCredentialError);
		loginCallbacks.addNeedsTwoFactor(this._showTwoFactorModal);
		loginCallbacks.addSuccess(this._transitionToNextRoute);

		return loginCallbacks;
	},

	_forgotPasswordCallbacks: function() {
		var forgotPasswordCallbacks = new BasicServiceCallbacks();

		forgotPasswordCallbacks.addFailedValidation(this._handleValidationError);
		forgotPasswordCallbacks.addSuccess(this._displayForgotPasswordNotification);

		return forgotPasswordCallbacks;
	},

	_handleValidationError: function(validationErrors) {
		this._setRequestErrors(validationErrors);
	},

	_handleLoginCredentialError: function(credentialErrors) {
		console.log("credentialErrors: ", credentialErrors);
		this._setRequestErrors(credentialErrors);
	},

	_transitionToNextRoute: function(){
		var previousTransition = this.get('previousTransition');

		if (Ember.isNone(previousTransition)){
			this.transitionToRoute('dashboard');
		}
		else{
			this._retryPreviousTransition();
		}
	},

	_showTwoFactorModal: function() {
		this.transitionToRoute('/verification/' + this.get('email'));
	},

	_displayForgotPasswordNotification: function() {
		$(document).foundation('reveal', 'close');
		alertify.success(Ember.I18n.translations.notifications.forgot_password_email_sent);
	},

	_retryPreviousTransition: function() {
		var previousTransition = this.get('previousTransition');
		this.set('previousTransition', null);
		previousTransition.retry();
	},

	notificationForgotPassword: null,
	previousTransition: null
});
