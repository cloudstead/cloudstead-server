<section class="deck">
	<div class="row">
		<dl class="tabs" data-tab>
			<dd class="active half-width-tab admin_home_tab">
				{{#link-to 'adminHome'}}
					{{t sections.admin.your_cloudsteads}}
				{{/link-to}}
			</dd>
			<dd class="half-width-tab">
				{{#link-to 'adminDetails' }}
					{{t sections.admin.account_details}}
				{{/link-to}}
			</dd>
		</dl>
		<div class="tabs-content">
			<div class="content active">
				{{#if cloudosInstances}}
				<table  width="100%">
					<thead>
						<tr>
							<th>{{t sections.admin.name}}</th>
							<th>{{t sections.admin.running}}</th>
							<th>{{t sections.admin.control}}</th>
						</tr>
					</thead>
					<tbody>
						{{#each cloudosInstance in arrangedContent }}
							<tr>
								<td>{{#link-to 'cloudstead_details' cloudosInstance.name}}{{cloudosInstance.name}}{{/link-to}}</td>
								<td>
									{{ cloudosInstance.state }}
								</td>
								<td>
									{{#if cloudosInstance.isInInitialState }}
										<a href="#" {{ action "doLaunchCloudOs" cloudosInstance.name }}>
											<span class="icon-cloud"></span> {{t sections.admin.launch}}
										</a>
									{{/if}}
									{{#unless cloudosInstance.isInDestroyingState }}
										<a href="#" {{action "deleteInstance" cloudosInstance.name}}>
											<span class="icon-trash-o"></span> {{t sections.admin.delete}}
										</a>
									{{/unless}}
								</td>
							</tr>
						{{/each}}
					</tbody>
				</table>
				{{else}}
					<article class="event-single">
						<p>{{t sections.admin.no_cloudsteads_message}}</p>
					</article>
				{{/if}}
			</div>
		</div>
	</div>
</section>
