import java.io.File;

import javax.swing.JFrame;

import io.github.raghultech.markdown.swing.preview.SwingMarkdownPanelPreview;

/**
 * ExampleSwingPanel demonstrates how to use SwingMarkdownPanelPreview
 * to embed a Markdown preview panel inside any Swing container like JFrame.
 */
public class ExampleSwingPanel {
	
	public static void main (String args[]) {
		// Create a new JFrame window
		JFrame frame = new JFrame("Embedded Markdown Preview");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close app on exit
		frame.setSize(800, 600); // Set window size

		// Create a File object pointing to your Markdown file
		File file = new File("EXAMPLE.md");

		/**
		 * Create a SwingMarkdownPanelPreview to render the Markdown content.
		 * You can pass either a File or a raw Markdown String to the constructor.
		 * This gives you a reusable JPanel that can be added anywhere.
		 */
		SwingMarkdownPanelPreview previewPanel = new SwingMarkdownPanelPreview(file);

		// Add the preview panel to the center of the JFrame
		frame.getContentPane().add(previewPanel);

		// Display the window
		frame.setVisible(true);
	}
}
