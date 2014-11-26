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


LoginService = function(loginData, callbacks) {
	this.loginData = loginData;
	this.callbacks = callbacks;
};

LoginService.prototype.registerSession = function(api_token, active_admin) {
		sessionStorage.removeItem('api_token');

		sessionStorage.setItem('api_token', api_token);
		sessionStorage.setItem('active_admin', active_admin);
};

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

LoginService.prototype.handleResponse = function(self, loginResponse) {
	return loginResponse.resolve(self);
};
