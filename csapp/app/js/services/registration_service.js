RegistrationService = function(registrationData, registrationCallbacks) {
	this.registrationData = registrationData;
	this.callbacks = registrationCallbacks;
};

RegistrationService.prototype._prepereDataForApi = function(registrationData) {
	return {
		name: registrationData['email'],
		firstName: registrationData['firstName'],
		lastName: registrationData['lastName'],
		admin: false,
		suspended: false,
		twoFactor: false,
		email: registrationData['email'],
		emailVerified: false,
		mobilePhoneCountryCode: registrationData['mobilePhoneCountryCode'],
		mobilePhone: registrationData['mobilePhone'],
		maxCloudsteads: 1,
		password: registrationData['password'],
		tos: !!registrationData['tos'],
		activationCode: registrationData['activationCode'],
		accountName: registrationData['email'],
	};
};

RegistrationService.prototype.register = function() {
	var validation = RegistrationValidator.validate(this.registrationData);
	var registrationResponse = new RegistrationResponse('empty_response', null);

	if (validation.hasFailed()) {
		return new RegistrationErrorResponse(validation.errors, this.callbacks.failedValidation);
	}

	var response = Api.register_admin(this._prepereDataForApi(this.registrationData));

	if (response.status === 'success'){
		registrationResponse = new RegistrationResponse(response, this.callbacks.success);
	}
	else if (response.status === 'error'){
		registrationResponse = new RegistrationErrorResponse(response, this.callbacks.registrationError);
	}

	return registrationResponse;
};

RegistrationService.prototype.handleResponse = function(self, registrationResponse) {
	return registrationResponse.resolve(self);
};



RegistrationCallbacks = function(){
	this.failedValidation = function(validationErrors) {
		return validationErrors;
	};

	this.registrationError = function(registrationError) {
		return registrationError;
	};

	this.success = function(credentialErrors) {
		console.log('Registration Successful');
	};
};

RegistrationCallbacks.prototype.addFailedValidation = function(callback) {
	this.failedValidation = callback;
};

RegistrationCallbacks.prototype.addRegistrationError = function(callback) {
	this.registrationError = callback;
};

RegistrationCallbacks.prototype.addSuccess = function(callback) {
	this.success = callback;
};



RegistrationResponse = function(payload, callback){
	this.payload = payload;
	this.callback = callback;
};

RegistrationResponse.prototype.resolve = function(self) {
	return this.callback.call(self);
};



RegistrationErrorResponse = function(payload, callback){
	RegistrationResponse.call(this, payload, callback);
};

RegistrationErrorResponse.prototype.resolve = function(self) {
	return this.callback.call(self, this.payload);
};
