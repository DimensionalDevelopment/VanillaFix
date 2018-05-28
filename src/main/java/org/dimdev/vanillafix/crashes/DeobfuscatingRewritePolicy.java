package org.dimdev.vanillafix.crashes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.ArrayList;
import java.util.List;

public class DeobfuscatingRewritePolicy implements RewritePolicy {
    @Override
    public LogEvent rewrite(LogEvent source) {
        if (source.getThrown() != null) StacktraceDeobfuscator.deobfuscateThrowable(source.getThrown());
        return source;
    }

    /** Modifies the log4j config to add the policy **/
    public static void install() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        LoggerConfig loggerConfig = rootLogger.get();

        // Remove appender refs from config
        List<AppenderRef> appenderRefs = new ArrayList<>(loggerConfig.getAppenderRefs());
        for (AppenderRef appenderRef : appenderRefs) {
            loggerConfig.removeAppender(appenderRef.getRef());
        }

        // Create the RewriteAppender, which wraps the appenders
        RewriteAppender rewriteAppender = RewriteAppender.createAppender(
                "VanillaFixDeobfuscatingAppender",
                "true",
                appenderRefs.toArray(new AppenderRef[0]),
                rootLogger.getContext().getConfiguration(),
                new DeobfuscatingRewritePolicy(),
                null
        );
        rewriteAppender.start();

        // Add the new appender
        loggerConfig.addAppender(rewriteAppender, null, null);
    }
}
