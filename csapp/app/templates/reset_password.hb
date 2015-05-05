<section class="deck white-background" style="min-height:900px;">
	<div class="row">
		<div class="medium-9 large-6 medium-centered columns">
			<div class="stepper dim">
				<h2 class="light">{{t forms.reset_password.label}}</h2>
				<form {{action 'doResetPassword' on="submit"}}>
						<div class="field-container">
							<label for="password">{{t forms.admin.password}}</label>
							{{#if requestMessages.error.password}}
								{{input type="password" value=password class="error"}}
							{{else}}
								{{input type="password" value=password}}
							{{/if}}

							{{#if requestMessages.error.password}}
								<span class="message error">{{requestMessages.error.password}}</span>
							{{/if}}
						</div>
						<div class="field-container">
							<label for="password_confirm">{{t forms.admin.password2}}</label>
							{{#if requestMessages.error.passwordConfirm}}
								{{input type="password" value=passwordConfirm class="error"}}
							{{else}}
								{{input type="password" value=passwordConfirm}}
							{{/if}}

							{{#if requestMessages.error.passwordConfirm}}
								<span class="message error">{{requestMessages.error.passwordConfirm}}</span>
							{{/if}}
						</div>
						<div class="field-container">
							<p><button type="submit" class="expand">{{t forms.reset_password.label}}</button></p>
						</div>
						<div class="forgot-password-notification">{{notificationResetPassword}}</div>
				</form>
			</div>
		</div>
	</div>
</section>


{{!-- <section class="deck white-background" style="min-height:900px;">
  <div class="row">

    <div class="medium-9 large-6 medium-centered columns">

  <div class="stepper dim">
    <h2 class="light">Sign In</h2>
    <form class="text-left">
      <label>
      Email
		  <input type="text" placeholder="Email">
	</label>

		<label>Input Label
		  <input type="text" placeholder="Password">
		</label>
		<a href="#" class="input-link"><small>Forgot Password?</small></a>

		<button class="expand">Sign In</button>

    </form>
  </div>
</div>

  </div>
</section><section class="deck white-background" style="min-height:900px;">
  <div class="row">

    <div class="medium-9 large-6 medium-centered columns">

  <div class="stepper dim">
    <h2 class="light">Sign In</h2>
    <form class="text-left">
      <label>
      Email
		  <input type="text" placeholder="Email">
	</label>

		<label>Input Label
		  <input type="text" placeholder="Password">
		</label>
		<a href="#" class="input-link"><small>Forgot Password?</small></a>

		<button class="expand">Sign In</button>

    </form>
  </div>
</div>

  </div>
</section> --}}


