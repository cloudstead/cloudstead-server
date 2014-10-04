String.prototype.trim = String.prototype.trim || function trim() { return this.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); };

App = Ember.Application.create({
    LOG_TRANSITIONS: true // for debugging, disable in prod
});

function locate(obj, path) {
    if (!path) return null;
    if (path[0] == '{' && path[path.length-1] == '}') {
        // strip leading/trailing curlies, if present
        path = path.substring(1, path.length-1);
    }
    path = path.split('.');
    var arrayPattern = /(.+)\[(\d+)\]/;
    for (var i = 0; i < path.length; i++) {
        var match = arrayPattern.exec(path[i]);
        if (match) {
            obj = obj[match[1]][parseInt(match[2])];
        } else {
            obj = obj[path[i]];
        }
    }

    return obj;
}

Ember.Handlebars.helper('t-subst', function(view, options) {
    var opts = options.hash;
    var message = locate(Em.I18n.translations, opts.messageKey);
    if (!message) return '??undefined translation: '+opts.messageKey;
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
