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
	this.resource('twoFactorVerification', { path: '/verification/:email' });
	this.route('activate_account', { path: '/activate/:activation_token' });
//	this.resource('settings');
//	this.resource('app', { path: '/app/:app_name' });
});

App.ProtectedRoute = Ember.Route.extend({
	beforeModel: function(transition) {
		if (Ember.isEmpty(sessionStorage.getItem('api_token'))){
			var loginController = this.controllerFor('login');
			loginController.set('previousTransition', transition);
			this.transitionTo('login');
		}
		else{
			this.controllerFor('application').refreshAuthStatus();
		}
	}
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
	},
	beforeModel: function(transition) {
		this._resetLoginControllerMessages();
	},

	_resetLoginControllerMessages: function() {
		var loginController = this.controllerFor('login');
		loginController.set('notificationForgotPassword', null);
		loginController.set('requestMessages', null);
	}
});

App.AdminHomeRoute = App.ProtectedRoute.extend({
	model: function () {
		return {
			cloudOsRequest: {
				name: '',
				edition: 'starter',
				appBundle: 'basic',
				region: 'us_west'
			}
		};
	}
});

App.AdminDetailsRoute = App.ProtectedRoute.extend({
	model: function () {
		var adminData = JSON.parse(sessionStorage.active_admin);
		return Api.get_admin_profile(adminData.uuid);
	}
});

App.CloudOsStatusRoute = App.ProtectedRoute.extend({
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
		this.controllerFor('application').refreshAuthStatus();
		this.transitionTo('index');
	}
});

App.ResetPasswordRoute = Ember.Route.extend({
	model: function (params) {
		return { token : params['token'] };
	}
});

App.TwoFactorVerificationRoute = Ember.Route.extend({
	model: function (params) {
		return {
			username: params.email,
			deviceId: getCookie("deviceId"),
			deviceName: getCookie("deviceName"),
			isRegister: true
		};
	}
});

App.ActivateAccountRoute = Ember.Route.extend({
	model: function(params) {
		return Api.activate_account(params.activation_token);
	},

	actions: {
		transitionToIndex: function() {
			this.transitionTo('index');
		}
	}
});
