package io.github.raghultech.markdown.swing.preview;

import java.awt.Desktop;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;


import io.github.raghultech.markdown.swing.exception.MarkdownPreviewContentException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewFileException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewInitializationException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewResourceException;
import io.github.raghultech.markdown.swing.integration.SwingMarkdownRenderer;
import io.github.raghultech.markdown.utils.filesentry.FileWatcher;
import io.github.raghultech.markdown.utils.openloom.LoadFile;



public class SwingMarkdownWindowPreview {

    private JFrame frame;
    private JEditorPane editorPane;
    private FileWatcher fileWatcher;
    private transient WeakReference<Thread> fileWatcherThread;
    private ExecutorService executor;
    private Timer updateTimer;

    private File currentFile;
    private String originalContent;
    private boolean isStringMode = false;
    private volatile boolean disposed = false;

    private String windowTitle = "Markdown Preview";
    private int windowWidth = 800;
    private int windowHeight = 600;
    private ImageIcon icon;
    private int iconSize = 16;
    private boolean isdarkMode = false;
    private SwingMarkdownRenderer render = SwingMarkdownRenderer.getInstance();
    

    public SwingMarkdownWindowPreview(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new MarkdownPreviewFileException("Invalid file provided: " + file);
        }
        this.currentFile = file;
    }

    public SwingMarkdownWindowPreview(String content) {
        if (content == null || content.isEmpty()) {
            throw new MarkdownPreviewContentException("Content cannot be null or empty");
        }
        this.originalContent = content;
        this.isStringMode = true;
    }

  

    public void launchPreview() {
    	try {
        createExecutor();
        setupUpdateTimer();

        SwingUtilities.invokeLater(() -> {
            if (disposed) return;

            // Create frame if not supplied
            if (frame == null) {
                frame = new JFrame(windowTitle);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(windowWidth, windowHeight);
                frame.setLocationRelativeTo(null);
            } else {
                frame.setTitle(windowTitle);
                frame.setSize(windowWidth, windowHeight);
            }
            
          
            if(icon ==null) { 
            	URL imageUrl = getClass().getResource("/MD.png");
            	if (imageUrl == null) {
            	    throw new MarkdownPreviewResourceException("Icon resource '/MD.png' not found.");
            	}
            	icon = new ImageIcon(imageUrl);
            }
  
            frame.setIconImage(icon.getImage());

            // Create editorPane if not supplied
            if (editorPane == null) {
                editorPane = new JEditorPane();
                editorPane.setContentType("text/html");
                editorPane.setEditable(false);
            }

            // Always attach hyperlink handler
            editorPane.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(editorPane);

            // Replace all content to ensure correct refresh
            frame.getContentPane().removeAll();
            frame.getContentPane().add(scrollPane);

            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dispose();
                }
            });
            editorPane.setText("<html><body style='font-family:sans-serif; padding:10px; color:gray;'>Loading preview...</body></html>");

            updatePreviewContent();

            if (!isStringMode) {
                watchFileForChanges();
            }

            frame.setVisible(true);
        });
    	 } catch (Exception e) {
	            dispose(); // Clean up if initialization fails
	            throw new MarkdownPreviewInitializationException("Failed to initialize Markdown preview window", e);
	        }
    }

    private void createExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "MarkdownSwingPreview");
                t.setDaemon(true);
                return t;
            });
        }
    }

	private void setupUpdateTimer() {
        if (isStringMode) return;
        updateTimer = new Timer(500, e -> updatePreviewContent());
        updateTimer.setRepeats(false);
    }

    private void updatePreviewContent() {
        if (disposed) return;

        executor.submit(() -> {
            String markdown;
            if (isStringMode) {
                markdown = originalContent;
            } else {
                StringBuilder content = LoadFile.getContent(currentFile);
                if (content == null) {
                    throw new MarkdownPreviewContentException("Could not read file: " + currentFile);
                }
                markdown = content.toString();
            }

            String html =  render.renderMarkdown(markdown);
            String wrappedHtml =  render.wrapInHtml(html, currentFile,isdarkMode);

            SwingUtilities.invokeLater(() -> {
                if (disposed) return;
                editorPane.setText(wrappedHtml);
                editorPane.setCaretPosition(0);
            });
        });
    }

    private synchronized void watchFileForChanges() {
        stopFileWatcher();

        fileWatcher = new FileWatcher(currentFile);
        fileWatcher.setFileChangeListener(changed -> {
            if (changed && !disposed) {
                SwingUtilities.invokeLater(this::triggerPreviewUpdate);
            }
        });

        Thread watcherThread = new Thread(fileWatcher);
        watcherThread.setDaemon(true);
        fileWatcherThread = new WeakReference<>(watcherThread);
        watcherThread.start();
    }

    private void triggerPreviewUpdate() {
        if (updateTimer != null) {
            updateTimer.restart();
        }
    }

    private synchronized void stopFileWatcher() {
        if (fileWatcher != null) {
            fileWatcher.stopWatching();
        }

        Thread watcherThread = (fileWatcherThread != null) ? fileWatcherThread.get() : null;
        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();
            try {
                watcherThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (fileWatcherThread != null) {
            fileWatcherThread.clear();
        }
    }

    public synchronized void dispose() {
        if (disposed) return;
        disposed = true;

        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
        stopFileWatcher();

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }
    
    /** Provide a custom JFrame before launch */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /** Provide a custom JEditorPane before launch */
    public void setEditorPane(JEditorPane pane) {
        this.editorPane = pane;
    }

    public JFrame getFrame() {
        return frame;
    }

    public JEditorPane getEditorPane() {
        return editorPane;
    }

    public void setWindowTitle(String title) {
        this.windowTitle = title;
        if (frame != null) {
            frame.setTitle(title);
        }
    }

    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        if (frame != null) {
            frame.setSize(width, height);
        }
    }
    public void setCurrentFile(File file) {
    	this.isStringMode = false;
    	this.originalContent = null;
    	this.currentFile = file;
    	this.disposed = false;
    	if(executor !=null) updatePreviewContent();
    }
    public File getCurrentFile () { return currentFile; }

    public void setContent(String content) { 
    	this.currentFile = null;
    	this.isStringMode = true;
    	this.originalContent = content;
    	this.disposed = false;
    	if(executor !=null) updatePreviewContent();
    	}  
    public String getContent() { return originalContent; }

    public ImageIcon getIcon() { return icon; }

    public void setIcon( ImageIcon icon ,int size) {
    	this.icon = icon;
        this.iconSize = size;
        // Optionally rescale the icon if it already exists
        if (this.icon instanceof ImageIcon) {
            java.awt.Image original = ((ImageIcon) icon).getImage();
            java.awt.Image resized = original.getScaledInstance(iconSize, iconSize, java.awt.Image.SCALE_SMOOTH);
            this.icon = new ImageIcon(resized);
        }
    }
    public void setIcon( ImageIcon icon) { this.icon = icon; }

    public int getIconSize() {
        return iconSize;
    }
    
    public void isDarkMode(boolean dark) {
    	this.isdarkMode = dark;
    	if(executor !=null) updatePreviewContent();
    }
    public boolean getisDarkMode() { return isdarkMode; }

}
