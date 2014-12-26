Ember.Test.registerHelper("should_see_index_page", function(app) {

	var hasSingIn = pageHasElementWithText('a', Em.I18n.translations.sections.index.signin_button);
	var hasSingUp = pageHasElementWithText('a', Em.I18n.translations.sections.index.signup_button);

	ok(hasSingIn && hasSingUp, "Should see the index page");
});
