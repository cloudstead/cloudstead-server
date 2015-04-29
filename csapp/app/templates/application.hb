<section class="off-canvas-wrap bg-dim" data-offcanvas="">
  <div class="inner-wrap">
		{{#view App.NavbarView}}
			{{#unless isHome}}
			<section class="app-bar">
				<nav class="top-bar" data-top-bar="">
					<section class="tab-bar-section show-for-small-only">
						<a class="left-off-canvas-toggle menu-icon" href="#"><span class="icon-bars"></span></a>
					</section>
					{{#if authStatus}}
						<section class="top-bar-section show-for-medium-up left">
							<ul class="left">
								<li>{{#link-to 'dashboard' }}{{ t topbar.dashboard }}{{/link-to}}</li>
								<li><a>{{ t topbar.community }}</a></li>
								<li>{{#link-to 'adminDetails' }}{{ t topbar.profile }}{{/link-to}}</li>
								<li class="button-item">
									{{#link-to 'new_cloudstead' class='button secondary tiny' }}
										{{ t topbar.new_cloudstead }}
									{{/link-to}}
								</li>
								<li>
									{{ outlet progressbar }}
								</li>	
							</ul>
						</section>

						<section class="top-bar-section show-for-medium-up right">
							<ul>
								<li><a href="#"></a></li>
								<li class="button-item">
									{{#link-to 'logout' classNames="button button-hollow tiny"}}
										{{ t topbar.logout }}
									{{/link-to}}
								</li>
							</ul>
						</section>
					{{else}}
						<section class="top-bar-section show-for-medium-up left">
							<ul class="left">
								<li>{{#link-to 'index' }}{{ t app.title }}{{/link-to}}</li>
							</ul>
						</section>	
					{{/if}}
				</nav>
			</section>
			{{#if authStatus}}
				<aside class="left-off-canvas-menu">
					<ul class="off-canvas-list">
						<li><label>{{ t sections.admin.my_cloudstead }}</label></li>
						<li>{{#link-to 'dashboard' }}{{ t topbar.dashboard }}{{/link-to}}</li>
						<li>{{#link-to 'dashboard' }}{{ t topbar.community }}{{/link-to}}</li>
						<li>{{#link-to 'adminDetails' }}{{ t topbar.profile }}{{/link-to}}</li>
						<li>{{#link-to 'new_cloudstead'}}{{ t topbar.new_cloudstead }}{{/link-to}}</li>
						<li>{{#link-to 'logout'}}{{ t topbar.logout }}{{/link-to}}</li>
					</ul>
				</aside>
			{{else}}
			
				<aside class="left-off-canvas-menu">
					<ul class="off-canvas-list">
						<li>{{#link-to 'index' }}{{ t app.title }}{{/link-to}}</li>
					</ul>
				</aside>
			{{/if}}

			{{/unless}}
		{{/view}}

		{{outlet}}
		{{outlet modal}}
	</div>
</section>

<footer class="footer">	
	<section class="sock">
		<div class="row">
			<div class="large-10 columns">
				<p class="copy sans">
					&copy; {{ t app.copyright }} &nbsp; | &nbsp; 
					<a href="http://www.cloudstead.io/privacy-policy" target="_blank">{{ t sections.footer.privacy_policy }}</a> &nbsp; | &nbsp;
					<a href="http://www.cloudstead.io/terms-of-service" target="_blank">{{ t sections.footer.terms_of_service }}</a>
				</p>
			</div>
			
			<div class="large-2 columns">
				<div class="social show-for-medium-up">
					<span class="icon-twitter-circle"></span>
					<span class="icon-facebook-circle"></span>
					<span class="icon-google-circle"></span>
				</div>
			</div>
		</div>
	</section>
</footer>
