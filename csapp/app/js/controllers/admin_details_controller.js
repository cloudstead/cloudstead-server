App.AdminDetailsController = Ember.ObjectController.extend({
	actions:{
		updateAdminAccount: function(){
			sessionData = JSON.parse(sessionStorage.active_admin);

			var data = {
				email: this.get('email'),
				name: this.get('email'),
				accountName: this.get('email'),
				firstName: this.get('firstName'),
				lastName: this.get('lastName'),
				mobilePhoneCountryCode : this.get('mobilePhoneCountryCode'),
				mobilePhone: this.get('mobilePhone'),

				admin: sessionData.admin,
				uuid: sessionData.uuid,
				suspended: sessionData.suspended,
				twoFactor: sessionData.twoFactor,
				emailVerified: sessionData.emailVerified,
				tos: true
			};

			var result = Api.update_admin_profile(data);
			if(result){
				alert('Account updated successfully.');
				location.reload();
			}

			//TODO - error checking, nicer messaging, update password part
		}
	},
	tempProperty : "!!!"
});
