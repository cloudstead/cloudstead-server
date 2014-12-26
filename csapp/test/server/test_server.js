function newTestServer() {

	var server = sinon.fakeServer.create();

	server.autoRespond = true;

	server.respondWith("PUT", /^\/api\/admins\/.+%40.+$/,
		[200, { "Content-Type": "application/json" }, '{}']);

	server.respondWith("POST", "/api/admins",
		function(request){
			var reqBodyObj = JSON.parse(request.requestBody);

			if (shouldRespondWithTwoFactor(reqBodyObj)) {
				request.respond(200, { "Content-Type": "application/json" }, '{ "sessionId": "2-factor" }');
			}
			else {
				request.respond(200, { "Content-Type": "application/json" },
					JSON.stringify(verifiedUserLoginResponse(reqBodyObj)));
			}
		});

	server.respondWith("GET", /^\/api\/admins\/.+$/,
		function(request){
			var reqBodyObj = JSON.parse(request.requestBody);
			request.respond(200, { "Content-Type": "application/json" }, JSON.stringify(adminProfileResponse()));
		});

	server.respondWith("GET", "/api/cloudos",
		[200, { "Content-Type": "application/json" }, '{}']);

	return server;

}

function shouldRespondWithTwoFactor(reqBodyObj){
	return (Ember.isNone(reqBodyObj['secondFactor']) || Ember.isEmpty(reqBodyObj['secondFactor'])) &&
		isVerifiedUserRequest(reqBodyObj)
}

function isVerifiedUserRequest(reqBodyObj) {
	return reqBodyObj['name'] !== Ember.Test.verifiedUserData['.email_input'];
}

function verifiedUserLoginResponse(reqBodyObj) {
	return {
		sessionId: "9db600f3-d9c7-4966-8032-ba4a901fb79e",
		account: {
			uuid: "22a7839e-0fd1-465f-b24c-58363c12fddd",
			name: reqBodyObj['name'],
			authId: "102458",
			firstName: reqBodyObj['first_name'],
			lastName: reqBodyObj['last_name'],
			admin: false,
			suspended: false,
			twoFactor: true,
			lastLogin: 1419588229372,
			email: reqBodyObj['name'],
			emailVerified: true,
			mobilePhone: 1233456,
			mobilePhoneCountryCode: "1",
			maxCloudsteads: 1,
			accountName: reqBodyObj['name']
		}
	};
}

function adminProfileResponse() {
	return {
			uuid: "22a7839e-0fd1-465f-b24c-58363c12fddd",
			name: "test@test.com",
			authId: "102458",
			firstName: "First Name",
			lastName: "Last Name",
			admin: false,
			suspended: false,
			twoFactor: true,
			lastLogin: 1419588229372,
			email: "test@test.com",
			emailVerified: true,
			mobilePhone: 1233456,
			mobilePhoneCountryCode: "1",
			maxCloudsteads: 1,
			accountName: "test@test.com"
	};
}
