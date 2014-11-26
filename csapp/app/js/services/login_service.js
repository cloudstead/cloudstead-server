// loginData is an instance of `LoginData`.
// loginCallbacks is an instance of `LoginCallbacks`.
LoginService = function(loginData, loginCallbacks) {
	this.loginData = loginData;
	this.callbacks = loginCallbacks;
};

// Validates `loginData` and performs the `admin_login` api call.
// Returns `LoginResponse` if login is a success or a two factor verification is needed.
// Returns `LoginValidationErrorResponse` if validation fails.
// Returns `LoginCredentialsErrorResponse` if server responds with an error.
LoginService.prototype.login = function() {
	var validation = LoginValidator.validate(this.loginData.name, this.loginData.password);
	var loginResponse = new LoginResponse('empty_response', {});

	if (validation.hasFailed()){
		return new LoginValidationErrorResponse(validation.errors, this.callbacks.failedValidation);
	}

	var response = Api.login_admin(this.loginData);

	if (!Ember.isNone(response.account) && !Ember.isNone(response.account.uuid)) {
		this.registerSession(response.sessionId, JSON.stringify(response.account));
		loginResponse = new LoginResponse(response, this.callbacks.success);
	}
	else if (!Ember.isNone(response.sessionId) && response.sessionId === '2-factor'){
		loginResponse = new LoginResponse(response, this.callbacks.needsTwoFactor);
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

LoginService.prototype.handleResponse = function(self, loginResponse) {
	return loginResponse.resolve(self);
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



LoginResponse = function(payload, callback){
	this.payload = payload;
	this.callback = callback;
};

LoginResponse.prototype.resolve = function(self) {
	return this.callback.call(self);
};



LoginValidationErrorResponse = function(payload, callback){
	LoginResponse.call(this, payload, callback);
};

LoginValidationErrorResponse.prototype.resolve = function(self) {
	return this.callback.call(self, this.payload);
};



LoginCredentialsErrorResponse = function(payload, callback){
	LoginResponse.call(this, payload, callback);
};

LoginCredentialsErrorResponse.prototype.resolve = function(self) {
	var error_msg = locate(Em.I18n.translations, 'errors');
	var errors = {
		email: error_msg.bad_credentials,
		password: error_msg.bad_credentials
	};
	return this.callback.call(self, errors);
};
