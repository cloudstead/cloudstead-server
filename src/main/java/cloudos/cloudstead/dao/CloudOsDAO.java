package cloudos.cloudstead.dao;

import cloudos.cloudstead.model.CloudOs;
import org.cobbzilla.wizard.dao.UniquelyNamedEntityDAO;
import org.springframework.stereotype.Repository;

import javax.validation.Valid;
import java.util.List;

@Repository
public class CloudOsDAO extends UniquelyNamedEntityDAO<CloudOs> {

    @Override public Object preCreate(@Valid CloudOs entity) {
        entity.setName(entity.getName().toLowerCase());
        return super.preCreate(entity);
    }

    @Override public Object preUpdate(@Valid CloudOs entity) {
        entity.setName(entity.getName().toLowerCase());
        return super.preUpdate(entity);
    }

    public CloudOs findByName(String name) { return findByUniqueField("name", name.toLowerCase()); }

    public List<CloudOs> findByAdmin(String uuid) { return findByField("adminUuid", uuid); }

}
