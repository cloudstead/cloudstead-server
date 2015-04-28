{{#modal-dialog}}
	<section class="deck white-background">
		<div class="row">
			<div class="medium-9 large-6 medium-centered columns">
				<div class="stepper dim">
					<h1 class="light">{{t forms.admin.get_started }}</h1>
					<form {{action 'doNewAccount' on="submit"}}>
						<label class="label-hide">
							{{ t forms.admin.firstName}}
							{{#if requestMessages.error.firstName}}
								{{input type="text" value=firstName class="error first_name_input" placeholder=(transAttr 'forms.admin.firstName') }}
								<span class="message error">{{requestMessages.error.firstName}}</span>
							{{else}}
								{{ input type="text" value=firstName classNames="first_name_input" placeholder=(transAttr 'forms.admin.firstName') }}
							{{/if}}
						</label>
						<label class="label-hide">
							{{ t forms.admin.lastName }}
							{{#if requestMessages.error.lastName}}
								{{ input type="text" value=lastName classNames="error last_name_input" placeholder=(transAttr 'forms.admin.lastName') }}
								<span class="message error">{{requestMessages.error.lastName}}</span>
							{{else}}
								{{ input type="text" value=lastName classNames="last_name_input" placeholder=(transAttr 'forms.admin.lastName') }}
							{{/if}}
						</label>
						<label class="label-hide">
							{{ t forms.admin.email }}
							{{#if requestMessages.error.email}}
								{{ input type="text" value=email classNames="error email_input" placeholder=(transAttr 'forms.admin.email') }}
								<span class="message error">{{requestMessages.error.email}}</span>
							{{else}}
								{{ input type="text" value=email classNames="email_input" placeholder=(transAttr 'forms.admin.email') }}
							{{/if}}
						</label>
						<label class="label-hide">
						  	{{t forms.admin.mobilePhoneCountryCode}}
						</label>
						{{view Ember.Select
							content=countryList
							optionValuePath="content.code"
							optionLabelPath="content.country"
							selectionBinding="mobilePhoneCountry"
							classNames="mobile_phone_country_code" }}	
						<label class="label-hide">
							{{t forms.admin.mobilePhone}}
							{{#if requestMessages.error.mobilePhone}}
								{{ input type="text" value=mobilePhone classNames="error mobile_phone_input" placeholder=(transAttr 'forms.admin.mobilePhone') }}
								<span class="message error">{{requestMessages.error.mobilePhone}}</span>
							{{else}}
								{{ input type="text" value=mobilePhone classNames="mobile_phone_input" placeholder=(transAttr 'forms.admin.mobilePhone') }}
							{{/if}}
						</label>
						<label class="label-hide">
							{{t forms.admin.password}}
							{{#if requestMessages.error.password}}
								{{ input type="password" value=password classNames="error password_input" placeholder=(transAttr 'forms.admin.password') }}
								<span class="message error">{{requestMessages.error.password}}</span>
							{{else}}
								{{ input type="password" value=password classNames="password_input" placeholder=(transAttr 'forms.admin.password') }}
							{{/if}}
						</label>
						<label class="label-hide">
							{{t forms.admin.password2}}
							{{#if requestMessages.error.password2}}
								{{ input type="password" value=password2 classNames="error password_confirm_input" placeholder=(transAttr 'forms.admin.password2') }}
								<span class="message error">{{requestMessages.error.password2}}</span>
							{{else}}
								{{ input type="password" value=password2 classNames="password_confirm_input"placeholder=(transAttr 'forms.admin.password2') }}
							{{/if}}
						</label>
						<div class="field-container login-footer text-left">
							<input type="checkbox" id="show-password"> 
							<label for="show-password" class="label-checkbox">
								<small>{{t forms.admin.show_password }}</small>
							</label>
						</div>
						<label class="label-hide">
							{{t forms.admin.activationCode}}
							{{#if requestMessages.error.activationCode}}
								{{input type="text" value=activationCode class="error activation_code" placeholder=(transAttr 'forms.admin.activationCode') }}
							<span class="message error">{{requestMessages.error.activationCode}}</span>
							{{else}}
								{{ input type="text" value=activationCode classNames="activation_code" placeholder=(transAttr 'forms.admin.activationCode') }}
							{{/if}}
						</label>
						{{!-- <small class="note note-up">optional</small> --}}
						<div class="field-container text-left">
							{{#if requestMessages.error.tos}}
								{{ input type="checkbox" checkedBinding="tos" classNames="error tos_checkbox" id="terms" }}
								<span class="message error">{{requestMessages.error.tos}}</span>
							{{else}}
								{{ input type="checkbox" checkedBinding="tos" classNames="tos_checkbox" id="terms" }}
							{{/if}}
						   <label for="terms" class="label-checkbox">
								<small>
								{{t forms.admin.tos }}
									<a target="_blank" href="http://www.cloudstead.io/terms-of-service/">
										{{t sections.footer.terms_of_service }}
									</a>
								</small>
						   </label>
						</div>
						<button class="expand" {{action 'doNewAccount'}}>{{t forms.admin.signup_button}}</button>
						<p>
							<small>
								{{t forms.admin.tos }}
								<a target="_blank" href="http://www.cloudstead.io/terms-of-service/">
									{{t sections.footer.terms_of_service }}
								</a>
							</small>
						</p>
				    </form> 
				</div>
			</div>
		</div>
	</section>
	{{ autofocus }}
{{/modal-dialog}}
