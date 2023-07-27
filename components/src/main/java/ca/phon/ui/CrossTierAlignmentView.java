package ca.phon.ui;

import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.alignment.CrossTierAlignment;
import ca.phon.session.alignment.TierAligner;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Map;

public class CrossTierAlignmentView extends JComponent {

    private Record record;

    private JXTree tree;

    public CrossTierAlignmentView() {
        this(null);
    }

    public CrossTierAlignmentView(Record record) {
        this.record = record;
        init();

        addPropertyChangeListener("record", e -> this.update());
    }

    public Record getRecord() {
        return this.record;
    }

    public void setRecord(Record record) {
        var oldRecord = this.record;
        this.record = record;
        super.firePropertyChange("record", oldRecord, record);
    }

    private void init() {
        setLayout(new BorderLayout());

        tree = new JXTree(alignmentToTree("Orthography"));
        final JScrollPane treeScroller = new JScrollPane(tree);

        add(treeScroller, BorderLayout.CENTER);
    }

    private void update() {
        tree.setModel(new DefaultTreeModel(alignmentToTree("Orthography")));
        tree.expandAll();
    }

    private TreeNode alignmentToTree(String tierName) {
        if(getRecord() == null) return new DefaultMutableTreeNode();
        final Tier<?> recordTier = getRecord().getTier(tierName);
        if(recordTier == null) return new DefaultMutableTreeNode();
        final CrossTierAlignment alignment = TierAligner.calculateCrossTierAlignment(getRecord(), recordTier);
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(alignment);

        for(Object element:alignment.getTopAlignmentElements()) {
            final DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode(element);
            final Map<String, Object> alignments = alignment.getAlignedElements(element);
            for(String alignedTier:alignments.keySet()) {
                final String text = String.format("%s: %s", alignedTier, alignments.get(alignedTier));
                final DefaultMutableTreeNode alignedElement = new DefaultMutableTreeNode(text);
                elementNode.add(alignedElement);
            }
            root.add(elementNode);
        }

        return root;
    }

}
