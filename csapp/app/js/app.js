String.prototype.trim = String.prototype.trim || function trim() { return this.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); };

App = Ember.Application.create({
	IS_HOME: false,
	//LOG_TRANSITIONS_INTERNAL : true,
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
	var message = originalMessage.slice(1, -1);

	// Hack for one error message.
	if (message.indexOf("teardownPreviousInstance.nonFatalError") !== -1){
		message = message.replace("teardownPreviousInstance", "teardownPreviousInstanceError");
	}

	return locate(Em.I18n.translations, message);
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

Ember.Handlebars.registerHelper('transAttr', function (key) {
  return Em.I18n.t(key);
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

Timer = {
	ms_in_s: 1000,
	ms2s: function(ms) {
		return ms/this.ms_in_s;
	},
	s2ms: function(s) {
		return s * this.ms_in_s;
	}
};


DeviceCookieGenerator = {
	generate: function() {
		var ckDeviceId = checkCookie("deviceId");
		var ckDeviceName = checkCookie("deviceName");

		if ((!ckDeviceId) || (!ckDeviceName)){
			setCookie("deviceId", generateDeviceId(), 365);
			setCookie("deviceName", getDeviceName(), 365);
		}
	}
};

ErrorResponseMessages = {
	"{err.email.notUnique}": {
		errorType: "email",
		message: "This email has already been taken."
	},

	"{err.mobilePhone.notUnique}": {
		errorType: "mobilePhone",
		message: "This phone number has already been registered."
	},

	doesKnowError: function(error) {
		return ErrorResponseMessages[error] !== undefined ? true : false;
	}
};
