import java.awt.BorderLayout;
import java.io.File;

import javax.swing.*;

import io.github.raghultech.markdown.swing.preview.SwingMarkdownTabbedPreview;

/**
 * ExampleSwingTab demonstrates how to use SwingMarkdownTabbedPreview
 * to add a Markdown preview as a new tab in an existing JTabbedPane.
 */
public class ExampleSwingTab {

    public static void main(String[] args) {
        // Create the main application window
        JFrame frame = new JFrame("Markdown Preview Tabs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close app when window closes
        frame.setSize(1200, 900); // Set window size
        frame.setLocationRelativeTo(null); // Center the window on screen
        frame.setLayout(new BorderLayout()); // Use BorderLayout

        // Create a tabbed pane for multiple tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // Allow scrolling if tabs overflow

        // Add some initial tabs with empty text editors
        tabbedPane.addTab("Editor 1", new JScrollPane(new JTextArea()));
        tabbedPane.addTab("Editor 2", new JScrollPane(new JTextArea()));

        // Add the tabbed pane to the frame's center area
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Create an instance of the Markdown preview
        File markdownFile = new File("EXAMPLE.md");
        SwingMarkdownTabbedPreview preview = new SwingMarkdownTabbedPreview(tabbedPane, markdownFile);

        // Show the main window
        frame.setVisible(true);

        /**
         * If you want, you can set a custom icon for the preview tab:
         * 
         * Icon icon = new ImageIcon(ExampleSwingTab.class.getResource("/MD.png"));
         * preview.setTabbedPaneIcon(icon, 16);
         */

        // Add the Markdown preview as a new tab in the tabbed pane
        preview.launchPreviewTab();
    }

}
