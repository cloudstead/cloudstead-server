App.CloudOSController = Ember.ObjectController.extend({
	requestMessages: null,

	_setRequestErrors: function(errorsObject) {
		this.set(
			'requestMessages',
			App.RequestMessagesObject.create({
				json: {
					"status": 'error',
					"api_token" : null,
					"errors": errorsObject
				}
			})
		);
	}
});
