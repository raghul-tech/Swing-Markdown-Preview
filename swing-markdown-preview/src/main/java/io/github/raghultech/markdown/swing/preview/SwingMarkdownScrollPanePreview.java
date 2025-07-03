package io.github.raghultech.markdown.swing.preview;

import java.awt.Desktop;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;

import io.github.raghultech.markdown.swing.exception.MarkdownPreviewContentException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewFileException;
import io.github.raghultech.markdown.swing.integration.SwingMarkdownRenderer;
import io.github.raghultech.markdown.utils.filesentry.FileWatcher;
import io.github.raghultech.markdown.utils.openloom.LoadFile;



/**
 * Simple Markdown previewer for Swing.
 * 
 * Features:
 * - Supports previewing from file or String
 * - Auto updates when file changes
 * - Allows user-supplied JEditorPane or default creation
 */
public class SwingMarkdownScrollPanePreview {

    private  File file;
    private  String content;
    private boolean isStringMode;
    private JEditorPane customEditorPane;

    private Timer updateTimer;
    private ExecutorService executor;
    private volatile boolean disposed = false;
    private FileWatcher fileWatcher;
    private transient WeakReference<Thread> fileWatcherThread;
    
    private boolean isdarkMode = false;

    private SwingMarkdownRenderer render = SwingMarkdownRenderer.getInstance();
    
    /**
     * Construct preview from a markdown File.
     */
    public SwingMarkdownScrollPanePreview(File file) {
        if (file == null || !file.isFile()) {
            throw new MarkdownPreviewFileException("Invalid file: " + file);
        }
        this.file = file;
        this.content = null;
        this.isStringMode = false;
    }

    /**
     * Construct preview from markdown String.
     */
    public SwingMarkdownScrollPanePreview(String content) {
        if (content == null || content.isEmpty()) {
            throw new MarkdownPreviewContentException("Content cannot be null or empty.");
        }
        this.file = null;
        this.content = content;
        this.isStringMode = true;
    }

    /**
     * Launch and return the preview component.
     */
    public JScrollPane createPreview() {
        createExecutor();
        setupUpdateTimer();

        String markdown = isStringMode ? content : LoadFile.getContent(file).toString();
        String htmlBody = render.renderMarkdown(markdown);
        String htmlWrapped = render.wrapInHtml(htmlBody, file,isdarkMode);

        JEditorPane editorPane = createOrUseEditorPane();
        editorPane.setText(htmlWrapped);
        editorPane.setCaretPosition(0);

        if (!isStringMode) watchFileForChanges();

        JScrollPane scrollPane = new JScrollPane(editorPane);
        return scrollPane;
    }
    
    

    private JEditorPane createOrUseEditorPane() {
        if (customEditorPane != null) {
            if (!"text/html".equalsIgnoreCase(customEditorPane.getContentType())) {
                System.err.println("Warning: Your editor pane is not text/html. Preview may not render correctly.");
            }
        /*    customEditorPane.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });*/
            return customEditorPane;
        }

        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return pane;
    }

    private void createExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "SwingMarkdownPreview");
                t.setDaemon(true);
                return t;
            });
        }
    }

	private void setupUpdateTimer() {
        if (isStringMode) return;
        updateTimer = new Timer(700, e -> updatePreviewContent());
        updateTimer.setRepeats(false);
    }

    private void watchFileForChanges() {
        stopFileWatcher();

        fileWatcher = new FileWatcher(file);
        fileWatcher.setFileChangeListener(changed -> {
            if (changed) {
                SwingUtilities.invokeLater(this::triggerPreviewUpdate);
            }
        });

        Thread watcherThread = new Thread(fileWatcher);
        watcherThread.setDaemon(true);
        fileWatcherThread = new WeakReference<>(watcherThread);
        watcherThread.start();
    }

    private void triggerPreviewUpdate() {
        if (updateTimer != null) updateTimer.restart();
    }

    private void updatePreviewContent() {
        if (disposed) return;
        String markdown = isStringMode ? content : LoadFile.getContent(file).toString();
        String htmlBody = render.renderMarkdown(markdown);
        String htmlWrapped = render.wrapInHtml(htmlBody, file,isdarkMode);

        JEditorPane editorPane = customEditorPane != null ? customEditorPane : null;
        if (editorPane == null) {
            // No reference to default pane
            return;
        }
        editorPane.setText(htmlWrapped);
        editorPane.setCaretPosition(0);
    }

    private void stopFileWatcher() {
        if (fileWatcher != null) {
            fileWatcher.stopWatching();
        }
        Thread watcherThread = fileWatcherThread != null ? fileWatcherThread.get() : null;
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

    /**
     * Stop background tasks.
     */
    public void dispose() {
        disposed = true;
        if (executor != null) executor.shutdownNow();
        stopFileWatcher();
    }
    
    public void setCurrentFile(File file) {
    	this.isStringMode = false;
    	this.content = null;
    	this.file = file;
    	this.disposed = false;
    }
    public File getCurrentFile () { return file; }

    public void setContent(String content) { 
    	this.file = null;
    	this.isStringMode = true;
    	this.content = content;
    	this.disposed = false;
    	}  
    public String getContent() { return content; }
    
    public void isDarkMode(boolean dark) {
    	this.isdarkMode = dark;
    	if(executor !=null) updatePreviewContent();
    }
    public boolean getisDarkMode() { return isdarkMode; }


	  /**
     * Optionally inject your own JEditorPane.
     */
    public void setEditorPane(JEditorPane editorPane) {
        this.customEditorPane = editorPane;
    }

	public JEditorPane getEditorPreview() {
		  JScrollPane scrollPane = createPreview();
		  if (scrollPane != null && scrollPane.getViewport().getView() instanceof JEditorPane) {
	            return (JEditorPane) scrollPane.getViewport().getView();
	        }
		  throw new IllegalStateException("Failed to extract JEditorPane from preview.");
	}

}

