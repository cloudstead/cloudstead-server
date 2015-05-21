App.CloudOSController = Ember.ObjectController.extend({
	requestMessages: null,

	_setRequestErrors: function(errorsObject) {
		console.log("errors object b => ", errorsObject);
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
				json: messageObject
			})
		);
	},

	_resetRequestMessage: function() {
		this.set('requestMessages', null);
	}
});
