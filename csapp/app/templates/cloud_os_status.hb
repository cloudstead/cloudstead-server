<div class="row">
	&nbsp;
</div>
<div class="row">
	<div class="large-4 large-centered columns">
		<table class='table'>
			<thead>
			<tr>
				<td>message</td>
				<td>time</td>
				<td>error?</td>
			</tr>
			</thead>
			<tr>
				<td><button class="button" {{action 'doRelaunchCloudOs'}}>{{t forms.cloudos.relaunch_button}}</button></td>
			</tr>
			{{#each status.history}}
			<tr>
				<td>{{messageKey}}</td>
				<td>{{timestamp}}</td>
				<td>{{error}}</td>
			</tr>
			{{/each}}
		</table>
	</div>
</div>
