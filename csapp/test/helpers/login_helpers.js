Ember.Test.loginFields = [
	".email_input",
	".password_input"
];

Ember.Test.registerHelper("should_see_signin_form", function(app, message) {
	message = default_string(message, "Should see the signin form.");

	hasElements(Ember.Test.loginFields, "Should see the signin form.");
});

Ember.Test.registerHelper("fill_in_login_form", function(app, userData) {
	Ember.Test.loginFields.forEach(function(field){
		fillIn(field, userData[field]);
	});
});

Ember.Test.registerHelper("submit_login_form", function(app, userData) {
	fill_in_login_form(userData);
	click("#confirm_sign_in");
});

Ember.Test.registerHelper("login_user", function(app, token, accountInfo) {
	token = default_string(token, "test-api-token");
	accountInfo = default_object(accountInfo, adminProfileResponse());

	sessionStorage.removeItem('api_token');

	sessionStorage.setItem('api_token', token);
	sessionStorage.setItem('active_admin', JSON.stringify(accountInfo));
});
