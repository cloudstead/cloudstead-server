Ember.Test.registerHelper("should_see_admin_home_page", function(app, message) {
	message = default_string(message, "Should see cloudstead admin home page.");

	equal(
		find('.admin_home_tab').text().trim(),
		Em.I18n.translations.sections.admin.your_cloudsteads,
		message
	);
});

Ember.Test.registerHelper("should_see_admin_details_page", function(app, message) {
	message = default_string(message, "Should see cloudstead admin details page.");

	equal(
		find('.update_details').text().trim(),
		Em.I18n.translations.forms.admin.update,
		message
	);
});
