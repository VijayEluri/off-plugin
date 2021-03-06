package net.sickill.off;

import net.sickill.off.common.OffListModel;
import net.sickill.off.common.ProjectFile;
import net.sickill.off.common.AbstractProject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author sickill
 */
class FakeProject extends AbstractProject {

    static String PROJECT_ROOT = "/tmp/project-x/";
    private Collection<ProjectFile> col;

    public FakeProject() {
        col = new ArrayList<>();

        String[] names = {
            "README",
            "Rakefile",
            "app/models/user.rb",
            "app/models/topic.rb",
            "lib/tags.rb",
            "spec/helper.rb",
            "spec/hlr.rb",
            "app/views/users/index.html",
            "app/controllers/users_controller.rb",
            "app/views/topics/index.html",
            "spec/models/user_test.rb",
            "spec/zone.cfg",
            "app/views/elements/index.html",
            "app/models/user_topic.rb",
            "jola/Thumbs.db",
            "gems/gems/jola.pl"
        };

        for (String name : names) {
            col.add(new FakeProjectFile(name));
        }
    }

    public ProjectFile getFileByName(String n) {
        for (ProjectFile pf : col) {
            if (pf.getFileName().equals(n)) {
                return pf;
            }
        }

        return null;
    }

    @Override
    public void init(OffListModel model) {
        this.model = model;
        final List<String> sourceGroups = Collections.singletonList(PROJECT_ROOT);
        String projectRootDir = PROJECT_ROOT;

        this.model.reinit(sourceGroups);
        for (ProjectFile pf : col) {
            this.model.addFile(pf);
        }
    }

}
