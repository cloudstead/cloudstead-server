App.AdminHomeController = Ember.ObjectController.extend({
	cloudosInstances: function(){
		var result = Api.list_cloudos_instances();
		try {
			var uuid = result[0]["uuid"];
			return Api.list_cloudos_instances();
		}catch(e){
			return null;
		}

	}.property(),
	actions: {
		doNewCloudOs: function () {

			var cloudOsRequest = this.get('cloudOsRequest');
			var validate = this.validateName(cloudOsRequest.name);
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

			var status = Api.new_cloud_os(cloudOsRequest);
			if (status) {
				this.send('openModal','cloudOsCreation', this.get('model') );
			} else {
				alert(locate(Em.I18n.translations, status.errorMessageKey));
			}
		},
		deleteInstance: function(instanceName){
			var r = confirm("Are you sure you want to delete cloudstead "+instanceName+" ?");
			if (r === true) {
				result = Api.delete_cloudos_instance(instanceName);

					if (result) {
						alert('Cloudstead ' + instanceName + 'is successfully deleted');
						location.reload();
					} else {
						console.log('not deleted');
					}

			} else {
					console.log('action canceled');
			}
		},
		close: function() {
			return this.send('closeModal');
		},
		addMoreClouds:function(){
			this.set('isAddCloudsEnabled',!this.get('isAddCloudsEnabled'));
		}
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
	isAddCloudsEnabled:function(){
		if (this.get('cloudosInstances')){
			return false;
		}
		else{
			return true;
		}
	}.property('cloudosInstances')
});
