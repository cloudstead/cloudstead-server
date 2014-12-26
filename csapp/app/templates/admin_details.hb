<section class="deck">
	<div class="row">
		<dl class="tabs" data-tab>
			<dd class="half-width-tab">
				{{#link-to 'adminHome'}}
					{{t sections.admin.your_cloudsteads}}
				{{/link-to}}
			</dd>
			<dd class="active half-width-tab">
				{{#link-to 'adminDetails' }}
					{{t sections.admin.account_details}}
				{{/link-to}}
			</dd>
		</dl>
		<div class="tabs-content">
			<div class="content active">
				<form {{action 'updateAdminAccount' on="submit"}}>
					<div class="large-12 columns">
						<div class="field-container">
							<div class="row">
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">{{t forms.admin.firstName}}</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{#if requestMessages.error.firstName}}
										{{input type="text" value=firstName class="error first_name_input"}}
										<span class="message error">{{requestMessages.error.firstName}}</span>
									{{else}}
										{{ input type="text" value=firstName classNames="first_name_input" }}
									{{/if}}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									&nbsp;
								</div>
							</div>
						</div>
						<div class="field-container">
							<div class="row">
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">{{t forms.admin.lastName}}</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{#if requestMessages.error.lastName}}
										{{ input type="text" value=lastName classNames="error last_name_input" }}
										<span class="message error">{{requestMessages.error.lastName}}</span>
									{{else}}
										{{ input type="text" value=lastName classNames="last_name_input" }}
									{{/if}}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									&nbsp;
								</div>
							</div>
						</div>
						<div class="field-container">
							<div class="row">
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">{{t forms.admin.email}}</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{#if requestMessages.error.email}}
										{{ input type="text" value=email classNames="error email_input" }}
										<span class="message error">{{requestMessages.error.email}}</span>
									{{else}}
										{{ input type="text" value=email classNames="email_input" }}
									{{/if}}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									&nbsp;
								</div>
							</div>
						</div>
						<div class="select-container">
							<div class="row">
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">
										{{t forms.admin.mobilePhoneCountryCode}}
									</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{view Ember.Select
										content=countryList
										optionValuePath="content.code"
										optionLabelPath="content.country"
										selectionBinding="mobilePhoneCountry" }}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									&nbsp;
								</div>
							</div>
						</div>
						<div class="field-container">
							<div class="row">
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">
										{{t forms.admin.mobilePhone}}
									</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{#if requestMessages.error.mobilePhone}}
										{{ input type="text" value=mobilePhone classNames="error mobile_phone_input" }}
										<span class="message error">{{requestMessages.error.mobilePhone}}</span>
									{{else}}
										{{ input type="text" value=mobilePhone classNames="mobile_phone_input" }}
									{{/if}}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									&nbsp;
								</div>
							</div>
						</div>
						<div class="field-container">
							<div class="row">
								<div class="large-3 hide-for-medium hide-for-small columns">
									&nbsp;
								</div>
								<div class="large-5 medium-10 small-10 columns">
									<button type="submit" class="secondary expand update_details" {{action 'updateAdminAccount'}}>
										{{t forms.admin.update}}
									</button>
								</div>
								<div class="small-2 columns">
									&nbsp;
								</div>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
</section>
