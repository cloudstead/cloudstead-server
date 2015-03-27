<section class="deck">
	<div class="row">
		<form >
			<div class="large-6 large-centered medium-12 small-12 columns">
				<div class="row">
					<div>
						<label class="msg-mod">{{t forms.cloudos.name}}</label>
					</div>
				</div>
				<div class="row">
					<div>
						{{#if requestMessages.error.cloudOsName}}
							{{input type="text" value=name class="error"}}
						{{else}}
							{{input type="text" value=name}}
						{{/if}}

						{{#if requestMessages.error.cloudOsName}}
							<span class="message error">{{requestMessages.error.cloudOsName}}</span>
						{{/if}}
					</div>
				</div>
				<div class="row">
					<div>
						<label class="msg-mod">{{t forms.cloudos.region}}</label>
					</div>
				</div>
				<div class="row">
									{{view Ember.Select
										content=regionList
										optionValuePath="content.value"
										optionLabelPath="content.label"
										selectionBinding="selectedRegion" }}
				</div>
				<div class="row">
					<div>
						<label class="msg-mod">{{t forms.cloudos.edition}}</label>
					</div>
				</div>
				<div class="row">
									{{view Ember.Select
										content=editionList
										optionValuePath="content.value"
										optionLabelPath="content.label"
										selectionBinding="selectedEdition" }}
				</div>
				<div class="row">
					<div>
						<label class="msg-mod">{{t forms.cloudos.app_bundle}}</label>
					</div>
				</div>
				<div class="row">
									{{view Ember.Select
										content=bundleList
										optionValuePath="content.value"
										optionLabelPath="content.label"
										selectionBinding="selectedBundle" }}
				</div>
				<div class="row">
					&nbsp;
				</div>
				<div class="row">
					<div class="large-6 large-centered medium-12 small-12 columns">
						<button id="cloudOsSubmit" type="submit" class="expand" {{action 'doNewCloudOs'}}>
							{{t forms.cloudos.new_button}}
						</button>
					</div>
				</div>
			</div>
		</form>
	</div>
</section>
