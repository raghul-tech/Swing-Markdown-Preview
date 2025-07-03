package io.github.raghultech.markdown.swing.preview;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;

import io.github.raghultech.markdown.swing.exception.MarkdownPreviewContentException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewFileException;
import io.github.raghultech.markdown.swing.exception.MarkdownPreviewInitializationException;
import io.github.raghultech.markdown.swing.integration.SwingMarkdownRenderer;
import io.github.raghultech.markdown.utils.filesentry.FileWatcher;
import io.github.raghultech.markdown.utils.openloom.LoadFile;



@SuppressWarnings("unused")
public class SwingMarkdownTabbedPreview {

    private final JTabbedPane tabbedPane;
    private final Map<Object, PreviewTab> previewTabs = new HashMap<>();
    private Timer updateTimer;
    private ExecutorService executor;
    private File currentFile;
    private String originalContent;
    private boolean isStringMode = false;
    private volatile boolean disposed = false;
    private FileWatcher fileWatcher;
    private transient WeakReference<Thread> fileWatcherThread;
    private JButton tabBtn;
    private Icon icon;
    private int iconSize = 16;
    private String tabName =null;
    private boolean isdarkMode = false;
    private SwingMarkdownRenderer render = SwingMarkdownRenderer.getInstance();
    
    public SwingMarkdownTabbedPreview(JTabbedPane tabbedPane, File file) {
    	 if (file == null || !file.exists() || !file.isFile()) {
 	        throw new MarkdownPreviewFileException("Invalid file provided: " + file);
 	    }
 	 if(tabbedPane == null) throw new MarkdownPreviewInitializationException("TabbedPane should not be null");
 	this.currentFile = file;
 	 this.tabbedPane = tabbedPane;
    }

    public SwingMarkdownTabbedPreview(JTabbedPane tabbedPane, String content) {
    	if (content == null ||content.isEmpty() ) {
            throw new MarkdownPreviewContentException("The String Content should not be null or empty " );
        }
    	 if(tabbedPane == null) throw new MarkdownPreviewInitializationException("TabbedPane should not be null");
    	this.originalContent = content;
    	  this.isStringMode = true;
    	  this.tabbedPane = tabbedPane;
    }

    public void launchPreviewTab() {
        createExecutor();
        setupUpdateTimer();
        createAndShowPreviewTab();
    }

    
    public boolean isPreviewShowing() {
    	return !disposed;
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

    private void createAndShowPreviewTab() {
        String markdown = isStringMode
                ? originalContent
                : LoadFile.getContent(currentFile).toString();

        String htmlBody =  render.renderMarkdown(markdown);
        String htmlWrapped =  render.wrapInHtml(htmlBody, currentFile,isdarkMode);

        JEditorPane editorPane = new JEditorPane("text/html", htmlWrapped);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(editorPane);

        editorPane.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Object key = getPreviewKey();
        PreviewTab previewTab = new PreviewTab(scrollPane, editorPane);
        previewTabs.put(key, previewTab);

        SwingUtilities.invokeLater(() -> addTabWithPreview(scrollPane, key));

        if (!isStringMode) watchFileForChanges();
    }

    private Object getPreviewKey() {
        return isStringMode ? this : currentFile;
    }

    private void addTabWithPreview(JComponent component, Object key) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        int insertIndex;

        if (selectedIndex == -1) {
            // No tab selected, insert at end
            insertIndex = tabbedPane.getTabCount();
        } else {
            // Insert after the selected tab
            insertIndex = selectedIndex + 1;
        }
        String title = tabName != null ? tabName : isStringMode ?  "Markdown Preview" : "Preview: " + currentFile.getName();

        // You can pass null icon or load your ImageIcon here
        Icon tabIcon = icon != null ? icon : null;

        JPanel tabHeader = createTabHeader(title, component, key, icon);

        tabbedPane.insertTab(title, icon, component, null, insertIndex);
        tabbedPane.setTabComponentAt(insertIndex, tabHeader);
        tabbedPane.setSelectedIndex(insertIndex);
    }


  
    private JPanel createTabHeader(String title, JComponent comp, Object key, Icon icon) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);

        JLabel label = new JLabel(title);
        if (icon != null) {
            label.setIcon(icon);
            label.setIconTextGap(4);
        }

        // Close button
        JButton closeBtn = new JButton("×");
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusable(false);
        closeBtn.addActionListener(e -> {
            int index = tabbedPane.indexOfComponent(comp);
            if (index != -1) {
                previewTabs.remove(key);
                tabbedPane.remove(index);
            }
        });

        // Example: Refresh button (optional)
    //    JButton refreshBtn = new JButton("\u21BA"); // Unicode for refresh arrow
      //  refreshBtn.setBorder(BorderFactory.createEmptyBorder());
        //refreshBtn.setContentAreaFilled(false);
      //  refreshBtn.setFocusable(false);
        //refreshBtn.setToolTipText("Refresh Preview");
     //   refreshBtn.addActionListener(e -> updatePreviewContent());

        panel.add(label);
        panel.add(Box.createHorizontalStrut(5));
     //   panel.add(refreshBtn);
        panel.add(closeBtn);

        return panel;
    }



