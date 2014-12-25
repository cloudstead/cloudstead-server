{{#modal-dialog}}
	<div class="row modal-filler">
		<legend>{{t sections.login_two_factor}}</legend>
	</div>
	<div class="row modal-content">
			{{#if isFirst}}
				<div class="large-12 columns" style="padding-top:50px; padding-right: 0px;">
					{{t forms.verification.verify_msg}}
				</div>
			{{/if}}
			<div class="large-12 columns" style="padding-bottom:50px; padding-top: 40px;">
				<form {{action 'verifyFactor' on="submit"}}>
					<div class="field-container">
						<div class="row">
							<div class="small-7 columns">
								<label for="right-label" class="right">{{t forms.verification.verify_code}}</label>
							</div>
							<div class="small-5 columns">
								{{#if requestMessages.error.verifyCode}}
									{{ input type="text" value=verifyCode classNames="error two_factor_code" }}
								{{else}}
									{{ input type="text" value=verifyCode classNames="two_factor_code" }}
								{{/if}}

								{{#if requestMessages.error.verifyCode}}
									<span class="message error">{{requestMessages.error.verifyCode}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="large-7 medium-5 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-2 small-6 columns">
									<button type="submit" class="alert expand" {{action 'close'}}>{{t forms.admin.cancel_button}}</button>
							</div>
							<div class="large-1 medium-1 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-2 small-6 columns">
									<button type="submit" class="secondary expand verification_submit" {{action 'verifyFactor'}}>{{t forms.verification.verify_submit}}</button>
							</div>
						</div>
					</div>
				</form>
			</div>
	</div>
	<div class="row modal-filler">
		&nbsp;
	</div>
{{/modal-dialog}}
