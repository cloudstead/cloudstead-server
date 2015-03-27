<div id="cloudstead-content">
{{#view App.NavbarView}}
	<section class="off-canvas-wrap bg-dim">
		<div class="inner-wrap">
			<section class="app-bar">
				<nav class="top-bar" data-top-bar>
					<ul class="title-area">
						<li class="name">
							<h1>
								<a class="brand" href="#">{{t app.title}}</a>
							</h1>
						</li>
						<li class="toggle-topbar menu-icon">
							<a href="#">
								<span></span>
							</a>
						</li>
					</ul>
					{{#if authStatus}}
						<section class="top-bar-section show-for-medium-up left">
							<ul class="left">
								<li>{{#link-to 'dashboard' }}{{ t topbar.dashboard }}{{/link-to}}</li>
								<li>{{#link-to 'dashboard' }}{{ t topbar.community }}{{/link-to}}</li>
								<li>{{#link-to 'adminDetails' }}{{ t topbar.profile }}{{/link-to}}</li>
								<li class="button-item">
									{{#link-to 'new_cloudstead' classNames='button secondary tiny' }}
										{{ t topbar.new_cloudstead }}
									{{/link-to}}
								</li>
							</ul>
						</section>
						<section class="top-bar-section show-for-medium-up right">
								<ul class="right">
									<li class='button-item'>
										{{#link-to 'logout' classNames="button button-hollow tiny"}}
											{{ t topbar.logout }}
										{{/link-to}}
									</li>
								</ul>
						</section>
					{{/if}}
				</nav>
			</section>
		</div>
	</section>
{{/view}}

{{outlet}}
{{outlet modal}}
</div>

<footer class="footer">
	<section class="row">
		<div class="row">
			<div class="large-12 columns">
				<ul class="inline-list footer-list">
					<li>
						<span class="copy sans">&copy; 2014 Cloudstead</span>
					</li>
					<li>
						<a href="http://www.cloudstead.io/privacy-policy">Privacy Policy</a>
					</li>
					<li>
						<a href="http://www.cloudstead.io/terms-of-service">Terms of Service</a>
					</li>
				</ul>
			</div>
		</div>
	</section>
</footer>
