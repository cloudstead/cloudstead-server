App.CloudOsStatusController = Ember.ObjectController.extend({
	actions: {
		doRelaunchCloudOs: function () {
			var cloudOsRequest = {
				name: this.get('cloudOs.name'),
				edition: this.get('cloudOs.edition'),
				appBundle: this.get('cloudOs.appBundle'),
				region: this.get('cloudOs.region')
			};
			var status = Api.new_cloud_os(cloudOsRequest);

			if (status) {
				this.transitionToRoute('cloudOsStatus', cloudOsRequest.name);
			} else {
				alert(locate(Em.I18n.translations, status.errorMessageKey));
			}
		}
	}
});
