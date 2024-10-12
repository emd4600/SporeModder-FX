package sporemodder.view.ribbons;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import sporemodder.UIManager;
import sporemodder.view.ribbons.modandgit.ModAndGitActionsUI;
import sporemodder.view.ribbons.project.BasicActionsUI;
import sporemodder.view.ribbons.project.ModdingActionsUI;
import sporemodder.view.ribbons.project.OtherProjectRibbonUI;

public class ModAndGitRibbonTab extends RibbonTabController {
    public static final String ID = "MOD_AND_GIT";

    private ModAndGitActionsUI modAndGitActions;

    public void addTab(Ribbon ribbon) {
        tab = new RibbonTab("Git & Mod Publish");
        ribbon.getTabs().add(tab);

        modAndGitActions = UIManager.get().loadUI("ribbons/modandgit/ModAndGitActionsUI");

        tab.getGroups().add((RibbonGroup) modAndGitActions.getMainNode());
    }

    public ModAndGitActionsUI getModAndGitActionsUI() {
        return modAndGitActions;
    }
}
