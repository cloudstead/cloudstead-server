module("User login", {

	setup: function() {
		// before each test, ensure the application is ready to run.
		Ember.run(App, App.advanceReadiness);
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
		should_see_starting_page();
	});
});
