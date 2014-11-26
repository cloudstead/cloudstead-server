LoginValidator = {
	validate: function(email, password){
		var response = {
			errors: {
				email: null,
				password:null
			},
			hasFailed: function() {
				return (this.errors.email) || (this.errors.password);
			}
		};

		response.errors.email = EmailValidator.validate(email).errors.email;
		response.errors.password = PresenceValidator.validate(password).errors.presence;

		return response;
	}
};
