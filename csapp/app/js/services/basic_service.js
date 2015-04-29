BasicService = function(subject, data, callbacks) {
	this.subject = subject;
	this.serviceData = data;
	this.callbacks = callbacks;
};

BasicService.prototype.isNone = function(value) {
	return value === undefined || value === null;
};

BasicService.prototype.handleResponse = function(response) {
	return response.resolve(this.subject);
};

BasicService.prototype.perform = function() {
	return this.handleResponse(null);
};

BasicService.prototype._do = function(response, f, args) {
	var r = response;
	if (response.isNoResponse()){
		r = f.apply(this, args);
	}
	return r;
};



BasicNoResponse = function(){
};

BasicNoResponse.prototype.resolve = function(self) {
	return null;
};

BasicNoResponse.prototype.isNoResponse = function() {
	return true;
};



BasicEmptyResponse = function(callback){
	this.callback = callback;
};

BasicEmptyResponse.prototype.resolve = function(self) {
	return this.callback.call(self);
};

BasicEmptyResponse.prototype.isNoResponse = function() {
	return false;
};



BasicPayloadResponse = function(payload, callback){
	BasicEmptyResponse.call(this, callback);
	this.payload = payload;
};

BasicPayloadResponse.prototype = new BasicEmptyResponse();

BasicPayloadResponse.prototype.resolve = function(self) {
	return this.callback.call(self, this.payload);
};



BasicServiceCallbacks = function(){
	this.failedValidation = function(validationErrors) {
		return validationErrors;
	};

	this.failedWithError = function(error) {
		return error;
	};

	this.success = function(credentialErrors) {
		$('#signupModal').foundation('reveal', 'close');
		console.log('success');
	};
};

BasicServiceCallbacks.prototype.addFailedValidation = function(callback) {
	this.failedValidation = callback;
};

BasicServiceCallbacks.prototype.addError = function(callback) {
	this.failedWithError = callback;
};

BasicServiceCallbacks.prototype.addSuccess = function(callback) {
	this.success = callback;
};
