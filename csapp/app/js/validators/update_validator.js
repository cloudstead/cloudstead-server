UpdateValidator = {
	validatableFields:  ["firstName", "lastName", "email", "mobilePhone", "mobilePhoneCountryCode"],
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
		var response = this._generateResponse();

		this.validatableFields.forEach(function(field) {
			response.errors[field] = PresenceValidator.validate(registrationData[field]).errors.presence;
		});

		response.errors.email = EmailValidator.validate(registrationData['email']).errors.email;

		response.errors.mobilePhone = NumberValidator.validate(registrationData['mobilePhone']).errors.number;

		return response;
	}
};
