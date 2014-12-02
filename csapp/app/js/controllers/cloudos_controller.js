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
	},

	_setRequestMessage: function(messageObject) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: registrationErrors
			})
		);
	},

	_resetRequestMessage: function() {
		this.set('requestMessages', null);
	}
});
