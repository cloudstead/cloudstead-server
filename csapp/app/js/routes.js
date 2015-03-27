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
	this.resource('adminDetails', { path: '/profile' });

	this.resource('resetPassword', { path: '/reset_password/:token' });
	this.resource('twoFactorVerification', { path: '/verification/:email' });
	this.route('activate_account', { path: '/activate/:activation_token' });
	this.route('dashboard');
	this.route('new_cloudstead');
	this.route('cloudstead_details', { path: '/cloudstead/:cloudstead_name' });

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
	},

	actions: {
		transitionToDashboard: function() {
			this.transitionTo('dashboard');
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

App.DashboardRoute = App.ProtectedRoute.extend({
	model: function () {
		return App.CloudosInstance.createFromArray(Api.list_cloudos_instances());
	},

	afterModel: function (model, transition) {
		if (Ember.isEmpty(model)){
			// render new cloudstead page
			this.transitionTo('new_cloudstead');
		} else if (model.content.length === 1) {
			// render instance details page
			this.transitionTo('cloudstead_details', model.get('firstObject.name'));
		}
	}
});

App.CloudsteadDetailsRoute = App.ProtectedRoute.extend({
	model: function (params) {
		return App.CloudosInstance.addNew(Api.cloud_os_details(params.cloudstead_name));
	},

	actions: {
		transitionToDashboard: function() {
			this.transitionTo('dashboard');
		}
	}
});

App.NewCloudsteadRoute = App.ProtectedRoute.extend({
	cloudstead_tranlations: function(){
		return Em.I18n.translations.cloudstead_info;
	}.property(),

	model: function () {
		return {
			name: '',
			edition: 'starter',
			appBundle: 'basic',
			region: 'us_west'
		};
	},

	setupController: function(controller, model) {
		this._super(controller, model);
		this.populateRegions(controller, model);
		this.populateEditions(controller, model);
		this.populateBundles(controller, model);
	},

	populateRegions: function(controller, model) {
		var self = this;
		var regions = Api.get_cloudstead_regions();
		console.log("regions: ", regions);

		var reg = regions.map(function(region) {
			return {
				label: self.get('cloudstead_tranlations').regions[region],
				value: region
			};
		});

		console.log("regs: ", reg);

		controller.set("regionList", reg);
		model.region = reg[0];
	},

	populateEditions: function(controller, model) {
		var self = this;
		var editions = Api.get_cloudstead_editions();
		console.log("editions: ", editions);

		var eds = editions.map(function(edition) {
			return {
				label: self.get('cloudstead_tranlations').editions[edition],
				value: edition
			};
		});

		controller.set("editionList", eds);
		model.edition = eds[0];
	},

	populateBundles: function(controller, model) {
		var self = this;
		var bundles = Api.get_cloudstead_bundles();
		console.log("bundles: ", bundles);

		var bdl = bundles.map(function(bundle) {
			return {
				label: self.get('cloudstead_tranlations').bundles[bundle],
				value: bundle
			};
		});

		controller.set("bundleList", bdl);
		model.appBundle = bdl[0];
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
