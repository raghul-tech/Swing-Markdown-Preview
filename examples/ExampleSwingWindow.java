import java.awt.Color;
import java.io.File;

import javax.swing.JFrame;

import io.github.raghultech.markdown.swing.preview.SwingMarkdownWindowPreview;

/**
 * ExampleSwingWindow demonstrates how to show a standalone Markdown preview window.
 */
public class ExampleSwingWindow {

    public static void main(String[] args) {
        // Create a File object pointing to your Markdown file
        File file = new File("EXAMPLE.md");
      
        // Create the preview instance that will render the Markdown
        /**
         * You can pass either a File or a raw Markdown String to the constructor.
         */
        SwingMarkdownWindowPreview preview = new SwingMarkdownWindowPreview(file);

        /**
         * If you want, you can set your own custom JFrame for the preview window.
         * Otherwise, the preview creates and manages its own JFrame automatically.
         */

        /*
        // Example of creating a custom JFrame (optional)
        JFrame myFrame = new JFrame("My Custom Preview");
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        myFrame.setResizable(false); // Make window fixed-size
        myFrame.setBackground(Color.DARK_GRAY); // Set custom background color

        // Attach your custom frame to the preview
        preview.setFrame(myFrame);
        */

        // Set the title shown in the preview window
        preview.setWindowTitle("My Markdown Viewer");

        // Set the preview window size (width, height)
        preview.setWindowSize(900, 700);

        // Uncomment to enable dark mode styling
        // preview.isDarkMode(true);

        // Uncomment to set a custom window icon
        /*
        URL imageUrl = ExampleSwingWindow.class.getResource("/MD.png");
        ImageIcon icon = new ImageIcon(imageUrl);
        preview.setIcon(icon,16); // Set icon and size
        */

        // Launch the preview window with all the configured options
        preview.launchPreview();
    }
}
