ForgotPasswordService = function(email, forgotPasswordCallbacks) {
	this.email = email;
	this.callbacks = forgotPasswordCallbacks;
};

ForgotPasswordService.prototype.forget = function() {
	var validation = EmailValidator.validate(this.email);
	var forgotPasswordResponse = new ForgotPasswordResponse('empty_response', {});

	if (validation.hasFailed()){
		forgotPasswordResponse = new ForgotPasswordValidationErrorResponse(
			validation.errors, this.callbacks.failedValidation);
	}
	else{
		Api.forgot_password(this.email);

		forgotPasswordResponse = new ForgotPasswordResponse("success", this.callbacks.success);
	}

	return forgotPasswordResponse;
};

ForgotPasswordService.prototype.handleResponse = function(self, forgotPasswordResponse) {
	return forgotPasswordResponse.resolve(self);
};



ForgotPasswordResponse = function(payload, callback){
	this.payload = payload;
	this.callback = callback;
};

ForgotPasswordResponse.prototype.resolve = function(self) {
	return this.callback.call(self);
};



ForgotPasswordValidationErrorResponse = function(payload, callback){
	ForgotPasswordResponse.call(this, payload, callback);
};

ForgotPasswordValidationErrorResponse.prototype.resolve = function(self) {
	return this.callback.call(self, this.payload);
};



ForgotPasswordCallbacks = function(){
	this.failedValidation = function(validationErrors) {
		return validationErrors;
	};

	this.success = function(credentialErrors) {
		console.log('Forgot Password Successful');
	};
};

ForgotPasswordCallbacks.prototype.addFailedValidation = function(callback) {
	this.failedValidation = callback;
};

ForgotPasswordCallbacks.prototype.addSuccess = function(callback) {
	this.success = callback;
};
