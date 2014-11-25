PresenceValidator = {
	validate: function(fieldValue){
		var response = {
			errors: {
				presence: null
			},
			hasFailed: function() {
				return !Ember.isNone(this.errors.presence);
			}
		};

		var error_msg = locate(Em.I18n.translations, 'errors');

		if ((!fieldValue) || (fieldValue.trim() === '')){
			response.errors.presence = error_msg.field_required;
		}

		return response;
	}
};
