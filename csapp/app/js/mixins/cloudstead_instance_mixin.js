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
		deleteInstance: function(instance){
			var self = this;
			var trans = Em.I18n.translations.sections.admin.dialogs;
			alertify.defaults.theme.ok = "tiny";
			alertify.defaults.theme.cancel = "button-hollow tiny";
			alertify.defaults.glossary.title = 'Are you sure you want to destroy this cloud?';
			//show as confirm
			alertify.confirm(document.createElement('div'), function(){
				result = App.CloudosInstance.destroy(instance);
				if (result) {
					alertify.success(trans.info_delete_pre_name + instance.get('name') + trans.info_delete_post_name);
					self.send("transitionToDashboard");
				} else {
					alertify.error(trans.error_delete_pre_name + instance.get('name') + trans.error_delete_post_name);
				}
			// alertify.success('Accepted');
			},function(){
				//alertify.error('action canceled');
			}).setting('labels', {'ok':'Yes', 'cancel': trans.forms.admin.cancel_button});
		},
		close: function() {
			return this.send('closeModal');
		},
		addMoreClouds:function(){
			this.set('isAddCloudsEnabled',!this.get('isAddCloudsEnabled'));
		}
	}
});
