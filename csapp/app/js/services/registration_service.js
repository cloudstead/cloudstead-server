RegistrationService = function(subject, registrationData, registrationCallbacks) {
	BasicService.call(this, subject, registrationData, registrationCallbacks);
	this.registrationData = this.serviceData;
};

RegistrationService.prototype = new BasicService();

RegistrationService.prototype.perform = function() {
	var response = new BasicNoResponse();
	response = this._do(response, this.validate);
	response = this._do(response, this.register);
	return this.handleResponse(response);
};

RegistrationService.prototype.validate = function() {
	var validation = RegistrationValidator.validate(this.registrationData);
	var response = new BasicNoResponse();

	if (validation.hasFailed()){
		response = new BasicPayloadResponse(validation.errors, this.callbacks.failedValidation);
	}

	return response;
};

RegistrationService.prototype.register = function() {
	var registrationResponse = new BasicNoResponse();

	var response = Api.register_admin(this._prepereDataForApi(this.registrationData));

	if (API_RESPONSE_STATUS.isSuccess(response.status)){
		registrationResponse = new BasicEmptyResponse(this.callbacks.success);
	}
	else if (API_RESPONSE_STATUS.isError(response.status)){
		var proccessedResponse = this._proccessRegistrationError(response);

		registrationResponse =
			new BasicPayloadResponse(proccessedResponse, this.callbacks.failedWithError);
	}

	return registrationResponse;
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

RegistrationService.prototype._proccessRegistrationError = function(registrationResponse) {
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
