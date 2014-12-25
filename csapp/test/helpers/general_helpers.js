Ember.Test.registerHelper("default_string", function(app, string, value) {
	if (Ember.isNone(string) || Ember.isEmpty(string)){
		string = value;
	}
	return string;
});

Ember.Test.registerHelper("hasElements", function(app, elements, message) {
	var actual = true;
	var missing = "";

	elements.forEach(function(element){
		if (find(element).length == 0){
			missing = missing.length == 0 ? " Missing: " : missing;
			actual = false;
			missing += element + "; ";
		}
	});

	ok(actual, message + missing);
});

Ember.Test.registerHelper("notHasElements", function(app, elements, message) {
	var actual = true;
	var missing = "";

	elements.forEach(function(element){
		if (find(element).length != 0){
			missing = missing.length == 0 ? " Found: " : missing;
			actual = false;
			missing += element + "; ";
		}
	});

	ok(actual, message + missing);
});