private  synchronized void watchFileForChanges() {
	  if (isStringMode) return;
  stopFileWatcher();  // Stop existing watcher before starting a new one
 
  if (currentFile == null || !currentFile.exists()) {
      return;
  }
  
  fileWatcher = new FileWatcher(currentFile);
  fileWatcher.setFileChangeListener(changed -> {
      if (changed) {
         // SwingUtilities.invokeLater(this::updatePreviewContent);
          SwingUtilities.invokeLater(this::triggerPreviewUpdate);
      	//triggerPreviewUpdate();
      }
  });//file watcher changign to boolean
  
  
  
  Thread watcherThread = new Thread(fileWatcher);
  watcherThread.setDaemon(true);
  fileWatcherThread = new WeakReference<>(watcherThread);
  watcherThread.start();


}

private synchronized void stopFileWatcher() {
  if (fileWatcher != null) {
      fileWatcher.stopWatching(); // Gracefully stop the watcher
  }

  Thread watcherThread = (fileWatcherThread != null) ? fileWatcherThread.get() : null;

  if (watcherThread != null && watcherThread.isAlive()) {
      watcherThread.interrupt();
      try {
          watcherThread.join(1000); // Wait for the thread to stop
      } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
      }
  }

  // Clear WeakReference to help with garbage collection
  if (fileWatcherThread != null) {
      fileWatcherThread.clear();
  }
}

    private void triggerPreviewUpdate() {
        if (updateTimer != null) updateTimer.restart();
    }

    private void updatePreviewContent() {
        if (disposed) return;
        try {
        String markdown = LoadFile.getContent(currentFile).toString();
        String htmlBody =  render.renderMarkdown(markdown);
        String htmlWrapped =  render.wrapInHtml(htmlBody, currentFile,isdarkMode);
        Object key = getPreviewKey();
        PreviewTab tab = previewTabs.get(key);
        if (tab != null) {
            tab.editorPane.setText(htmlWrapped);
            tab.editorPane.setCaretPosition(0);
        }
    } catch (Exception e) {
        dispose(); // Clean up if initialization fails
        throw new MarkdownPreviewInitializationException("Failed to initialize Markdown preview", e);
    }
    }

    public void dispose() {
        disposed = true;
        if (executor != null) executor.shutdownNow();
        stopFileWatcher();
    }
    public Icon getTabbedPaneIcon() { return icon; }

    public void setTabbedPaneIcon( Icon icon ,int size) {
    	this.icon = icon;
        this.iconSize = size;
        // Optionally rescale the icon if it already exists
        if (this.icon instanceof ImageIcon) {
            java.awt.Image original = ((ImageIcon) icon).getImage();
            java.awt.Image resized = original.getScaledInstance(iconSize, iconSize, java.awt.Image.SCALE_SMOOTH);
            this.icon = new ImageIcon(resized);
        }
    }
    public void setTabbedPaneIcon( Icon icon) { this.icon = icon; }

    public int getTabbedPaneIconSize() {
        return iconSize;
    }


    public void setTabbedPaneButton(JButton btn) { this.tabBtn = btn; }
    public JButton getTabbedPaneButton() { return tabBtn; } 

    public void setTabbedPaneName(String name) { this.tabName = name; }
    public String getTabbedPaneName() { return tabName; }

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

    public void isDarkMode(boolean dark) {
    	this.isdarkMode = dark;
    	if(executor !=null) updatePreviewContent();
    }
    public boolean getisDarkMode() { return isdarkMode; }

    private static class PreviewTab {
        final JComponent container;
        final JEditorPane editorPane;
        PreviewTab(JComponent container, JEditorPane editorPane) {
            this.container = container;
            this.editorPane = editorPane;
        }
    }
}
