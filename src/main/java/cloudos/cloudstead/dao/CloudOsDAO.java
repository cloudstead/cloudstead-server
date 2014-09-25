package cloudos.cloudstead.dao;

import org.cobbzilla.util.collection.MapBuilder;
import org.cobbzilla.wizard.dao.AbstractUniqueCRUDDAO;
import org.cobbzilla.wizard.validation.UniqueValidatorDaoHelper;
import org.springframework.stereotype.Repository;
import cloudos.cloudstead.model.CloudOs;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Repository
public class CloudOsDAO extends AbstractUniqueCRUDDAO<CloudOs> {

    @Override
    protected Map<String, UniqueValidatorDaoHelper.Finder<CloudOs>> getUniqueHelpers() {
        return MapBuilder.build(new Object[][]{
                {"name", new UniqueValidatorDaoHelper.Finder<CloudOs>() {
                    @Override public CloudOs find(Object query) { return findByName(query.toString()); }
                }}
        });
    }

    @Override
    public Object preCreate(@Valid CloudOs entity) {
        entity.setName(entity.getName().toLowerCase());
        return super.preCreate(entity);
    }

    @Override
    public Object preUpdate(@Valid CloudOs entity) {
        entity.setName(entity.getName().toLowerCase());
        return super.preUpdate(entity);
    }

    public CloudOs findByName(String name) { return findByUniqueField("name", name.toLowerCase()); }

    public List<CloudOs> findByAdmin(String uuid) { return findByField("adminUuid", uuid); }

}
