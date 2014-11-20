<section class="deck">
	<div class="row">
		<div class="medium-6 large-5 small-12 medium-centered columns">
			<form {{action 'doResetPassword' on="submit"}}>
				<fieldset class="wrap-set small-12">
					<legend>{{t forms.reset_password.label}}</legend>
					<div class="field-container">
						<label for="password">{{t forms.admin.password}}</label>
						{{#if requestMessages.error.password}}
							{{input type="password" value=password class="error"}}
						{{else}}
							{{input type="password" value=password}}
						{{/if}}

						{{#if requestMessages.error.password}}
							<span class="message error">{{requestMessages.error.password}}</span>
						{{/if}}
					</div>
					<div class="field-container">
						<label for="password_confirm">{{t forms.admin.password2}}</label>
						{{#if requestMessages.error.passwordConfirm}}
							{{input type="password" value=passwordConfirm class="error"}}
						{{else}}
							{{input type="password" value=passwordConfirm}}
						{{/if}}

						{{#if requestMessages.error.passwordConfirm}}
							<span class="message error">{{requestMessages.error.passwordConfirm}}</span>
						{{/if}}
					</div>
					<div class="field-container">
						<p><button type="submit" class="expand">{{t forms.reset_password.label}}</button></p>
					</div>
				</fieldset>
			</form>
		</div>
	</div>
</section>
