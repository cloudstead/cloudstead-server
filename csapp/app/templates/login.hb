{{#modal-dialog action="close"}}
	<div class="row modal-filler">
		<legend>{{t sections.login}}</legend>
	</div>
	<div class="row modal-content">
			<div class="large-12 columns" style="padding-top:50px; padding-bottom:50px;">
				<form {{action 'doLogin' on="submit"}}>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right">{{t forms.admin.email}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.email}}
									{{ input type="text" value=email class="error email_input" }}
								{{else}}
									{{ input type="text" value=email class="email_input" }}
								{{/if}}

								{{#if requestMessages.error.email}}
									<span class="message error">{{requestMessages.error.email}}</span>
								{{/if}}
							</div>
							<div class="large-2 medium-2 hide-for-small columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 small-12 columns">
								<label for="right-label" class="right">{{t forms.admin.password}}</label>
							</div>
							<div class="large-5 small-12 columns">
								{{#if requestMessages.error.password}}
									{{input type="password" value=password class="error password_input"}}
								{{else}}
									{{input type="password" value=password class="password_input"}}
								{{/if}}

								{{#if requestMessages.error.password}}
									<span class="message error">{{requestMessages.error.password}}</span>
								{{/if}}
							</div>
							<div class="large-2 medium-2 hide-for-small columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-5 hide-for-medium hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-6 small-6 columns">
									<button id="confirm_sign_in" type="submit" class="secondary expand" {{action 'doLogin'}}>{{t forms.admin.login_button}}</button>
							</div>
							<div class="large-1 hide-for-medium hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-5 small-6 columns">
									<button id="cancel_sign_in" class="alert expand" {{action 'close'}}>{{t forms.admin.cancel_button}}</button>
							</div>
							<div class="large-2 medium-1 hide-for-small columns">
								&nbsp;
							</div>
						</div>
					</div>
				</form>
				<a href="" {{action "doForgotPassword"}}>{{t forms.admin.forgot_password}}</a>
			</div>
			<div class="forgot-password-notification">{{notificationForgotPassword}}</div>
	</div>
	<div class="row modal-filler">
		&nbsp;
	</div>
	{{ autofocus }}
{{/modal-dialog}}
