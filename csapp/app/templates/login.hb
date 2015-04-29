{{#modal-dialog action="close"}}

<section class="deck white-background" style="min-height:900px;">
	<div class="row">
		<div class="medium-9 large-6 medium-centered columns">
			<div class="stepper dim">
				<h2 class="light">{{t sections.login}}</h2>
				<form class="text-left" {{action 'doLogin' on="submit"}}>
					<label>
						{{t forms.admin.email}}
						{{#if requestMessages.error.email}}
							{{ input type="text" value=email class="error email_input" placeholder=(transAttr 'forms.admin.email') }}
							<span class="message error">{{requestMessages.error.email}}</span>
						{{else}}
							{{ input type="text" value=email class="email_input" placeholder=(transAttr 'forms.admin.email') }}
						{{/if}}
					</label>

					<label>
						{{t forms.admin.password}}
						{{#if requestMessages.error.password}}
							{{input type="password" value=password class="error password_input" placeholder=(transAttr 'forms.admin.password') }}
							<span class="message error">{{requestMessages.error.password}}</span>
						{{else}}
							{{input type="password" value=password class="password_input" placeholder=(transAttr 'forms.admin.password')}}
						{{/if}}
					</label>
					<a href="#" class="input-link"{{action "doForgotPassword"}}>
						<small>{{t forms.admin.forgot_password}}</small>
					</a>
					<button class="expand"  {{action 'doLogin'}}>{{t sections.login}}</button>
				</form>
			</div>
		</div>
	</div>
</section>

	{{ autofocus }}
{{/modal-dialog}}
