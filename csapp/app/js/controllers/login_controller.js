App.LoginController = App.CloudOSController.extend({
	actions: {
		doLogin: function () {

			DeviceCookieGenerator.generate();

			var loginService = new LoginService(this._loginData(), this._loginCallbacks());

			loginService.handleResponse(this, loginService.login());
		},

		close: function() {
				return this.transitionToRoute('index');
		},

		doForgotPassword: function() {
			var validate = EmailValidator.validate(this.get('email'));

			if (validate.hasFailed()){
				this._setRequestErrors(validate.errors);
				return false;
			}
			else{
				Api.forgot_password(this.get('email'));

				this.set(
					'notificationForgotPassword',
					Ember.I18n.translations.notifications.forgot_password_email_sent
				);
			}
		}
	},

	_loginData: function() {
		return {
			name: this.get('email'),
			password: this.get('password'),
			deviceId: getCookie("deviceId"),
			deviceName: getCookie("deviceName")
		};
	},

	_loginCallbacks: function() {
		return {
			failedValidation: this._handleLoginValidationError,
			failedCredentials: this._handleLoginCredentialError,
			needsTwoFactor: this._showTwoFactorModal,
			success: this._transitionToNextRoute
		};
	},

	_handleLoginValidationError: function(validationErrors) {
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
		this.send('closeModal');
		this.set('model',{
			username: this.get('email'),
			deviceId: getCookie("deviceId"),
			deviceName: getCookie("deviceName"),
			isRegister: false
		});
		this.send('openModal','twoFactorVerification', this.get('model') );
	},

	previousTransition: null,

	_retryPreviousTransition: function() {
		var previousTransition = this.get('previousTransition');
		this.set('previousTransition', null);
		previousTransition.retry();
	},

	name:'',
	password:'',
	notificationForgotPassword: null
});
