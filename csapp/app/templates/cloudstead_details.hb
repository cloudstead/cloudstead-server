<section class="deck">
	<div class="row">
		<h3>Cloudstead {{ name }} details</h3>
		<div class="row">
			{{#if isInInitialState }}
				<button class="small" {{action "doLaunchCloudOs" name}} >{{ t cloudstead_info.actions.launch }}</button>
			{{/if}}
			{{#unless isInDestroyingState }}
				<button class="small" {{action "deleteInstance" this}}>{{ t cloudstead_info.actions.destroy }}</button>
			{{/unless}}
		</div>
		<p>Edition: {{ edition }}</p>
		<p>Region: {{ region }}</p>
		<p>Bundle: {{ appBundle }}</p>
		<ol>
			Apps:
			{{#each app in allApps}}
				<li>{{ app }}</li>
			{{/each}}
		</ol>
	</div>
</section>
