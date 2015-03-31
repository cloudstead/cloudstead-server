App.CloudsteadInstanceMixin = Ember.Mixin.create({
	actions: {
		doLaunchCloudOs: function (instanceName) {
			var trans = Em.I18n.translations.sections.admin.dialogs;
			var confirm = window.confirm(trans.confirm_launch_pre_name + instanceName + trans.confirm_launch_post_name);
			if (confirm){
				var launched_cloudos = Api.launch_cloud_os(instanceName);
				console.log("launching: ", launched_cloudos);
				this.send("showHeaderProgressbar", launched_cloudos.cloudOs);
			}
		},
		deleteInstance: function(instanceName){
			var trans = Em.I18n.translations.sections.admin.dialogs;
			var r = confirm( trans.confirm_delete_pre_name + instanceName + trans.confirm_delete_post_name);
			if (r === true) {
				result = App.CloudosInstance.destroy(instanceName);

				if (result) {
					alert(trans.info_delete_pre_name + instanceName + trans.info_delete_post_name);
					this.send("transitionToDashboard");
				} else {
					alert(trans.error_delete_pre_name + instanceName + trans.error_delete_post_name);
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
	}
});
