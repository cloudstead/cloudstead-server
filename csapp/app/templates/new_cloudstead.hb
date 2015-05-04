<section class="slide-canvas">

	<div class="row hide" id="progress-bar-block">
		<div class="large-8 large-centered columns">
		<div data-alert="" class="alert-box info radius margin-top">
			Please wait while your cloustead is building.
			<a href="#" class="close">×</a>
		</div>
		<div class="dashboard-container">
			<h1 class="delta light">Progress</h1>
			<div class="progress">
			  <span id="cs-progress-bar" class="meter" style="width: 0%"></span>
			  <small id="cs-progress-text" class="status">0% complete</small>
			</div>
		</div>
		</div>
	</div>

	<div class="row">
		<div class="large-8 large-centered columns">
			<div class="dashboard-container">
	            <h1 class="delta light">{{ t topbar.new_cloudstead }} </h1>
	            <article>
					<form>
						<div class="field-container">
							<label>
								{{t forms.cloudos.name}}
								{{#if requestMessages.error.cloudOsName}}
									{{input type="text" value=name class="error"}}
									<span class="message error">{{requestMessages.error.cloudOsName}}</span>
								{{else}}
									{{input type="text" value=name}}
								{{/if}}
							</label>						 
						</div>
						<div class="field-container">
							<label>
								{{t forms.cloudos.region}}
								{{view Ember.Select
									content=regionList
									optionValuePath="content.value"
									optionLabelPath="content.label"
									selectionBinding="selectedRegion" }}
							</label>
						</div>
						<div class="field-container">
							<label>
								{{t forms.cloudos.edition}}
								{{view Ember.Select
									content=editionList
									optionValuePath="content.value"
									optionLabelPath="content.label"
									selectionBinding="selectedEdition" }}
							</label>
						</div>
						<div class="field-container">
							<label>
								{{t forms.cloudos.app_bundle}}
								{{view Ember.Select
									content=bundleList
									optionValuePath="content.value"
									optionLabelPath="content.label"
									selectionBinding="selectedBundle" }}
							</label>
{{!-- 							<a href="" class="note-up"><small>Add Additional Apps</small></a> --}}
						</div>
						<button class="small" {{action 'doNewCloudOs'}} >{{t forms.cloudos.new_button}}</button>
					</form> 
				</article>
			</div>
		</div>
	</div>
</section>
