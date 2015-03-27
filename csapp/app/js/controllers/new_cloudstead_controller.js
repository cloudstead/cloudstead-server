App.NewCloudsteadController = Ember.ObjectController.extend({

	actions: {
		doNewCloudOs: function() {
			this.set("region", this.get("selectedRegion.value"));
			this.set("edition", this.get("selectedEdition.value"));
			this.set("appBundle", this.get("selectedBundle.value"));
			var validate = this.validateName(this.get('name'));
			if (validate.cloudOsName) {
				this.set('requestMessages',
						App.RequestMessagesObject.create({
							json: {"status": 'error', "api_token" : null,
								"errors":
									{"cloudOsName": validate.cloudOsName}}
						})
					);
				return false;
			}

			Api.new_cloud_os(this.get("content"));
			this.send('transitionToDashboard');
		},
	},

	validateName: function(cloudOsName){

		var response = {"cloudOsName":null};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var reserved = ["www", "wwws", "http", "https",
						"mail", "email", "mailbox", "mbox", "smtp", "imap", "pop", "pop3",
						"corp", "blog", "news",
						"mobile", "root", "postmaster", "admin",
						"cloudos", "upcloud", "cloudstead"];

		if ((cloudOsName.trim() === '') || (!cloudOsName)){
			response.cloudOsName = error_msg.field_required;
		}else if (cloudOsName.length < 4){
			response.cloudOsName = error_msg.cloudosname_short;
		}
		if ($.inArray(cloudOsName.trim().toLowerCase(), reserved) > -1){
			response.cloudOsName = error_msg.cloudos_reserverd;
		}

		return response;
	},

	regionList: [],

	selectedRegion: function() {
		return this.get("region");
	}.property('region'),

	editionList: [],

	selectedEdition: function() {
		return this.get("edition");
	}.property('edition'),

	bundleList: [],

	selectedBundle: function() {
		return this.get("bundle");
	}.property('bundle'),

});
