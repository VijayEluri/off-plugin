package net.sickill.off.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.swing.AbstractListModel;

/**
 *
 * @author kill
 */
public class OffListModel extends AbstractListModel<OffListElement> {
    private static final long serialVersionUID = 7121724322112004624L;
    public static int MAX_RESULTS = 50;
    private ArrayList<ProjectFile> allFiles;
    private List<OffListElement> matchingFiles;
    private Filter filter;
    private Settings settings;
    private IndexingListener indexingListener;
    private SearchStatusListener statusListener;
    private HashMap<String, Integer> accessFrequency = new HashMap<String, Integer>();
    private boolean isIndexing = false;
    Logger logger;
    Object mutex = new Object();

    public OffListModel(Settings s) {
        settings = s;
        filter = null;
        logger = Logger.getLogger(this.getClass().getName());
        clear();
    }

    void setIndexingListener(IndexingListener indexingListener) {
        this.indexingListener = indexingListener;
    }

    void setStatusListener(SearchStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void clear() {
        synchronized(mutex) {
            allFiles = new ArrayList<ProjectFile>();
        }
        reset();
    }

    public void setIndexing(boolean indexing) {
        isIndexing = indexing;

        if (indexingListener != null)
            indexingListener.setIndexing(indexing);
    }

    public boolean isIndexing() {
        return isIndexing;
    }

    private void reset() {
        matchingFiles = new ArrayList<OffListElement>();
    }

    public void addFile(ProjectFile pf) {
        synchronized(mutex) {
            if ((!settings.getIgnoreWildcard().matches(pf.getPathInProject())) && !allFiles.contains(pf)) {
                allFiles.add(pf);
            }
        }
    }

    public void renameFile(Object id, String newName) {
        synchronized(mutex) {
            for (ProjectFile f : allFiles) {
                if (f.getId() == id) {
                    f.rename(newName);
                    break;
                }
            }
        }
    }

    public void removeFile(Object id) {
        synchronized(mutex) {
            for (ProjectFile f : allFiles) {
                if (f.getId() == id) {
                    allFiles.remove(f);
                    break;
                }
            }
        }
    }

    public void setFilter(final String f) {
        if (filter != null &&  f.equals(filter.toString()))
            return;
        if (f.length() < settings.getMinPatternLength()) {
            filter = null;
            reset();
            fireContentsChanged(this, 0, getSize());
            return;
        }
        filter = (f == null ? null : new Filter(f, settings));
    }

    public void refilter() {
        reset();
        if (filter != null) {

            logger.info("[OFF] refiltering model");
            boolean withPath = filter.toString().indexOf("/") != -1;
            synchronized(mutex) {
                for (ProjectFile file : allFiles) {
                    passFilter(file, withPath);
                }
            }

            // sort by filename
            if (settings.isNameSorting()) {
                Collections.sort(matchingFiles, new FileNameComparator());
            }

            // sort by file extension
            if (settings.isExtensionSorting()) {
                Collections.sort(matchingFiles, new FileExtensionComparator());
            }

            // sort by match distance if smart matching
            if (settings.isSmartMatch() && settings.isDistanceSorting()) {
                Collections.sort(matchingFiles, new DistanceComparator());
            }

            // sort by access frequency
            if (settings.isPopularitySorting()) {
                Collections.sort(matchingFiles, new PopularityComparator());
            }

            // put files on less-priority-mask to the bottom of list
            Collections.sort(matchingFiles, new PriorityComparator());
        }
        fireContentsChanged(this, 0, getSize());
        if (statusListener != null) statusListener.setSearchSuccess(getSize() > 0);
    }

    private void passFilter(ProjectFile file, boolean withPath) {
        String name = withPath ? file.getPathInProject().toLowerCase() : file.getName().toLowerCase();
        Matcher matcher = filter.matcher(name);
        if (matcher.matches()) {
            OffListElement e = new OffListElement(matcher, file, withPath);

            // lower priority
            if (!settings.getLessPriorityMask().equals("")) {
                if (settings.getLessPriorityWildcard().matches(file.getPathInProject())) {
                    e.setPriority(-1);
                }
            }

            // higher priority
            if (!settings.getMorePriorityMask().equals("")) {
                if (settings.getMorePriorityWildcard().matches(file.getPathInProject())) {
                    e.setPriority(1);
                }
            }
            matchingFiles.add(e);
        }
    }

    public OffListElement getElementAt(int index) {
        return matchingFiles.get(index);
    }

    public int getSize() {
        return matchingFiles.size() < MAX_RESULTS ? matchingFiles.size() : MAX_RESULTS;
    }

    public void incrementAccessCounter(ProjectFile pf) {
        String path = pf.getFullPath();
        if (accessFrequency.containsKey(path)) {
            accessFrequency.put(path, accessFrequency.get(path)+1);
        } else {
            accessFrequency.put(path, 1);
        }
    }

    private int getPopularity(String fullPath) {
        Integer p = accessFrequency.get(fullPath);
        return p == null ? 0 : p;
    }

    class FileNameComparator implements Comparator<OffListElement> {
        public int compare(OffListElement o1, OffListElement o2) {
            return o1.getFile().getName().toLowerCase().compareTo(
                    o2.getFile().getName().toLowerCase());
        }
    }

    class FileExtensionComparator implements Comparator<OffListElement> {
        public int compare(OffListElement o1, OffListElement o2) {
            return o1.getFile().getExtension().toLowerCase().compareTo(
                    o2.getFile().getExtension().toLowerCase());
        }
    }

    class PopularityComparator implements Comparator<OffListElement> {
        public int compare(OffListElement o1, OffListElement o2) {
            int a = getPopularity(o1.getFile().getFullPath());
            int b = getPopularity(o2.getFile().getFullPath());
            return (a > b ? -1 : (a == b) ? 0 : 1);
        }
    }

    class DistanceComparator implements Comparator<OffListElement> {
        public int compare(OffListElement o1, OffListElement o2) {
            int a = o1.getFilterDistance();
            int b = o2.getFilterDistance();
            return (a < b ? -1 : (a == b) ? 0 : 1);
        }
    }

    class PriorityComparator implements Comparator<OffListElement> {
        public int compare(OffListElement o1, OffListElement o2) {
            int a = o1.getPriority();
            int b = o2.getPriority();
            return (a > b ? -1 : (a == b) ? 0 : 1);
        }
    }
}