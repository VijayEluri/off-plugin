package net.sickill.off.jedit;

import javax.swing.Icon;
import net.sickill.off.common.AbstractProject;
import net.sickill.off.common.ProjectFile;
import projectviewer.vpt.VPTFile;

/**
 * @author sickill
 */
public class JEditProjectViewerFile extends ProjectFile {

  VPTFile file;

  public JEditProjectViewerFile(AbstractProject pp, VPTFile node) {
    super(pp);
    file = node;
  }

  @Override
  public Icon getIcon() {
    return file.getIcon(false);
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public String getFullPath() {
    return file.getNodePath();
  }

  @Override
  public long getSize() {
    return file.getFile().getLength();
  }

  @Override
  public void rename(String newName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getId() {
    return file;
  }

  public VPTFile getFile() {
    return file;
  }

}
