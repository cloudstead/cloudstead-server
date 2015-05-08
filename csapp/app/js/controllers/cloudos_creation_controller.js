App.CloudOsCreationController = Ember.ObjectController.extend({
	actions:{
		backToAdmin:function(){
			this.send('closeModal');
			location.reload();
		},

		setStatusMessage: function(message) {
			this.set('statusMessage', swapStatusMessage(message));
		},

		updateProgressBar: function(current_percent) {
			$('#progressMeter').css('width', current_percent + '%');
			$(".launch-loading-for-small > #progressMeter").css('width', current_percent + '%');
		},

	},
	getStatusData: function(){
		var cloudos_name = this.get('model.name');
		var result = Api.cloud_os_launch_status(cloudos_name);

		if (!Ember.isNone(result) && !Ember.isNone(result.history)){
			this.set('hasErrorMessage', false);
			this.watchLaunchStatus(cloudos_name);
		}
		else{
			alert("Error fetching status history");
			this.send('hideHeaderProgressbar');
		}

		return result;
	},

	watchLaunchStatus: function(cloudos_name) {
		var self = this;

		var cheff_total_part = 70;
		var current_percent = 0;
		var current_phase = "";


		var statusInterval = setInterval(function(){
				result = App.CloudosLaunchStatus.create(Api.cloud_os_launch_status(cloudos_name));


				if (result.hasNoError()){
					last_status = result.lastStatus();

					if (result.isSuccess()) {

						self.set('statusMessage', swapStatusMessage(result.lastStatusMessage()));
						self.set("isInProgress", false);
						$(".launch-loading-for-small").removeClass("visible").addClass('invisible');
						self.send("hideHeaderProgressbar");
						clearInterval(statusInterval);
					}

					if (result.isInChefPercetageStage()) {

						if (result.isNotInPhase(current_phase)){
							current_phase = result.lastStatusMessage();
							current_percent = result.getChefPrecent(cheff_total_part);
							self.send('setStatusMessage', result.chefPercentageMessage());
						}

					} else {

						current_percent = result.getLaunchPercent();
						self.send('setStatusMessage', result.lastStatusMessage());
					}
					self.send('updateProgressBar', current_percent);

				} else {
					self.send('setStatusMessage', result.lastStatusMessage());
					self.set('hasErrorMessage', true);
					self.set("isInProgress", false);
					clearInterval(statusInterval);
				}
			}, 5000);
	},

	statusMessage : function(){
		var status = this.getStatusData();
		if (status.history){
			var last_status = status.history[status.history.length - 1];
			return swapStatusMessage(last_status["messageKey"]);
		}else{
			return null;
		}
	}.property(),
	isInProgress:true,

	hasErrorMessage: false
});
