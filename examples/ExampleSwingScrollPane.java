import java.awt.Desktop;
import java.io.File;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;

import io.github.raghultech.markdown.swing.preview.SwingMarkdownScrollPanePreview;

/**
 * ExampleSwingScrollPane demonstrates how to use SwingMarkdownScrollPanePreview
 * to create a scrollable Markdown preview inside a Swing application.
 */
public class ExampleSwingScrollPane {

    /**
     * If you don’t call setEditorPane(), the preview class handles hyperlink logic automatically.
     * If you do call setEditorPane(), you must add the hyperlink listener yourself.
     */
    public static void main(String[] args) {
        // Create a File object pointing to your Markdown file
        File markdownFile = new File("EXAMPLE.md");

        /**
         * (Optional) Create your own JEditorPane for more control.
         * You can skip this part and let SwingMarkdownScrollPanePreview handle it.
         */
        JEditorPane myPane = new JEditorPane();
        myPane.setContentType("text/html"); // Tell it to render HTML
        myPane.setEditable(false); // Read-only

        // Attach a hyperlink listener so clicking links opens the default browser
        myPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Create the Markdown preview
        SwingMarkdownScrollPanePreview preview = new SwingMarkdownScrollPanePreview(markdownFile);

        // If using a custom JEditorPane, attach it here
        preview.setEditorPane(myPane);

        // Create the JScrollPane containing the rendered Markdown
        JScrollPane previewPane = preview.createPreview();

        // Create the main JFrame to hold the preview
        JFrame frame = new JFrame("Markdown Preview Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the app when window closes
        frame.setSize(800, 600); // Set size
        frame.add(previewPane); // Add the scroll pane to the frame
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true); // Show the window
    }
}
