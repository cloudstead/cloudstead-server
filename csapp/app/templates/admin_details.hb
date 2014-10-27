<section class="deck">
	<div class="row">
		<dl class="tabs" data-tab>
			<dd>{{#link-to 'adminHome'}}Your Cloudsteads{{/link-to}}</dd>
			<dd class="active">{{#link-to 'adminDetails' }}Account details{{/link-to}}</dd>
		</dl>
		<div class="tabs-content">
			<div class="content active">
				<form {{action 'updateAdminAccount' on="submit"}}>

		<div class="large-12 columns">
				<div class="field-container">
					<div class="row">
						<div class="small-3 columns">
							<label for="right-label" class="left msg-mod">{{t forms.admin.firstName}}</label>
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
						<div class="small-3 columns">
							<label for="right-label" class="left msg-mod">{{t forms.admin.lastName}}</label>
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
						<div class="small-3 columns">
							<label for="right-label" class="left msg-mod">{{t forms.admin.email}}</label>
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
						<div class="small-3 columns">
							<label for="right-label" class="left msg-mod">{{t forms.admin.mobilePhoneCountryCode}}</label>
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
						<div class="small-3 columns">
							<label for="right-label" class="left msg-mod">{{t forms.admin.mobilePhone}}</label>
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
						<div class="large-3 medium-3 hide-for-small columns">
							&nbsp;
						</div>
						<div class="large-5 medium-2 small-6 columns">
								<button type="submit" class="secondary expand" {{action 'updateAdminAccount'}}>Update</button>
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
