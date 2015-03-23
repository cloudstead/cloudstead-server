App.CloudosLaunchStatus = Ember.Object.extend({

	chef_percent_regex: /\{setup\.cheffing\.percent_done_(\d{1,3})\}/,

	launch_stages: {
		"{setup.instanceLookup}": 1,
		"{setup.startingMasterInstance}": 2,
		"{setup.creatingDnsRecord}": 12,
		"{setup.createAppStoreAccount}": 16,
		"{setup.generatingSendgridCredentials}": 18,
		"{setup.cheffing}": 20,
		"{setup.cheffing.percentage}": 90,
		"{setup.completed}": 95,
		"{setup.success}": 100
	},

	hasNoError: function() {
		return Ember.isNone(this.get('errorMessageKey'));
	},

	lastStatus: function() {
		return this.get('history')[this.get('history').length -1];
	},

	isSuccess: function() {
		return this.lastStatusMessage() === '{setup.success}';
	},

	lastStatusMessage: function() {
		return this.lastStatus()["messageKey"];
	},

	isInChefPercetageStage: function() {
		return this.chef_percent_regex.test(this.lastStatusMessage());
	},

	isNotInPhase: function(phase) {
		return phase !== this.lastStatusMessage();
	},

	chefPercentageMessage: function() {
		return "{setup.cheffing}";
	},

	getLaunchPercent: function() {
		return this.launch_stages[this.lastStatusMessage()];
	},

	getChefPrecent: function(cheff_total_part) {
		var current_cheff_percent = parseInt(this.chef_percent_regex.exec(this.lastStatusMessage())[1], 10);
		return Math.floor(this.launch_stages["{setup.cheffing}"] + (current_cheff_percent * cheff_total_part/100));
	},


});
