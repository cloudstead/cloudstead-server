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
		App.CloudosInstance.all.clear();
		array.forEach(function(item) {
			App.CloudosInstance.all.pushObject(App.CloudosInstance.create(item));
		});

		return App.CloudosInstance.all;
	},

	addNew: function(instance) {
		var new_instance = App.CloudosInstance.create(instance);
		App.CloudosInstance.all.pushObject(new_instance);
		return new_instance;
	},

	clearAll: function() {
		App.CloudosInstance.all.clear();
	},

	findByName: function(instance_name) {
		return App.CloudosInstance.all.find(function(instance) {
			return instance.get("name") === instance_name;
		});
	}
});
