PasswordResetService = function(subject, sersetDate, resetCallbacks) {
	BasicService.call(this, subject, sersetDate, resetCallbacks);
};

PasswordResetService.prototype = new BasicService();

PasswordResetService.prototype.perform = function() {
	var response = new BasicNoResponse();
	response = this._do(response, this.validate);
	response = this._do(response, this.reset);
	return this.handleResponse(response);
};

PasswordResetService.prototype.validate = function() {
	var validation = PasswordValidator.validate(this.serviceData.password, this.serviceData.confirm);
	var response = new BasicNoResponse();

	if (validation.hasFailed()){
		response = new BasicPayloadResponse(validation.errors, this.callbacks.failedValidation);
	}

	return response;
};

PasswordResetService.prototype.reset = function() {
		var resetResponse = new BasicNoResponse();

		var response = Api.reset_password(this.serviceData.token, this.serviceData.password);

		if (API_RESPONSE_STATUS.isSuccess(response.status)) {
			this.registerSession(response.sessionId, JSON.stringify(response.account));
			resetResponse = new BasicEmptyResponse(this.callbacks.success);
		}
		else {
			resetResponse = new BasicPayloadResponse(response, this.callbacks.failedReset);
		}

		return resetResponse;
};

PasswordResetService.prototype.isResetSuccessful = function(response) {
	return API_RESPONSE_STATUS.isSuccess(response.status);
};



PasswordResetCallbacks = function(){
	this.failedValidation = function(validationErrors) {
		return validationErrors;
	};

	this.success = function(credentialErrors) {
		console.log('Reset Password Successful');
	};

	this.failedReset = function(resetErrors) {
		return resetErrors;
	};
};

PasswordResetCallbacks.prototype.addFailedValidation = function(callback) {
	this.failedValidation = callback;
};

PasswordResetCallbacks.prototype.addSuccess = function(callback) {
	this.success = callback;
};

PasswordResetCallbacks.prototype.addFailedReset = function(callback) {
	this.failedReset = callback;
};
