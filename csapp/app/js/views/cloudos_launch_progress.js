App.CloudosLaunchProgressView = Ember.View.extend({
	startWatching: function () {
		Ember.$(document).foundation();
		var controller = this.get('controller');
		console.log("cm: ", controller.get('model'));
		controller.getStatusData();
	}.on('didInsertElement'),
});
