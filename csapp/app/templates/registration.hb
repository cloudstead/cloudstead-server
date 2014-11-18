{{#modal-dialog}}
	<div class="row modal-filler">
		<legend>{{t sections.registration}}</legend>
	</div>
	<div class="row modal-content">
	<form {{action 'doNewAccount' on="submit"}}>

			<div class="large-12 columns">
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.firstName}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.firstName}}
									{{input type="text" value=firstName class="error first_name_input"}}
									<span class="message error">{{requestMessages.error.firstName}}</span>
								{{else}}
									{{ input type="text" value=firstName classNames="first_name_input" }}
								{{/if}}
							</div>
							<div class="large-2 hide-for-medium hide-for-small columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.lastName}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.lastName}}
									{{ input type="text" value=lastName classNames="error last_name_input" }}
									<span class="message error">{{requestMessages.error.lastName}}</span>
								{{else}}
									{{ input type="text" value=lastName classNames="last_name_input" }}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.email}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.email}}
									{{ input type="text" value=email classNames="error email_input" }}
									<span class="message error">{{requestMessages.error.email}}</span>
								{{else}}
									{{ input type="text" value=email classNames="email_input" }}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.mobilePhoneCountryCode}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.mobilePhoneCountryCode}}
									{{ input type="text" value=mobilePhoneCountryCode classNames="error mobile_country_code_input" }}
									<span class="message error">{{requestMessages.error.mobilePhoneCountryCode}}</span>
								{{else}}
									{{ input type="text" value=mobilePhoneCountryCode classNames="mobile_country_code_input" }}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.mobilePhone}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.mobilePhone}}
									{{ input type="text" value=mobilePhone classNames="error mobile_phone_input" }}
									<span class="message error">{{requestMessages.error.mobilePhone}}</span>
								{{else}}
									{{ input type="text" value=mobilePhone classNames="mobile_phone_input" }}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.password}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.password}}
									{{ input type="password" value=password classNames="error password_input" }}
									<span class="message error">{{requestMessages.error.password}}</span>
								{{else}}
									{{ input type="password" value=password classNames="password_input" }}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.password2}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.password2}}
									{{ input type="password" value=password2 classNames="error password_confirm_input" }}
									<span class="message error">{{requestMessages.error.password2}}</span>
								{{else}}
									{{ input type="password" value=password2 classNames="password_confirm_input" }}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-8 columns">
								<label for="right-label" class="right msg-mod">{{t forms.admin.tos}}</label>
							</div>
							<div class="large-5 small-4 columns">
								{{#if requestMessages.error.tos}}
									{{ input type="checkbox" checkedBinding="tos"  classNames="error tos_checkbox"}}
									<span class="message error">{{requestMessages.error.tos}}</span>
								{{else}}
									{{ input type="checkbox" checkedBinding="tos"  classNames="tos_checkbox"}}
								{{/if}}
							</div>
							<div class="large-2 small-12 columns">
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 medium-1 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-5 small-6 columns">
									<button id="cancel_sign_up" type="submit" class="alert expand" {{action 'close'}}>
										{{t forms.admin.cancel_button}}
									</button>
							</div>
							<div class="large-1 hide-for-medium hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-5 small-6 columns">
									<button id="confirm_sign_up" type="submit" class="secondary expand" {{action 'doNewAccount'}}>
										{{t forms.admin.signup_button}}
									</button>
							</div>
							<div class="large-2 medium-1 hide-for-small columns">
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
