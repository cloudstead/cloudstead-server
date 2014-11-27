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
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">{{t forms.admin.lastName}}</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{ input type="text" value=lastName }}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									{{#if requestMessages.error.lastName}}
										<span class="message error right">{{requestMessages.error.lastName}}</span>
									{{/if}}
								</div>
							</div>
						</div>
						<div class="field-container">
							<div class="row">
								<div class="large-3 medium-10 small-10 columns">
									<label for="right-label" class="left msg-mod">{{t forms.admin.email}}</label>
								</div>
								<div class="large-5 medium-10 small-10 columns">
									{{ input type="text" value=email }}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									{{#if requestMessages.error.email}}
										<span class="message error right">{{requestMessages.error.email}}</span>
									{{/if}}
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
									{{#if requestMessages.error.mobilePhoneCountryCode}}
										<span class="message error right">
											{{requestMessages.error.mobilePhoneCountryCode}}
										</span>
									{{/if}}
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
									{{ input type="text" value=mobilePhone }}
								</div>
								<div class="large-2 medium-10 small-10 columns">
									{{#if requestMessages.error.mobilePhone}}
										<span class="message error right">{{requestMessages.error.mobilePhone}}</span>
									{{/if}}
								</div>
							</div>
						</div>
						<div class="field-container">
							<div class="row">
								<div class="large-3 hide-for-medium hide-for-small columns">
									&nbsp;
								</div>
								<div class="large-5 medium-10 small-10 columns">
									<button type="submit" class="secondary expand" {{action 'updateAdminAccount'}}>
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
