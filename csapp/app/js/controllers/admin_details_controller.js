App.AdminDetailsController = App.CloudOSController.extend(App.CountriesMixin, {
	actions:{
		updateAdminAccount: function(){
			this.set("mobilePhoneCountryCode", this.get("mobilePhoneCountry").code);

			var updateService =
				new UpdateService(this, this._updateData(), this._updateCallbacks());

			updateService.perform();

			//TODO - nicer messaging, update password part
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
});
