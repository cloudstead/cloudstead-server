ForgotPasswordService = function(subject, email, forgotPasswordCallbacks) {
	BasicService.call(this, subject, email, forgotPasswordCallbacks);
	this.email = this.serviceData;
};

ForgotPasswordService.prototype = new BasicService();

ForgotPasswordService.prototype.perform = function() {
	var response = new BasicNoResponse();
	response = this._do(response, this.validate);
	response = this._do(response, this.forget);
	return this.handleResponse(response);
};

ForgotPasswordService.prototype.validate = function() {
	var validation = EmailValidator.validate(this.email);
	var response = new BasicNoResponse();

	if (validation.hasFailed()){
		response = new BasicPayloadResponse(validation.errors, this.callbacks.failedValidation);
	}

	return response;
};

ForgotPasswordService.prototype.forget = function() {

	Api.forgot_password(this.email);

	return new BasicEmptyResponse(this.callbacks.success);
};
