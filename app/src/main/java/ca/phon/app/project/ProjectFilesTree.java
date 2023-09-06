package ca.phon.app.project;

import ca.phon.app.log.LogUtil;
import ca.phon.project.Project;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProjectFilesTree extends JTree {

    private boolean showHiddenFiles = false;

    private boolean showProjectFiles = false;

    private final Project project;

    /**
     * Create tree for project
     *
     * @param project
     * @return tree for project
     */
    public static TreeNode treeForProject(Project project, boolean includeProjectFiles, boolean includeHidden) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(project);
        final Path projectFolderPath = Path.of(project.getLocation());
        scanFolder(projectFolderPath, projectFolderPath, includeProjectFiles, includeHidden, root);
        return root;
    }

    private static void scanFolder(Path rootPath, Path folderPath, boolean includeProjectFiles, boolean includeHidden, TreeNode parent) {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            List<Path> pathList = new ArrayList<>();
            for(Path p:stream) {
                pathList.add(p);
            }
            Comparator<Path> pathComparator = (p1, p2) -> {
                if(Files.isDirectory(p1) && Files.isRegularFile(p2)) {
                    return 1;
                } else if(Files.isRegularFile(p1) && Files.isDirectory(p2)) {
                    return -1;
                } else {
                    return p1.toString().compareTo(p2.toString());
                }
            };
            Collections.sort(pathList, pathComparator);
            for(Path p:pathList) {
                final Path relativePath = rootPath.relativize(p);
                if(Files.isDirectory(p) && folderFilter(p, includeHidden)) {
                    final DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(relativePath);
                    ((DefaultMutableTreeNode)parent).add(folderNode);
                    scanFolder(rootPath, p, includeProjectFiles, includeHidden, folderNode);
                } else if(fileFilter(p, includeProjectFiles, includeHidden)) {
                    final DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(relativePath);
                    ((DefaultMutableTreeNode)parent).add(fileNode);
                }
            }
        } catch (IOException e) {
            LogUtil.severe(e);
        }
    }

    private static boolean folderFilter(Path projectFolder, boolean includeHidden) throws IOException {
        if(Files.isHidden(projectFolder) && !includeHidden) return false;
        final String folderName = projectFolder.getFileName().toString();
        if(!folderName.startsWith("~")
                && !folderName.endsWith("~")
                && !folderName.startsWith(".")
                && !folderName.startsWith("__"))
            return true;
        else
            return false;
    }

    private static boolean fileFilter(Path projectFile, boolean includeProjectFiles, boolean includeHidden) throws IOException {
        if(Files.isHidden(projectFile) && !includeHidden) return false;
        final String fileName = projectFile.getFileName().toString();
        if(!fileName.startsWith("~")
                && !fileName.endsWith("~")
                && !fileName.startsWith(".")
                && !fileName.startsWith("__")) {
            final SessionInputFactory inputFactory = new SessionInputFactory();
            if(inputFactory.createReaderForFile(projectFile.toFile()) == null) {
                return includeProjectFiles;
            }
            return true;
        } else
            return false;
    }

    public ProjectFilesTree(Project project) {
        super(treeForProject(project,  false,false));
        this.project = project;
        setCellRenderer(new ProjectFilesCellRenderer());
    }

    private final class ProjectFilesCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel retVal = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if(value instanceof DefaultMutableTreeNode node) {
                if(node.getUserObject() instanceof Path path) {
                    final Path fullPath = Path.of(project.getLocation(), path.toString());
                    retVal.setText(path.getFileName().toString());
                    retVal.setIcon(IconManager.getInstance().getSystemIconForPath(fullPath.toString(), IconSize.SMALL));
                }
            }

            return retVal;
        }

    }

}
