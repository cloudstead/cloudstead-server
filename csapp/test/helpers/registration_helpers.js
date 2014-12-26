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

Ember.Test.adminHomePageElements = ["#admin_home"];

Ember.Test.twoFactorVerificationFields = [".two_factor_code"];

Ember.Test.registerHelper("should_see_signup_form", function(app) {
	hasElements(Ember.Test.registrationFields, "Should see the signup form.");
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
