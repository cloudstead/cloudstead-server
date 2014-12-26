module("User login", {

	setup: function() {
		// before each test, ensure the application is ready to run.
		Ember.run(App, App.advanceReadiness);
		sessionStorage.clear();
	},

	teardown: function() {
		// reset the application state between each test
		App.reset();
	}
});

test("User clicks the 'Sign-in' button", function() {
	visit("/");
	click(".signin_link");

	andThen(function() {
		should_see_signin_form();
	});
});

test("User cancels login", function() {
	visit("/login");
	click("#cancel_sign_in");

	andThen(function() {
		should_see_index_page();
	});
});

test("Unverified user logs in", function() {
	visit("/login");

	submit_login_form(Ember.Test.firstUserData);

	andThen(function() {
		should_see_two_factor_verification_form(
			"After login form submission should see the two factor verification form");
	});

	submit_two_factor_verification_code();

	andThen(function() {
		should_see_admin_home_page(
			"After two factor verification submission should see the admin home page");
	});
});

test("Verified user logs in", function() {
	visit("/login");

	submit_login_form(Ember.Test.verifiedUserData);

	andThen(function() {
		should_see_admin_home_page();
	});
});

test("Unregistered user tries to visit a protected page", function() {
	visit("/adminDetails");

	andThen(function() {
		should_see_signin_form("Should be redirected to login page.");
	});

	submit_login_form(Ember.Test.verifiedUserData);

	andThen(function() {
		should_see_admin_details_page("After login should see the previously requested page.");
	});
});
