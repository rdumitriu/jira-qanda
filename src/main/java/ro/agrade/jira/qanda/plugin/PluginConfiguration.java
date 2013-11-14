/*
 * Created on 11/13/13
 */
package ro.agrade.jira.qanda.plugin;

import java.util.*;
import java.io.*;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the plugin configuration,
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class PluginConfiguration {
    private static final Log LOG = LogFactory.getLog(PluginConfiguration.class);

    private static final String QANDA_CONFIGURATION = "qanda.properties";

    private Properties props;

    public PluginConfiguration() {
        props = new Properties();
        initFromFile();
    }

    private void initFromFile() {
        File mainDir = ComponentAccessor.getComponent(JiraHome.class).getHome();
        File qandaConfigFile = new File(mainDir, QANDA_CONFIGURATION);
        if(qandaConfigFile.exists() && qandaConfigFile.canRead()) {
            InputStream is = null;
            try {
                is = new FileInputStream(qandaConfigFile);
                props.load(is);
            } catch(IOException e) {
                LOG.warn("Could not load QandA properties, will set them to defaults", e);
            } finally {
                if(is != null) try { is.close(); } catch(IOException e) {}
            }
        } else {
            LOG.info(String.format("No configuration file found at '%s', will use defaults.",
                                   qandaConfigFile.getAbsolutePath()));
        }
    }

    public String getStringConfig(String key, String defaultValue) {
        String s = props.getProperty(key);
        return s != null ? s.trim() : defaultValue;
    }

    public int getIntConfig(String key, int defaultValue) {
        String s = props.getProperty(key);
        if(s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch(NumberFormatException e) {
            LOG.warn(String.format("Property %s is not an integer, reverting to default %d",
                                   key, defaultValue));
        }
        return defaultValue;
    }

    public boolean getBooleanConfig(String key, boolean defaultValue) {
        String s = props.getProperty(key);
        if(s == null) {
            return defaultValue;
        }
        return "t".equals(s) || "true".equals(s) || "1".equals(s);
    }
}
