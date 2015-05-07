<div class="progress" data-dropdown="progressInfo" aria-expanded="false">
	<span id="progressMeter" class="meter" style="width:0%" xxx="1"></span>
</div>
<div id="progressInfo" data-dropdown-content class="f-dropdown content" aria-hidden="true" tabindex="-1">
	<p {{bindAttr class="hasErrorMessage:alert-box alert"}}>{{ statusMessage }}</p>
</div>
