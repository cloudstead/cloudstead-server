LoginValidator = {
	validate: function(email, password){
		var response = {
			errors: {
				name: null,
				password:null
			},
			hasFailed: function() {
				return (this.errors.name) || (this.errors.password);
			}
		};

		response.errors.name = EmailValidator.validate(email).errors.email;
		response.errors.password = PresenceValidator.validate(password).errors.presence;

		return response;
	}
};
