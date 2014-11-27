NumberValidator = {
	validate: function(number){
		var response = {
			errors: {
				number: null
			},
			hasFailed: function() {
				return !Ember.isNone(this.errors.number);
			}
		};

		var error_msg = locate(Em.I18n.translations, 'errors');
		var pattern = /^[0-9]+$/;

		if ((!number) || (number.toString().trim() === '')){
			response.errors.number = error_msg.field_required;
		}else if(!pattern.test(number)){
			response.errors.number = error_msg.not_a_number;
		}

		return response;
	}
};
