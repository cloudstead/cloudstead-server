package cloudos.cloudstead.dao;

import org.cobbzilla.util.collection.MapBuilder;
import org.cobbzilla.wizard.dao.AbstractUniqueCRUDDAO;
import org.cobbzilla.wizard.validation.UniqueValidatorDaoHelper;
import org.springframework.stereotype.Repository;
import cloudos.cloudstead.model.Admin;

import java.util.Map;

@Repository
public class AdminDAO extends AbstractUniqueCRUDDAO<Admin> {

    @Override
    protected Map<String, UniqueValidatorDaoHelper.Finder<Admin>> getUniqueHelpers() {
        return MapBuilder.build(new Object[][]{
                {"email", new UniqueValidatorDaoHelper.Finder<Admin>() {
                    @Override public Admin find(Object query) { return findByEmail(query.toString()); }
                }},
                {"mobilePhone", new UniqueValidatorDaoHelper.Finder<Admin>() {
                    @Override public Admin find(Object query) { return findByMobilePhone(query.toString()); }
                }},
        });
    }

    public Admin findByEmail(String email) { return findByUniqueField("email", email); }

    public Admin findByMobilePhone(String mobilePhone) { return findByUniqueField("mobilePhone", mobilePhone); }

}
