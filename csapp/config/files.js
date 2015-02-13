/* Exports a function which returns an object that overrides the default &
 *   plugin file patterns (used widely through the app configuration)
 *
 * To see the default definitions for Lineman's file paths and globs, see:
 *
 *   - https://github.com/linemanjs/lineman/blob/master/config/files.coffee
 */
module.exports = function(lineman) {
  //Override file patterns here
  return {
    js: {
      app: [
        "app/js/app.js",
        "app/js/api.js",
        "app/js/mixins/**/*.js",
        "app/js/controllers/cloudos_controller.js",
        "app/js/**/*.js"
      ],
      vendor: [
        "vendor/js/jquery-1.11.0.js",
        "vendor/js/foundation.min.js",
        "vendor/js/handlebars-v1.3.0.js",
        "vendor/js/ember-1.5.1.js",
        "vendor/js/modernizer.js",
        "vendor/js/purl.js",
        "vendor/js/i18n.js",
        "vendor/js/all.js",
        "vendor/js/countries.js",
        "vendor/js/sinon-quint-1.0.0.js",
        "vendor/js/sinon-1.11.1.js",
        "vendor/js/ember-autofocus.min.js"
      ]
    },
    img: {
      root: "images"
    },
    webfonts: {
        root: "fonts"
      }
  };
};
