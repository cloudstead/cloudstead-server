App.TwoFactorVerificationController = Ember.ObjectController.extend({
	actions:{
		close: function() {
			this.send('closeModal');
			return this.transitionToRoute('index');
		},
		verifyFactor: function(){

			var data = {
				name: this.get('model')["username"],
				secondFactor: this.get('verifyCode'),
				deviceId: this.get('model')["deviceId"],
				deviceName: this.get('model')["deviceName"]
			};

			var validationError = Validator.validateTwoFactorVerificationCode(data.secondFactor);

			if (validationError.verificationCode){
				this._handleVerificationCodeError(validationError);
			}
			else{
				this.send('_validateSecondFactorResponse', Api.send_second_factor(data));
			}
		},
		_validateSecondFactorResponse: function(response) {
			if (response.status === 'success'){
				if (response.api_token) {
					this.send('closeModal');
					this.transitionToRoute('adminHome');
				}
			}else{
				// TODO display error messages, requires error message from API.
			}
		}
	},

	isFirst: function(){
		return this.get('model')["isRegister"];
	}.property('model'),

	_handleVerificationCodeError: function(validationError) {
		this.set('requestMessages',
			App.RequestMessagesObject.create({
				json: {
					"status": 'error',
					"api_token" : null,
					"errors": {
						"verifyCode": validationError.verificationCode
					}
				}
			})
		);
	}
});
