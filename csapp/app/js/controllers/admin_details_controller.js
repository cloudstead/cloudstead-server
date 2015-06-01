App.AdminDetailsController = App.CloudOSController.extend(App.CountriesMixin, {
	actions:{
		updateAdminAccount: function(){
			this.set("mobilePhoneCountryCode", this.get("mobilePhoneCountry").code);

			var updateService =
				new UpdateService(this, this._updateData(), this._updateCallbacks());

			updateService.perform();

			//TODO - nicer messaging, update password part
		},

		deleteAdminAccount: function () {
			console.log("this now need to perform credentials check, and delete account if credentials are good");
			this._doLogin();
		}
	},

	_updateData: function() {
		sessionData = JSON.parse(sessionStorage.active_admin);

		return {
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
			tos: true,
			activationCode: "foo"
		};
	},

	_updateCallbacks: function() {
		var updateCallbacks = new BasicServiceCallbacks();

		updateCallbacks.addFailedValidation(this._handleValidationError);
		updateCallbacks.addError(this._handleUpdateError);
		updateCallbacks.addSuccess(this._handleUpdateSuccess);

		return updateCallbacks;
	},

	_handleValidationError: function(validationErrors) {
		this._setRequestErrors(validationErrors);
	},

	_handleUpdateError: function(updateErrors) {
		this._setRequestMessage(updateErrors);
	},

	_handleUpdateSuccess: function() {
		this._resetRequestMessage();
		alert('Account updated successfully.');
	},

	_doLogin: function() {
		var self = this;
		var loginService = new LoginService(self, self._loginData(), self._loginCallbacks());
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
		loginCallbacks.addError(this._handleLoginCredentialError);
		loginCallbacks.addSuccess(this._successLogin);

		return loginCallbacks;
	},

	_handleLoginCredentialError: function(credentialErrors) {
		console.log("credentialErrors: ", credentialErrors);
		this._setRequestErrors(credentialErrors);
	},

	_successLogin: function(){
		var error_msg = locate(Em.I18n.translations, 'errors');
		console.log("email and pass are good, try logout and transition to /");
		var response = Api.delete_account();
		console.log("response => ", response);
		if(response.statusCode === 200){
			this.transitionToRoute('/logout');
		}
		else{
			alertify.error(error_msg.server_error);
		}
	}
});
