App.ApplicationController = Ember.ObjectController.extend({
	//	api_token: sessionStorage.getItem('api_token'),
	//	active_account: Api.json_safe_parse(sessionStorage.getItem('active_account')),
	//	actions: {
	//		'select_app': function (app_name) {
	//			this.transitionTo('app', App.app_model(app_name));
	//		}
	//	}
	authStatus: null,

	//isHome: true,

	refreshAuthStatus: function() {
		this.set('authStatus', sessionStorage.getItem('api_token'));
	},

	isHome: function() {
		return this.get('currentPath') === 'index';
  }.property('currentPath')

});
