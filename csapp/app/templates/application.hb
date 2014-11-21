<div id="cloudstead-content">
{{#view App.NavbarView}}
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
		<section class="top-bar-section">
			<ul class="right">
				{{#if authStatus}}
					<li>{{#link-to 'logout'}}Sign out{{/link-to}}</li>
				{{else}}
					<!--<li>{{#link-to 'login'}}Sign in{{/link-to}}</li>-->
				{{/if}}
			</ul>
		</section>
	</nav>
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
