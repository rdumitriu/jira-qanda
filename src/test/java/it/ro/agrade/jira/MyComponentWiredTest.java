package it.ro.agrade.jira;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
//import ro.agrade.jira.MyPluginComponent;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest
{
    private final ApplicationProperties applicationProperties;
    //private final MyPluginComponent myPluginComponent;

    public MyComponentWiredTest(ApplicationProperties applicationProperties) { //,MyPluginComponent myPluginComponent) {
        this.applicationProperties = applicationProperties;
        //this.myPluginComponent = myPluginComponent;
    }

    @Test
    public void testMyName() {
        //assertEquals("names do not match!", "myComponent:" + applicationProperties.getDisplayName(),myPluginComponent.getName());
    }
}