App.ActivateAccountView = Ember.View.extend({
	redirectToIndex: function () {
		TIMER_STEP_IN_SECONDS = 1;
		var passedInSeconds = 0;
		var self = this;
		var controller = this.get('controller');
		var delayInSeconds = 3;

		if (controller.get('isAccountActivated')) {
			var interval = setInterval(
				function() {
					console.log("timing....");
					passedInSeconds += 1;

					var timeLeft = delayInSeconds - passedInSeconds;

					controller.set(
						"transitionNotification",
						Ember.I18n.translations.notifications.activate_account_successful +
						" " + timeLeft + "s."
						);

					if (timeLeft <= 0){
						clearInterval(interval);

						self.controller.send("transitionToIndex");
					}
				},
				Timer.s2ms(TIMER_STEP_IN_SECONDS)
			);
		}
	}.on('didInsertElement'),
});
