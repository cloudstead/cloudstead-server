App.ApplicationView = Ember.View.extend({
    initFoundation: function () {
        Ember.$(document).foundation();
    }.on('didInsertElement')
});

App.NavbarView = Ember.View.extend({
	authStatus: sessionStorage.getItem('api_token')
	});

App.ModalView = Ember.View.extend({
	keyPress: function(evt) {
		if (evt.key === "Esc") {
			this.get('controller').send("close");
		}
	}
});

App.RegistrationView = App.ModalView.extend({});

App.LoginView = App.ModalView.extend({});
