EmptyValidator = {
	isEmpty: function(value) {
		return Ember.isNone(value) || String(value).trim().length === 0;
	}
};
