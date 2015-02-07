package cloudos.cloudstead.main;

import org.cobbzilla.util.collection.ArrayUtil;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.cache.redis.ActivationCodeService;

public class ActivationCodeMain {

    public static void main (String[] args) {
        final String redisKey = CommandShell.execScript("cd ~cloudstead && cat .cloudstead.env | grep CLOUD_STORAGE_DATA_KEY | tr '=' ' ' | awk '{print $3}'").trim();
        ArrayUtil.append(args, redisKey);
        ActivationCodeService.main(args);
    }

}
