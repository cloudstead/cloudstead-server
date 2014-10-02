App.ApplicationView = Ember.View.extend({
    initFoundation: function () {
        Ember.$(document).foundation();  
    }.on('didInsertElement')
});

App.NavbarView = Ember.View.extend({
	authStatus: sessionStorage.getItem('api_token')
	});