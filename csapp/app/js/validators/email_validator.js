EmailValidator = {
	validate: function(email){
		var response = {
			errors: {
				email: null
			},
			hasFailed: function() {
				return !Ember.isNone(this.errors.email);
			}
		};

		var error_msg = locate(Em.I18n.translations, 'errors');
		var pattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

		if ((email.trim() === '') || (!email)){
			response.errors.email = error_msg.field_required;
		}else if(!pattern.test(email)){
			response.errors.email = error_msg.email_invalid;
		}

		return response;
	}
};
