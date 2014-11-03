{{#modal-dialog}}
	<div class="row modal-filler">
		<legend>{{t sections.registration}}</legend>
	</div>
	<div class="row modal-content">
	<form {{action 'doNewAccount' on="submit"}}>

			<div class="large-12 columns">
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.firstName}}</label>
							</div>
							<div class="small-5 columns">
									{{ input type="text" value=firstName }}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.firstName}}
									<span class="message error right">{{requestMessages.error.firstName}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.lastName}}</label>
							</div>
							<div class="small-5 columns">
								{{ input type="text" value=lastName }}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.lastName}}
									<span class="message error right">{{requestMessages.error.lastName}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.email}}</label>
							</div>
							<div class="small-5 columns">
								{{ input type="text" value=email }}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.email}}
									<span class="message error right">{{requestMessages.error.email}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.mobilePhoneCountryCode}}</label>
							</div>
							<div class="small-5 columns">
								{{ input type="text" value=mobilePhoneCountryCode }}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.mobilePhoneCountryCode}}
									<span class="message error right">{{requestMessages.error.mobilePhoneCountryCode}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.mobilePhone}}</label>
							</div>
							<div class="small-5 columns">
								{{ input type="text" value=mobilePhone }}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.mobilePhone}}
									<span class="message error right">{{requestMessages.error.mobilePhone}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.password}}</label>
							</div>
							<div class="small-5 columns">
								{{input type="password" value=password}}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.password}}
									<span class="message error right">{{requestMessages.error.password}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.password2}}</label>
							</div>
							<div class="small-5 columns">
								{{input type="password" value=password2}}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.password2}}
									<span class="message error right">{{requestMessages.error.password2}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.tos}}</label>
							</div>
							<div class="small-5 columns">
								{{input type="checkbox" checkedBinding="tos"}}
							</div>
							<div class="small-2 columns">
								{{#if requestMessages.error.tos}}
									<span class="message error right">{{requestMessages.error.tos}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 medium-7 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-2 small-6 columns">
									<button type="submit" class="alert expand" {{action 'close'}}>{{t forms.admin.cancel_button}}</button>
							</div>
							<div class="large-1 medium-1 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-2 small-6 columns">
									<button type="submit" class="secondary expand" {{action 'doNewAccount'}}>{{t forms.admin.signup_button}}</button>
							</div>
							<div class="small-2 columns">
								&nbsp;
							</div>
						</div>

					</div>
			</div>
	</form>
	</div>
	<div class="row modal-filler">
		&nbsp;
	</div>
{{/modal-dialog}}
