App.LoginController = Ember.ObjectController.extend({
	actions: {
		doLogin: function () {

			DeviceCookieGenerator.generate();

			var loginData = {
				name: this.get('name'),
				password: this.get('password'),
				deviceId: getCookie("deviceId"),
				deviceName: getCookie("deviceName")
			};

			var loginCallbacks = {
				failedValidation: this._handleLoginValidationError,
				failedCredentials: this._handleLoginCredentialError,
				needsTwoFactor: this._showTwoFactorModal,
				success: this._transitionToNextRoute
			};

			var loginService = new LoginService(loginData, loginCallbacks);

			loginService.handleResponse(this, loginService.login());
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

	_handleLoginValidationError: function(validationErrors) {
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

	_handleLoginCredentialError: function(validationErrors) {
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
			username: this.get('name'),
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
	requestMessages: null,
	notificationForgotPassword: null
});
