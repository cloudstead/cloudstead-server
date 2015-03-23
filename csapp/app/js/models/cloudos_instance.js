App.CloudosInstance = Ember.Object.extend({
	isInInitialState: function() {
		return (this.get('state') === 'initial') ? true : false;
	}.property(),

	isInDestroyingState: function() {
		return (this.get('state') === 'destroying') ? true : false;
	}.property(),
});

App.CloudosInstance.reopenClass({
	all: Ember.ArrayProxy.create({content: []}),

	createFromArray: function(array) {
		array.forEach(function(item) {
			App.CloudosInstance.all.pushObject(App.CloudosInstance.create(item));
		});

		return App.CloudosInstance.all;
	}
});
