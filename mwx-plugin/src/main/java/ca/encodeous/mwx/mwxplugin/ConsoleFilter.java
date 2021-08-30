package ca.encodeous.mwx.mwxplugin;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ConsoleFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        return !record.getMessage().contains("at BlockPosition");
    }
}
