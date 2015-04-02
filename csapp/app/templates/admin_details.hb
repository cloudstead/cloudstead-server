
<section class="slide-canvas">
	<div class="row">
		<div class="large-8 large-centered columns">
			<div class="dashboard-container">
				<h1 class="delta light">{{t topbar.profile }}</h1>
				<article>
				<form {{action 'updateAdminAccount' on="submit"}}>
					<label>
						{{t forms.admin.firstName}}
						{{#if requestMessages.error.firstName}}
							{{input type="text" value=firstName class="error"}}
							<span class="message error">{{requestMessages.error.firstName}}</span>
						{{else}}
							{{ input type="text" value=firstName }}
						{{/if}}
					</label>
					<label>
						{{t forms.admin.lastName}}
						{{#if requestMessages.error.lastName}}
							{{input type="text" value=lastName class="error"}}
							<span class="message error">{{requestMessages.error.lastName}}</span>
						{{else}}
							{{ input type="text" value=lastName }}
						{{/if}}
					</label>
					<label>
						{{t forms.admin.email}}
						{{#if requestMessages.error.email}}
							{{ input type="text" value=email classNames="error" }}
							<span class="message error">{{requestMessages.error.email}}</span>
						{{else}}
							{{ input type="text" value=email }}
						{{/if}}
					</label>
					<div class="field-container">
					    <label>{{t forms.admin.mobilePhoneCountryCode}}
						{{view Ember.Select
							content=countryList
							optionValuePath="content.code"
							optionLabelPath="content.country"
							selectionBinding="mobilePhoneCountry" }}
					    </label>
					</div>
					<label>
						{{t forms.admin.mobilePhone}}
						{{#if requestMessages.error.mobilePhone}}
							{{ input type="text" value=mobilePhone classNames="error" }}
							<span class="message error">{{requestMessages.error.mobilePhone}}</span>
						{{else}}
							{{ input type="text" value=mobilePhone }}
						{{/if}}
					</label>
					<button class="small" {{action 'updateAdminAccount'}}>{{t forms.admin.update}}</button>
{{!-- 					<button class="button-hollow dim small">{{t forms.admin.cancel_button }}</button> --}}
				</form> 
		    </article>
		  </div>
		</div>
	</div>
</section>

