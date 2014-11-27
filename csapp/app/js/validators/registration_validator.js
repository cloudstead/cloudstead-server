RegistrationValidator = {
	validatableFields:  ["firstName", "lastName", "email", "password", "password2", "mobilePhone",
		"mobilePhoneCountryCode", "tos", "activationCode"],
	_generateResponse: function() {
		return {
			errors: {},
			hasFailed: function() {
				var self = this;
				var failed = false;
				RegistrationValidator.validatableFields.forEach(function(field) {
					failed = self.errors[field] ? true : failed;
				});
				return failed;
			}
		};
	},

	validate: function(registrationData){
		var response = RegistrationValidator._generateResponse();

		RegistrationValidator.validatableFields.forEach(function(field) {
			response.errors[field] = PresenceValidator.validate(registrationData[field]).errors.presence;
		});

		var password_validation =
			PasswordValidator.validate(registrationData['password'], registrationData['password2']);

		if (password_validation.hasFailed()){
			response.errors['password'] = password_validation.errors.password;
			response.errors['password2'] = password_validation.errors.passwordConfirm;
		}

		response.errors.email = EmailValidator.validate(registrationData['email']).errors.email;

		response.errors.mobilePhone = NumberValidator.validate(registrationData['mobilePhone']).errors.number;

		return response;
	}
};
