
<section class="slide-canvas">
	<div class="row">
		<div class="large-8 large-centered columns">
			<div class="dashboard-container">
				<h1 class="delta light">{{t topbar.profile }}</h1>
				<article>
				<form>
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
					<div class="button-bar">
						<button class="small left" {{action 'updateAdminAccount'}}>{{t forms.admin.update}}</button>
						<button class="small right alert" data-reveal-id="deleteModal">{{t forms.admin.delete}}</button>
					</div>
				</form> 
			</article>
		  </div>
		</div>
	</div>
</section>

<div id="deleteModal" class="reveal-modal narrow" data-reveal="" aria-labelledby="modalTitle" aria-hidden="true" role="dialog" style="/* display: none; */ opacity: 1; /* visibility: hidden; */ /* top: -463px; */">
	<div class="reveal-content">
		<h2 id="modalTitle" class="light text-center">{{t forms.admin.confirm}}</h2>
		<form class="text-center" {{action 'deleteAdminAccount' on="submit"}}>

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

			<button class="expand alert" {{action 'deleteAdminAccount'}} >{{t forms.admin.delete}}</button>
		</form>
		<a class="close-reveal-modal" aria-label="Close">×</a>
	</div>
</div>

