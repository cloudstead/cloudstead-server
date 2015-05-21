App.CountriesMixin = Ember.Mixin.create({
	countryList: Countries.sortedList(),
	mobilePhoneCountry: Ember.computed(function() {
			return Countries.findByCode(this.get('mobilePhoneCountryCode'));
		})
});
