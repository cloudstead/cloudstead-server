package cloudos.cloudstead.server;

import lombok.AllArgsConstructor;
import org.cobbzilla.wizard.cache.redis.RedisConfiguration;

@AllArgsConstructor
public class CloudsteadRedisConfiguration extends RedisConfiguration {

    private CloudsteadConfiguration configuration;

    @Override public String getKey() { return configuration.getCloudConfig().getDataKey(); }

}
