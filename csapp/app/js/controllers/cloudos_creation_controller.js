App.CloudOsCreationController = Ember.ObjectController.extend({
	actions:{
		backToAdmin:function(){
			this.send('closeModal');
			location.reload();
		}
	},
	getStatusData: function(){
		var self = this;
		var result = Api.cloud_os_launch_status( self.get('model')["cloudOsRequest"]["name"]);
		if (result){
			if (result.history){
				var last_status = result.history[result.history.length-1];
				$('#progressMeter').css('width', (result.history.length * 10) + '%');
			}

			var statusInterval = setInterval(function(){

				result = Api.cloud_os_launch_status(self.get('model')["cloudOsRequest"]["name"]);
				if (result.history){
					last_status = result.history[result.history.length-1];
					$('#progressMeter').css('width', (result.history.length * 10) + '%');
					self.set('statusMessage', swapStatusMessage(last_status["messageKey"]));

					if (result.history.length >= 10){
						self.set('isInProgress', false);
						window.clearInterval(statusInterval);
					}
				}
			}, 5000);
		}
		else{
			alert("Error fetching status history");
			this.send('closeModal');
			location.reload();
		}

		return result;
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
	isInProgress:true
});
