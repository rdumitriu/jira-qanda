/*
 * Created on 11/25/13
 */
package ro.agrade.jira.qanda.plugin;

import java.util.*;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import ro.agrade.jira.qanda.Answer;
import ro.agrade.jira.qanda.Question;
import ro.agrade.jira.qanda.dao.AnswerDataServiceImpl;
import ro.agrade.jira.qanda.dao.QuestionDataServiceImpl;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Migrate the keys stored by mistake in the user columns to usernames
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class UpgradeTask1_0_9 implements PluginUpgradeTask {
    private static final Log LOG = LogFactory.getLog(UpgradeTask1_0_9.class);

    private final UserManager userManager;

    public UpgradeTask1_0_9(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        LOG.debug("Upgrading QandA mapping from user key to username");
        try {
            migrateUsers();
        } catch(Throwable t) {
            LOG.error("Failed to migrate users, but it's not that bad.", t);
        }

        return new ArrayList<Message>();
    }

    private void migrateUsers() {
        // This segment of code is quite rough, but it should work.
        Set<String> users = new TreeSet<String>(); //unique set of user keys
        List<Question> questions = scanQuestions(users);
        List<Answer> answers = scanAnswers(users);
        //we have the user keys, let's see what has changed
        Map<String, String> diffKeyUName = calculateDifference(users);
        if(diffKeyUName.size() > 0) {
            LOG.info(String.format("Upgrade task: There are %d users to be touched.",
                    diffKeyUName.size()));
            //do the change
            processQuestions(questions, diffKeyUName);
            processAnswers(answers, diffKeyUName);
        } else {
            LOG.info("Upgrade task: luckily for you, no users need to be touched");
        }
    }

    private void processAnswers(List<Answer> answers, Map<String, String> diffKeyUName) {
        if(answers == null) {
            return;
        }
        AnswerDataServiceImpl impl = new AnswerDataServiceImpl(null);
        for(Answer a : answers) {
            String key = a.getUser();
            String newName = diffKeyUName.get(key);
            if(newName != null) {
                if(LOG.isDebugEnabled()){
                    LOG.debug(String.format("Changing owner of answer %s from %s to %s",
                            a.getAnswerId(), key, newName));
                }
                impl.changeUser(a, newName);
            }
        }
    }

    private void processQuestions(List<Question> questions, Map<String, String> diffKeyUName) {
        if(questions == null) {
            return;
        }
        QuestionDataServiceImpl impl = new QuestionDataServiceImpl(null);
        for(Question q : questions) {
            String key = q.getUser();
            String newName = diffKeyUName.get(key);
            if(newName != null) {
                if(LOG.isDebugEnabled()){
                    LOG.debug(String.format("Changing owner of question %s from %s to %s",
                            q.getId(), key, newName));
                }
                impl.changeUser(q, newName);
            }
        }
    }

    private Map<String, String> calculateDifference(Set<String> users) {
        Map<String, String> ret = new HashMap<String, String>();
        for(String s : users) {
            ApplicationUser u = JIRAUtils.toUserObject(userManager, s);
            if(u != null && !u.getKey().equals(u.getName())) {
                if(LOG.isDebugEnabled()){
                    LOG.debug(String.format("Found user mismatch key:%s username:%s",
                            u.getKey(), u.getName()));
                }
                ret.put(u.getKey(), u.getName());
            }
        }
        return ret;
    }

    private List<Answer> scanAnswers(Set<String> users) {
        AnswerDataServiceImpl impl = new AnswerDataServiceImpl(null);
        List<Answer> answers = impl.getAllAnswers();
        if(answers != null) {
            for(Answer a : answers) {
                users.add(a.getUser());
            }
        }
        return answers;
    }

    private List<Question> scanQuestions(Set<String> users) {
        QuestionDataServiceImpl impl = new QuestionDataServiceImpl(null);
        List<Question> questions = impl.getAllQuestions();
        if(questions != null) {
            for(Question q : questions) {
                users.add(q.getUser());
            }
        }
        return questions;
    }

    @Override
    public int getBuildNumber() {
        return 1;
    }

    @Override
    public String getShortDescription() {
        return "Migrates questions and answers which contain the wrong user keys";
    }

    @Override
    public String getPluginKey() {
        return "ro.agrade.jira.qanda-pro";
    }
}
