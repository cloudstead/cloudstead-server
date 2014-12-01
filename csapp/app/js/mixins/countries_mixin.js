App.CountriesMixin = Ember.Mixin.create({
	countryList: Countries.list,
	mobilePhoneCountry: Ember.computed(function() {
			return Countries.findByCode(this.get('mobilePhoneCountryCode'));
		})
});
