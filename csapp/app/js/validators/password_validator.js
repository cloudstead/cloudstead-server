PasswordValidator = {
	MIN_PASSWORD_LENGTH: 8,

	validate: function(password, confirm){
		var response = {
			errors: {
				password: null,
				passwordConfirm: null
			},
			hasFailed: function() {
				return !Ember.isNone(this.errors.password) || !Ember.isNone(this.errors.passwordConfirm);
			}
		};

		var error_msg = locate(Em.I18n.translations, 'errors');

		response.errors.password = PresenceValidator.validate(password).errors.presence;
		response.errors.passwordConfirm = PresenceValidator.validate(confirm).errors.presence;

		if (this._passwordTooShort(password)){
			response.errors.password = error_msg.password_short;
		}

		if (response.hasFailed()){
			return response;
		}

		if (this._passwordsMissmatch(password, confirm)){
			response.errors.password = response.errors.passwordConfirm = error_msg.password_mismatch;
		}

		return response;
	},

	_passwordsMissmatch: function(password, confirm) {
		return password !== confirm;
	},

	_passwordTooShort: function(password) {
		return (password) && (password.length < this.MIN_PASSWORD_LENGTH);
	}
};
