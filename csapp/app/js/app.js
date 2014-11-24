String.prototype.trim = String.prototype.trim || function trim() { return this.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); };

App = Ember.Application.create({
		LOG_TRANSITIONS: true // for debugging, disable in prod
});

function locate(obj, path) {
	if (!path) {
		return null;
	}
	if (path[0] === '{' && path[path.length-1] === '}') {
			// strip leading/trailing curlies, if present
			path = path.substring(1, path.length-1);
	}
	path = path.split('.');
	var arrayPattern = /(.+)\[(\d+)\]/;
	for (var i = 0; i < path.length; i++) {
			var match = arrayPattern.exec(path[i]);
			if (match) {
					obj = obj[match[1]][parseInt(match[2], 0)];
			} else {
					obj = obj[path[i]];
			}
	}

	return obj;
}

function setCookie(cname, cvalue, exdays){
	var d = new Date();
	d.setTime(d.getTime() + (exdays*24*60*60*1000));
	var expires="expires="+d.toUTCString();
	document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
	var name = cname + "=";
	var ca = document.cookie.split(';');
	for(var i=0; i<ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0)===' ') {
			c = c.substring(1);
		}
		if (c.indexOf(name) !== -1) {
			return c.substring(name.length, c.length);
		}
	}
	return "";
}

function checkCookie(cname) {
	var cookie = getCookie(cname);
	if (cookie !== "") {
		return true;
	} else {
		return false;
	}
}

function generateDeviceId()
{
	var text = "";
	var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	for( var i=0; i < 20; i++ ){
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}

function getDeviceName(){
	return navigator.userAgent;
}

function swapStatusMessage(originalMessage){
	var n = null;
	try {
		n = originalMessage.indexOf(".");
	}catch(e){
		return originalMessage;
	}

	var filteredStatus = originalMessage.substring(n+1, originalMessage.length - 1);
	var locStatus = locate(Em.I18n.translations, 'setup');
	try{
		return locStatus[filteredStatus];
	}catch(e){
		return originalMessage;
	}
}

Ember.Handlebars.helper('t-subst', function(view, options) {
		var opts = options.hash;
		var message = locate(Em.I18n.translations, opts.messageKey);
		if (!message) {
			return '??undefined translation: '+opts.messageKey;
		}
		var value = this.get(opts.value);
		return value ? message.replace("{1}", value) : opts.defaultValue;
});

//function get_username () {
//    const account = Api.get_active_account();
//    return account ? account.name : null;
//}

App.RequestMessagesObject = Ember.Object.extend({
	_doInitialization: function() {
		var self = this;
		self.set("error", this.get('json')["errors"]);
	}.on('init')
});

App.ModalDialogComponent = Ember.Component.extend({
		actions: {
			close: function() {
				return this.sendAction();
			}
		}
	});

Validator = {
	validateTwoFactorVerificationCode: function(code){
		var errors = {"verificationCode": null};
		var error_msg = locate(Em.I18n.translations, 'errors');
		var codeRegexp = /^(\d){7}$/;

		if ((!code) || (code.trim() === '')){
			errors.verificationCode = error_msg.field_required;
		}
		else if (!codeRegexp.test(code)){
			errors.verificationCode = error_msg.two_factor_code_invalid;
		}
		return errors;
	}
};

PasswordValidator = {
	getErrorsFor: function(object, password, confirm){
		var data = new ValidatorData(locate(Em.I18n.translations, 'errors'), object);

		data.errors = EqualPasswordsValidator.getErrors(data, password, confirm);

		data.errors = PresenceValidator.getErrors(data, [password, confirm]);

		return data.errors;
	}
};

ValidatorData = function(error_msg, validationSubject) {
	this.errors = {
		is_not_empty: false
	};

	this.error_msg = error_msg;

	this.validationSubject = validationSubject;
};

PresenceValidator = {
	getErrors: function(data, fields) {
		fields.forEach(function(property){
			if(EmptyValidator.is_empty(data.validationSubject.get(property))){
				data.errors[property] = data.error_msg.field_required;
				data.errors["is_not_empty"] = true;
			}
		});

		return data.errors;
	}
};

EmptyValidator = {
	is_empty: function(value) {
		return (value === undefined || String(value).trim().length === 0) ? true : false;
	}
};

EqualPasswordsValidator = {
	getErrors: function(data, passwordField, confirmField) {
		password = data.validationSubject.get(passwordField);
		confirm = data.validationSubject.get(confirmField);

		if (password !== confirm){
			data.errors[passwordField] = data.error_msg.password_mismatch;
			data.errors[confirmField] = data.error_msg.password_mismatch;
			data.errors["is_not_empty"] = true;
		}
		return data.errors;
	}
};
