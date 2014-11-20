

App.ApplicationRoute = Ember.Route.extend({
	actions:{
		openModal: function(modalName, model){
			this.controllerFor(modalName).set('model',model);
			return this.render(modalName, {
				into: 'application',
				outlet: 'modal'
			});
		},
		closeModal: function(){
			return this.disconnectOutlet({
				outlet: 'modal',
				parentView: 'application'
			});
		}
	}
});

App.IndexRoute = App.ApplicationRoute;

App.Router.map(function() {
	this.resource('registration');
	this.resource('adminHome');
	this.resource('login');
	this.resource('logout');
	this.resource('cloudOsStatus', { path: '/cloudos/:cloudos_name' } );
	this.resource('about');
	this.resource('contact');
	this.resource('adminDetails');

	this.resource('resetPassword', { path: '/reset_password/:token' });
//	this.resource('settings');
//	this.resource('app', { path: '/app/:app_name' });
});

App.RegistrationRoute = Ember.Route.extend({
	model: function () {
		return {
			email: '',
			password: '',
			password2: '',
            tos: ''
		};
	}
});

App.LoginRoute = Ember.Route.extend({
	model: function () {
		return {
			email: '',
			password: ''
		};
	}
});

App.AdminHomeRoute = Ember.Route.extend({
	model: function () {
		return {
			cloudOsRequest: {
				name: ''
			}
		};
	},
	setupController: function(controller, model) {
		if (!sessionStorage.getItem('api_token')) {
			window.location.replace('/index.html');
		}
		controller.set("content", model);
	}
});

App.AdminDetailsRoute = Ember.Route.extend({
	model: function () {
		var adminData = JSON.parse(sessionStorage.active_admin);
		return Api.get_admin_profile(adminData.uuid);
	}
});

App.CloudOsStatusRoute = Ember.Route.extend({
	model: function (params) {
		var name = params.cloudos_name;
		return {
			name: name,
			status: Api.cloud_os_launch_status(name)
		};
	}
});

App.LogoutRoute = Ember.Route.extend({
	setupController: function(controller, model) {
		// probably want to do a more selective removal of attributes, rather than wiping everything out
		sessionStorage.clear();
		localStorage.clear();
		window.location.replace('/index.html');
	}
});

App.ResetPasswordRoute = Ember.Route.extend({
	model: function (params) {
		return { token : params['token'] };
	}
});
