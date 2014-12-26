Ember.Test.registerHelper("should_see_two_factor_verification_form", function(app, message) {
	message = default_string(message, "Should see two factor verification form.");

	hasElements(Ember.Test.twoFactorVerificationFields, message);
});

Ember.Test.registerHelper("submit_two_factor_verification_code", function(app) {
		fillIn(".two_factor_code", "0000000");
		click(".verification_submit");
});
