{{#modal-dialog}}
<div class="row modal-filler">
	<legend>{{t forms.cloudos.launch_title}}</legend>
</div>
<div class="row modal-content">
	<div class="large-12 columns">
		<p>{{t forms.cloudos.launch_string}} "{{ cloudOsRequest.name }}"</p>
	</div>
	<div class="large-10 columns">
		<div class="progress">
			<span id="progressMeter" class="meter" style="width:0%"></span>
		</div>
		<span><i>{{ statusMessage }}</i> </span>
	</div>
	<div class="large-2 columns">
		<button {{action 'backToAdmin'}} {{bind-attr class=":expand :alert isInProgress:el_hidden"}}>Close</button>
	</div>
</div>
<div class="row modal-filler">
	&nbsp;
</div>
{{/modal-dialog}}
