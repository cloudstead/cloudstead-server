<section class="deck">
	<div class="row">
		<div class="tabs-content">
			<div class="content active">
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
											<span class="icon-cloud"></span> {{ t cloudstead_info.actions.launch }}
										</a>
									{{/if}}
									{{#unless cloudosInstance.isInDestroyingState }}
										<a href="#" {{action "deleteInstance" cloudosInstance.name}}>
											<span class="icon-trash-o"></span> {{ t cloudstead_info.actions.destroy }}
										</a>
									{{/unless}}
								</td>
							</tr>
						{{/each}}
					</tbody>
				</table>
			</div>
		</div>
	</div>
</section>
