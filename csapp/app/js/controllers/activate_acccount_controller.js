App.ActivateAccountController = Ember.ObjectController.extend({
	isAccountActivated: function() {
		return (this.get('model').statusCode === 200) ? true : false;
	}.property(),

	userMessage: function() {
		return this.get('isAccountActivated') ?
			Em.I18n.translations.activate_account.success_message :
			Em.I18n.translations.activate_account.error_message;
	}.property(),

	transitionNotification: ""
});
