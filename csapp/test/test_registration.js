module("User registration", {

	setup: function() {
		// before each test, ensure the application is ready to run.
		Ember.run(App, App.advanceReadiness);
	},

	teardown: function() {
		// reset the application state between each test
		App.reset();
	}
});

test("User clicks the 'Sign-up' button", function() {
	visit("/");
	click(".signup_link");

	andThen(function() {
		should_see_signup_form();
	});
});

test("User cancels registration", function() {
	visit("/registration");
	click("#cancel_sign_up");

	andThen(function() {
		should_see_index_page();
	});
});

test("User exercises registration flow", function() {
	visit("/registration");

	submit_registration_form(Ember.Test.firstUserData);

	andThen(function() {
		should_see_two_factor_verification_form(
			"After registration form submission should see the two factor verification form");
	});

	submit_two_factor_verification_code();

	andThen(function() {
		should_see_admin_home_page(
			"After two factor verification submission should see the admin home page");
	});
});
