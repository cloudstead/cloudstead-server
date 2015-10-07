package cloudos.cloudstead.dao;

import cloudos.cloudstead.model.Admin;
import cloudos.dao.BasicAccountDAO;
import cloudos.model.auth.AuthenticationException;
import cloudos.model.auth.LoginRequest;
import org.cobbzilla.util.collection.MapBuilder;
import org.cobbzilla.wizard.dao.UniquelyNamedEntityDAO;
import org.cobbzilla.wizard.validation.UniqueValidatorDaoHelper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AdminDAO extends UniquelyNamedEntityDAO<Admin> implements BasicAccountDAO<Admin> {

    public Admin findByActivationKey(String key) { return findByUniqueField("emailVerificationCode", key); }

    public Admin findByResetPasswordToken(String key) { return findByUniqueField("hashedPassword.resetToken", key); }

    public void setPassword(Admin account, String newPassword) {
        account.getHashedPassword().setPassword(newPassword);
        account.getHashedPassword().setResetToken(null);
        update(account);
    }

    public List<Admin> findAdmins() { return findByField("admin", true); }

    public Admin authenticate(LoginRequest loginRequest) throws AuthenticationException {

        final String accountName = loginRequest.getName();
        final String password = loginRequest.getPassword();

        final Admin admin = findByName(accountName);
        if (admin == null || !admin.getHashedPassword().isCorrectPassword(password)) {
            throw new AuthenticationException(AuthenticationException.Problem.NOT_FOUND);
        }

        return admin;
    }

    @Override public Admin findByName(String name) {
        final Admin found = super.findByName(name);
        return found != null ? found : findByUniqueField("email", name);
    }

    @Override
    protected Map<String, UniqueValidatorDaoHelper.Finder<Admin>> getUniqueHelpers() {

        final Map<String, UniqueValidatorDaoHelper.Finder<Admin>> helpers = super.getUniqueHelpers();

        helpers.putAll(MapBuilder.<String, UniqueValidatorDaoHelper.Finder<Admin>>build(new Object[][]{
                {"email", new UniqueValidatorDaoHelper.Finder<Admin>() {
                    @Override public Admin find(Object query) { return findByEmail(query.toString()); }
                }},
                {"mobilePhone", new UniqueValidatorDaoHelper.Finder<Admin>() {
                    @Override public Admin find(Object query) { return findByMobilePhone(query.toString()); }
                }},
        }));

        return helpers;
    }

    public Admin findByEmail(String email) { return findByUniqueField("email", email); }

    public Admin findByMobilePhone(String mobilePhone) { return findByUniqueField("mobilePhone", mobilePhone); }

}
