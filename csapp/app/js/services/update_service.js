UpdateService = function(subject, updateData, updateCallbacks) {
	BasicService.call(this, subject, updateData, updateCallbacks);
	this.updateData = this.serviceData;
};

UpdateService.prototype = new BasicService();

UpdateService.prototype.perform = function() {
	var response = new BasicNoResponse();
	response = this._do(response, this.validate);
	response = this._do(response, this.update);
	return this.handleResponse(response);
};

UpdateService.prototype.validate = function() {
	var validation = UpdateValidator.validate(this.updateData);
	var response = new BasicNoResponse();

	if (validation.hasFailed()){
		response = new BasicPayloadResponse(validation.errors, this.callbacks.failedValidation);
	}

	return response;
};

UpdateService.prototype.update = function() {
	var updateResponse = new BasicNoResponse();

	var response = Api.update_admin_profile(this.updateData);

	if (API_RESPONSE_STATUS.isSuccess(response.status)){
		updateResponse = new BasicEmptyResponse(this.callbacks.success);
	}
	else if (API_RESPONSE_STATUS.isError(response.status)){
		var proccessedResponse = this._proccessRegistrationError(response);

		updateResponse =
			new BasicPayloadResponse(proccessedResponse, this.callbacks.failedWithError);
	}

	return updateResponse;
};

UpdateService.prototype._proccessRegistrationError = function(registrationResponse) {
	var result = {
		errors: {}
	};
	if (registrationResponse !== undefined){
		var response = registrationResponse.responseJSON;

		response.forEach(function(error){
			var errorMessage = error.message;

			if(ErrorResponseMessages.doesKnowError(errorMessage)){
				var errorType = ErrorResponseMessages[errorMessage].errorType;
				result.errors[errorType] = ErrorResponseMessages[errorMessage].message;
			}
		});
	}
	return result;
};



UpdateCallbacks = function(){
	this.failedValidation = function(validationErrors) {
		return validationErrors;
	};

	this.failedWithError = function(error) {
		return error;
	};

	this.success = function(credentialErrors) {
		console.log('Registration Successful');
	};
};

UpdateCallbacks.prototype.addFailedValidation = function(callback) {
	this.failedValidation = callback;
};

UpdateCallbacks.prototype.addError = function(callback) {
	this.failedWithError = callback;
};

UpdateCallbacks.prototype.addSuccess = function(callback) {
	this.success = callback;
};

