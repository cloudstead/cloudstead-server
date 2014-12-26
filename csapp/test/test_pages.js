module("Registered User", {

	setup: function() {
		// before each test, ensure the application is ready to run.
		Ember.run(App, App.advanceReadiness);
		sessionStorage.clear();
		login_user();
	},

	teardown: function() {
		// reset the application state between each test
		App.reset();
	}
});

test("User clicks the 'Account details' link", function() {
	visit("/adminHome");

	click(find("a:contains('" + Em.I18n.translations.sections.admin.account_details + "')"));

	andThen(function() {
		should_see_admin_details_page();
	});
});

test("User clicks the 'Youre cloudsteads' link", function() {
	visit("/adminDetails");

	click(find("a:contains('" + Em.I18n.translations.sections.admin.your_cloudsteads + "')"));

	andThen(function() {
		should_see_admin_home_page();
	});
});
