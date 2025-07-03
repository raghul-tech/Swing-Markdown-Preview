package io.github.raghultech.markdown.swing.preview;

import java.awt.Desktop;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

import io.github.raghultech.markdown.swing.exception.MarkdownPreviewContentException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewFileException;
import io.github.raghultech.markdown.swing.integration.SwingMarkdownRenderer;
import io.github.raghultech.markdown.utils.filesentry.FileWatcher;
import io.github.raghultech.markdown.utils.openloom.LoadFile;


/**
 * A reusable JPanel that previews Markdown content.
 */
@SuppressWarnings("serial")
public class SwingMarkdownPanelPreview extends JPanel {

    private final JEditorPane editorPane;
    private final JScrollPane scrollPane;

    private File currentFile;
    private String originalContent;
    private boolean isStringMode = false;
    private volatile boolean disposed = false;

    private FileWatcher fileWatcher;
    private transient WeakReference<Thread> fileWatcherThread;
    private ExecutorService executor;
    private Timer updateTimer;
    
    private boolean isdarkMode = false;

    private SwingMarkdownRenderer render = SwingMarkdownRenderer.getInstance();
    
    public SwingMarkdownPanelPreview(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new MarkdownPreviewFileException("Invalid file provided: " + file);
        }
        this.currentFile = file;
        this.editorPane = createEditorPane();
        this.scrollPane = new JScrollPane(editorPane);
        setLayout(new java.awt.BorderLayout());
        add(scrollPane, java.awt.BorderLayout.CENTER);

        createExecutor();
        setupUpdateTimer();
        updatePreviewContent();
        watchFileForChanges();
    }

    public SwingMarkdownPanelPreview(String content) {
        if (content == null || content.isEmpty()) {
            throw new MarkdownPreviewContentException("Content cannot be null or empty");
        }
        this.originalContent = content;
        this.isStringMode = true;
        this.editorPane = createEditorPane();
        this.scrollPane = new JScrollPane(editorPane);
        setLayout(new java.awt.BorderLayout());
        add(scrollPane, java.awt.BorderLayout.CENTER);

        createExecutor();
        updatePreviewContent();
    }

    private JEditorPane createEditorPane() {
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
                Thread t = new Thread(r, "MarkdownPreviewPanel");
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

            String html = render.renderMarkdown(markdown);
            String wrappedHtml = render.wrapInHtml(html, currentFile,isdarkMode);

            SwingUtilities.invokeLater(() -> {
                if (disposed) return;
                editorPane.setText(wrappedHtml);
                editorPane.setCaretPosition(0);
            });
        });
    }

    private synchronized void watchFileForChanges() {
        if (isStringMode) return;

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
    }

    public void setContent(String content) {
        this.isStringMode = true;
        this.originalContent = content;
        this.currentFile = null;
        stopFileWatcher();
        if(executor !=null) updatePreviewContent();
    }

    public void setCurrentFile(File file) {
        this.isStringMode = false;
        this.originalContent = null;
        this.currentFile = file;
        watchFileForChanges();
        if(executor !=null) updatePreviewContent();
    }
    public File getCurrentFile () { return currentFile; }
    public String getContent() { return originalContent; }
    public JEditorPane getEditorPane() {
        return editorPane;
    }
    public void isDarkMode(boolean dark) {
    	this.isdarkMode = dark;
    	if(executor !=null) updatePreviewContent();
    }
    public boolean getisDarkMode() { return isdarkMode; }
}
