// loginData is an instance of `LoginData`.
// loginCallbacks is an instance of `LoginCallbacks`.
LoginService = function(subject, loginData, loginCallbacks) {
	BasicService.call(this, subject, loginData, loginCallbacks);
	this.loginData = this.serviceData;
};

LoginService.prototype = new BasicService();

LoginService.prototype.perform = function() {
	var response = new BasicNoResponse();
	response = this._do(response, this.validate);
	response = this._do(response, this.login);
	return this.handleResponse(response);
};

LoginService.prototype.validate = function() {
	var validation = LoginValidator.validate(this.loginData.name, this.loginData.password);
	var response = new BasicNoResponse();

	if (validation.hasFailed()){
		response = new BasicPayloadResponse(validation.errors, this.callbacks.failedValidation);
	}

	return response;
};

LoginService.prototype.login = function() {
		var loginResponse = new BasicNoResponse();

		var response = Api.login_admin(this.loginData);

		if (this.isLoginSuccessful(response)) {
			this.registerSession(response.sessionId, JSON.stringify(response.account));
			loginResponse = new BasicEmptyResponse(this.callbacks.success);
		}
		else if (this.needsTwoFactor(response)){
			loginResponse = new BasicEmptyResponse(this.callbacks.needsTwoFactor);
		}
		else {
			loginResponse = new LoginCredentialsErrorResponse(response, this.callbacks.failedCredentials);
		}

		return loginResponse;
};

LoginService.prototype.registerSession = function(api_token, active_admin) {
	sessionStorage.removeItem('api_token');

	sessionStorage.setItem('api_token', api_token);
	sessionStorage.setItem('active_admin', active_admin);
};

LoginService.prototype.isLoginSuccessful = function(response) {
	return !this.isNone(response.account) && !this.isNone(response.account.uuid);
};

LoginService.prototype.needsTwoFactor = function(response) {
	return !this.isNone(response.sessionId) && response.sessionId === '2-factor';
};



LoginData = function(name, password, deviceId, deviceName) {
	this.name = name;
	this.password = password;
	this.deviceId = deviceId;
	this.deviceName = deviceName;
};



LoginCallbacks = function(){
	this.failedValidation = function(validationErrors) {
		return validationErrors;
	};

	this.failedCredentials = function(credentialErrors) {
		return credentialErrors;
	};

	this.needsTwoFactor = function() {
		console.log("Two Factor Verification is required");
	};

	this.success = function(credentialErrors) {
		console.log('Login Successful');
	};
};

LoginCallbacks.prototype.addFailedValidation = function(callback) {
	this.failedValidation = callback;
};

LoginCallbacks.prototype.addFailedCredentials = function(callback) {
	this.failedCredentials = callback;
};

LoginCallbacks.prototype.addNeedsTwoFactor = function(callback) {
	this.needsTwoFactor = callback;
};

LoginCallbacks.prototype.addSuccess = function(callback) {
	this.success = callback;
};



LoginCredentialsErrorResponse = function(payload, callback){
	BasicPayloadResponse.call(this, payload, callback);
};

LoginCredentialsErrorResponse.prototype = new BasicPayloadResponse();


LoginCredentialsErrorResponse.prototype.resolve = function(self) {
	var error_msg = locate(Em.I18n.translations, 'errors');
	var errors = {
		email: error_msg.bad_credentials,
		password: error_msg.bad_credentials
	};
	return this.callback.call(self, errors);
};
