App.rootElement = 'body';

App.setupForTesting();

App.injectTestHelpers();

// Load localization to tests
$.getScript("../dist/js/local/strings.js_en.mustache");

var FakeServer = newTestServer();
