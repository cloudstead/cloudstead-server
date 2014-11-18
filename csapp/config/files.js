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
      vendor: [
        "vendor/js/jquery-1.11.0.js",
        "vendor/js/foundation.min.js",
        "vendor/js/handlebars-v1.3.0.js",
        "vendor/js/ember-1.5.1.js",
        "vendor/js/modernizer.js",
        "vendor/js/purl.js",
        "vendor/js/i18n.js",
        "vendor/js/all.js",
        "vendor/js/countries.js"
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
