<section class="slide-canvas">
	<div class="row">
		<div class="large-8 large-centered columns">
			<div class="dashboard-container">
				<h2 class="delta light">{{t sections.admin.cloudsteads }}</h2>
				{{#each cloudosInstance in arrangedContent }}
					<article class="cloud-card">
						<h3 class="epsilon">
							{{cloudosInstance.name}}{{t app.domain}}
						</h3>
						<div class="field">
							<span class="field-name left">{{t forms.cloudos.region }}</span>
							<span class="field-value right">{{cloudosInstance.region}}</span>
						</div>
						<div class="field">
							<span class="field-name left">{{t forms.cloudos.edition }}</span>
							<span class="field-value right">{{cloudosInstance.edition}}</span>
						</div>
						<div class="field">
							<span class="field-name left">{{ t sections.admin.running }}</span>
							<span class="field-value right">{{ cloudosInstance.state }}</span>
						</div>
						<div class="field no-border">
							{{#link-to 'cloudstead_details' cloudosInstance.name class="button button-hollow dim small"}}
								{{t forms.admin.edit }}
							{{/link-to}}						
						</div>
					</article>
				{{/each}}
			</div>
		</div>
	</div>
</section>
