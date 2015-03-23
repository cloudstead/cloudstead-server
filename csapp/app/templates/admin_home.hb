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
						{{#each cloudosInstance in cloudosInstances }}
							<tr>
								<td>{{#link-to 'cloudOsStatus' cloudosInstance.name}}{{cloudosInstance.name}}{{/link-to}}</td>
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
				<form >
					<div class="field-container">
						<div class="row">
							<div class="small-2 columns">
								{{#if cloudosInstances}}
									<h3>
										<a href="#" {{action addMoreClouds}}>
											<span {{bind-attr class="isAddCloudsEnabled:icon-minus:icon-plus"}}></span>
										</a>
									</h3>
								{{else}}
									&nbsp;
								{{/if}}
							</div>
							<div {{bind-attr class=":large-5 :medium-12 :small-12 :columns isAddCloudsEnabled:el_visible:el_hidden"}}>
								<label for="right-label" class="right msg-mod">{{t forms.cloudos.name}}</label>
							</div>
							<div {{bind-attr class=":large-5 :medium-12 :small-12 :columns isAddCloudsEnabled:el_visible:el_hidden"}}>
								{{#if requestMessages.error.cloudOsName}}
									{{input type="text" value=cloudOsRequest.name class="error"}}
								{{else}}
									{{input type="text" value=cloudOsRequest.name}}
								{{/if}}

								{{#if requestMessages.error.cloudOsName}}
									<span class="message error">{{requestMessages.error.cloudOsName}}</span>
								{{/if}}
							</div>
						</div>
					</div>
					<div {{bind-attr class=":field-container isAddCloudsEnabled:el_visible:el_hidden"}}>
						<div class="row">
							<div class="large-7 hide-for-medium hide-for-small columns">
								&nbsp;
							</div>
							<div class="small-5 columns">
								<button id="cloudOsSubmit" type="submit" class="expand" {{action 'doNewCloudOs'}}>
									{{t forms.cloudos.new_button}}
								</button>
							</div>
						</div>
					</div>
					<div id="cloudOsCreating" class="field-container hide">
						<div class="row text-center">
							<img src="images/spinner.gif" />
						</div>
						<div class="row">
							&nbsp;
						</div>
						<div class="row text-center">
							{{t forms.cloudos.cloudos_init}}
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
</section>
