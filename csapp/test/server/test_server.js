function newTestServer() {

	var server = sinon.fakeServer.create();

	server.autoRespond = true;

	server.respondWith("PUT", /\/api\/admins\/.+%40.+/,
		[200, { "Content-Type": "application/json" }, '{}']);

	server.respondWith("POST", "/api/admins",
		function(request){
			var reqBodyObj = JSON.parse(request.requestBody);

			if (Ember.isNone(reqBodyObj['secondFactor']) || Ember.isEmpty(reqBodyObj['secondFactor'])) {
				request.respond(200, { "Content-Type": "application/json" }, '{ "sessionId": "2-factor" }');
			}
			else {
				request.respond(200, { "Content-Type": "application/json" }, '{"status": "success"}');
			}
		});

	server.respondWith("GET", "/api/cloudos",
		[200, { "Content-Type": "application/json" }, '{}']);

	return server;

}
