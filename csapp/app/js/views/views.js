App.ApplicationView = Ember.View.extend({
    initFoundation: function () {
        Ember.$(document).foundation();
    }.on('didInsertElement')
});

App.IndexView = Ember.View.extend({
    initFoundation: function () {
        Ember.$(document).foundation();
    }.on('didInsertElement'),
	initEvents: function(){
		// ON SIGN UP, ALL INPUT FIELDS NEED TO BE EMPTY
		$(document).on('opened.fndtn.reveal', '#signupModal', function () {
			var modal = $(this);
			$.each(modal.find("input"), function(index, item){
				if($(item).attr('type') === "text" || $(item).attr('type') === "password"){
					$(item).val("");
				}

			});
		});
	}.on('didInsertElement')
});

App.AdminDetailsView = Ember.View.extend({
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
