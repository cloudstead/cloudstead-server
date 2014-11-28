App.ResetPasswordController = Ember.ObjectController.extend({
	actions:{
		doResetPassword: function () {
			var resetPasswordService =
				new PasswordResetService(this, this._resetData(), this._resetCallbacks());

			resetPasswordService.perform();
		}
	},

	_resetData: function() {
		return {
			token: this.get('model').token,
			password: this.get('password'),
			confirm: this.get("passwordConfirm")
		};
	},

	_resetCallbacks: function() {
		var resetCallbacks = new PasswordResetCallbacks();

		resetCallbacks.addFailedValidation(this._handleChangeAccountPasswordErrors);
		resetCallbacks.addSuccess(this._resetSuccessful);
		resetCallbacks.addFailedReset(this._resetSuccessful);

		return resetCallbacks;
	},

	_resetSuccessful: function() {
		var delayInSeconds = 3;

		this._setResetNotificationTo(this._delayMessage(delayInSeconds));
		this._delayedTransitionTo("login", delayInSeconds);
	},

	_handleChangeAccountPasswordErrors: function(errors) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: {
					"status": 'error',
					"api_token" : null,
					"errors": errors
				}
			})
		);
	},

	_delayedTransitionTo: function(routeName, delayInSeconds){
		TIMER_STEP_IN_SECONDS = 1;
		var passedInSeconds = 0;
		var self = this;

		var interval = setInterval(
			function() {
				passedInSeconds += 1;
				self._setResetNotificationTo(
					self._delayMessage(parseInt(delayInSeconds - passedInSeconds, 10))
				);

				if (passedInSeconds >= delayInSeconds){
					clearInterval(interval);
					self.transitionToRoute(routeName);
				}
			},
			Timer.s2ms(TIMER_STEP_IN_SECONDS)
		);
	},

	_delayMessage: function(delayInSeconds) {
		return Ember.I18n.translations.notifications.reset_password_successful +
			" " + delayInSeconds + "s.";
	},

	_setResetNotificationTo: function(message){
		this.set("notificationResetPassword", message);
	}
});
