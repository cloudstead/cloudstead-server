Ember.Test.registrationFields = [
	".first_name_input",
	".last_name_input",
	".email_input",
	".mobile_phone_input",
	".password_input",
	".password_confirm_input",
	".tos_checkbox",
	".activation_code"
];

Ember.Test.loginFields = [
	".email_input",
	".password_input"
];

Ember.Test.adminHomePageElements = ["#admin_home"];

Ember.Test.twoFactorVerificationFields = [".two_factor_code"];

Ember.Test.registerHelper("should_see_index_page", function(app){
	var content = [
		"#sign_up_link",
		"#sign_in_link"
	];

	hasElements(content, "Should see the index page");
});

Ember.Test.registerHelper("should_see_signup_form", function(app) {
	hasElements(Ember.Test.registrationFields, "Should see the signup form.");
});

Ember.Test.registerHelper("should_see_starting_page", function(app) {
	equal(
		find('a.signin_link').text().trim(),
		Em.I18n.translations.sections.login,
		"Should see the starting page."
	);
});

Ember.Test.registerHelper("should_not_see_signup_form", function(app) {
	notHasElements(Ember.Test.registrationFields, "Should not see the signup form.");
});

Ember.Test.registerHelper("fill_in_registration_form", function(app, userData) {
	Ember.Test.registrationFields.forEach(function(field){
		fillIn(field, userData[field]);
	});
	find(".mobile_phone_country_code").val('1');
	find(".mobile_phone_country_code").change();
	click(".tos_checkbox");
});

Ember.Test.registerHelper("submit_registration_form", function(app, userData) {
	fill_in_registration_form(userData);
	click("#confirm_sign_up");
});

Ember.Test.registerHelper("should_see_two_factor_verification_form", function(app, message) {
	message = default_string(message, "Should see two factor verification form.");

	hasElements(Ember.Test.twoFactorVerificationFields, message);
});

Ember.Test.registerHelper("should_see_signin_form", function(app) {
	hasElements(Ember.Test.loginFields, "Should see the signin form.");
});

Ember.Test.registerHelper("fill_in_signin_form", function(app, userData) {
	Ember.Test.loginFields.forEach(function(field){
		fillIn(field, userData[field]);
	});
});

Ember.Test.registerHelper("should_see_admin_home_page", function(app, message) {
	message = default_string(message, "Should see cloudstead admin home page");

	equal(
		find('.admin_home_tab').text().trim(),
		Em.I18n.translations.sections.admin.your_cloudsteads,
		message
	);
});

Ember.Test.registerHelper("submit_two_factor_verification_code", function(app) {
		fillIn(".two_factor_code", "0000000");
		click(".verification_submit");
});
