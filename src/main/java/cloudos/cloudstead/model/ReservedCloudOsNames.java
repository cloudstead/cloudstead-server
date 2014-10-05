package cloudos.cloudstead.model;

import org.cobbzilla.wizard.validation.ReservedWords;

public class ReservedCloudOsNames implements ReservedWords {

    public static final String[] RESERVED_NAMES = {
            "www", "wwws", "http", "https",
            "mail", "email", "mailbox", "mbox", "smtp", "imap", "pop", "pop3",
            "corp", "blog", "news",
            "app", "apps", "appstore",
            "mobile", "root", "postmaster", "admin",
            "cloudos", "upcloud", "cloudstead"
    };

    @Override public String[] getReservedWords() { return RESERVED_NAMES; }

    public static boolean isReserved (String s) {
        for (String w : RESERVED_NAMES) {
            if (w.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}
