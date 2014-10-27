{{#modal-dialog action="close"}}
	<div class="row modal-filler">
		<legend>{{t sections.login}}</legend>
	</div>
	<div class="row modal-content">
			<div class="large-12 columns" style="padding-top:50px; padding-bottom:50px;">
				<form {{action 'doLogin' on="submit"}}>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right">{{t forms.admin.email}}</label>
							</div>
							<div class="small-5 columns">
								{{#if requestMessages.error.name}}
									{{ input type="text" value=name class="error" }}
								{{else}}
									{{ input type="text" value=name }}
								{{/if}}

								{{#if requestMessages.error.name}}
									<span class="message error">{{requestMessages.error.name}}</span>
								{{/if}}
							</div>
							<div class="large-2 medium-2 hide-for-small columns">
								&nbsp;
							</div>
						</div>
					</div>
					<div class="field-container">
						<div class="row">
							<div class="small-5 columns">
								<label for="right-label" class="right">{{t forms.admin.password}}</label>
							</div>
							<div class="small-5 columns">
										{{#if requestMessages.error.password}}
									{{input type="password" value=password class="error"}}
								{{else}}
									{{input type="password" value=password}}
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
							<div class="large-5 medium-5 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-2 small-6 columns">
									<button type="submit" class="alert expand" {{action 'close'}}>{{t forms.admin.cancel_button}}</button>
							</div>
							<div class="large-1 medium-1 hide-for-small columns">
								&nbsp;
							</div>
							<div class="large-2 medium-2 small-6 columns">
									<button type="submit" class="secondary expand" {{action 'doLogin'}}>{{t forms.admin.login_button}}</button>
							</div>
							<div class="large-2 medium-2 hide-for-small columns">
								&nbsp;
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