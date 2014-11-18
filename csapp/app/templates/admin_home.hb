<section class="deck">
	<div class="row">
		<dl class="tabs" data-tab>
			<dd class="active half-width-tab">{{#link-to 'adminHome'}}Your Cloudsteads{{/link-to}}</dd>
			<dd class="half-width-tab">{{#link-to 'adminDetails' }}Account details{{/link-to}}</dd>
		</dl>
		<div class="tabs-content">
			<div class="content active">
				{{#if cloudosInstances}}
				<table  width="100%">
					<thead>
						<tr>
							<th>Name
							</th>
							<th>Running
							</th>
							<th>Control
							</th>
						</tr>
					</thead>
					<tbody>
						{{#each cloudosInstances}}
						<tr>
							<td>{{#link-to 'cloudOsStatus' name}}{{name}}{{/link-to}}</td>
							<td>{{running}}</td>
							<td><a href="#" {{action "deleteInstance" name}}><span class="icon-trash-o"></span> Delete</a></td>
						</tr>
						{{/each}}
					</tbody>
				</table>
				{{else}}
					<article class="event-single">
						<p>You have no running Cloudsteads. Click the 'Start Cloudstead' button below to launch one.</p>
					</article>
				{{/if}}
				<form >
					<div class="field-container">
						<div class="row">
							<div class="small-2 columns">
								{{#if cloudosInstances}}
									<h3><a href="#" {{action addMoreClouds}}><span {{bind-attr class="isAddCloudsEnabled:icon-minus:icon-plus"}}></span></a></h3>
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
								<button id="cloudOsSubmit" type="submit" class="expand" {{action 'doNewCloudOs'}}>{{t forms.cloudos.new_button}}</button>
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
