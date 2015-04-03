<section class="slide-canvas">
	<div class="row">
		<div class="large-8 large-centered columns">
			<div class="dashboard-container">
{{!-- 				<h2 class="delta light">{{ name }}</h2> --}}
					<article class="cloud-card">
						<h3>{{ name }}{{t app.domain }}</h3>
						<div class="field">
							<span class="field-name left">{{t forms.cloudos.region }}</span>
							<span class="field-value right">{{region}}</span>
						</div>
						<div class="field">
							<span class="field-name left">{{t forms.cloudos.edition }}</span>
							<span class="field-value right">{{edition}}</span>
						</div>
						<div class="field">
							<span class="field-name left">{{ t sections.admin.running }}</span>
							<span class="field-value right">{{ state }}</span>
						</div>
						<div class="field no-border">
							{{#if isInInitialState }}
								<button class="small" {{ action "doLaunchCloudOs" name }}>{{ t cloudstead_info.actions.launch }}</button>
							{{/if}}
							{{#unless isInDestroyingState }}
								<button class="alert small right" {{action "deleteInstance" this }}>{{ t cloudstead_info.actions.destroy }}</button>
							{{/unless}}
						</div>
					</article>
			</div>
		</div>
	</div>
</section>

