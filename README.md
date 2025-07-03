<h1 align="center">📝 Swing Markdown Preview</h1>

<p align="center">
  <em>Render beautiful GitHub-style Markdown in your Java Swing apps.</em>
</p>

<p align="center">
  <strong>⚡ Lightweight. 💼 Production Ready. 🧩 Fully Customizable.</strong>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.raghul-tech/swing-markdown-preview">
    <img src="https://img.shields.io/maven-central/v/io.github.raghul-tech/swing-markdown-preview?style=for-the-badge&color=blueviolet" alt="Maven Central" />
  </a>
  <a href="https://github.com/raghul-tech/Swing-Markdown-Preview/actions/workflows/maven.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/raghul-tech/Swing-Markdown-Preview/maven.yml?label=Build&style=for-the-badge&color=brightgreen" alt="Build Status" />
  </a>
  <a href="https://github.com/raghul-tech/Swing-Markdown-Preview/actions/workflows/codeql.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/raghul-tech/Swing-Markdown-Preview/codeql.yml?label=CodeQL&style=for-the-badge&color=informational" alt="CodeQL Security" />
  </a>
  <a href="https://javadoc.io/doc/io.github.raghul-tech/swing-markdown-preview">
    <img src="https://img.shields.io/badge/Javadoc-1.0.0-blue?style=for-the-badge&logo=java" alt="Javadoc" />
  </a>
  <a href="https://github.com/raghul-tech/Swing-Markdown-Preview/releases">
    <img src="https://img.shields.io/github/release/raghul-tech/Swing-Markdown-Preview?label=Release&style=for-the-badge&color=success" alt="Latest Release" />
  </a>
  <a href="https://buymeacoffee.com/raghultech">
    <img src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-Support-orange?style=for-the-badge&logo=buy-me-a-coffee" alt="Buy Me A Coffee" />
  </a>
</p>

---

## ✨ What is Swing Markdown Preview?

**Swing Markdown Preview** is a modern Java library to display Markdown content in Swing applications.  
With GitHub-like styling, theme switching, and live updating, you can turn your text into beautifully rendered HTML **without complex setup**.

---

## 🎯 Who Is It For?
- 🧱 Java Swing Developers needing Markdown rendering

- 📖 Note-taking or documentation tool makers

- 🧑‍🏫 Students or teachers building markdown-based learning tools

- ⚙️ IDE or editor plugin developers needing a quick Markdown viewer

---

## ⚖️ Minimal vs All-In-One JAR

| Variant                | Includes Flexmark?   | Use Case                          |
|------------------------|----------------------|-----------------------------------|
| **swing-markdown-preview**            | ❌ No                | Maven users managing dependencies manually |
| **swing-markdown-preview-all** | ✅ Yes (flexmark-all) | Anyone who wants it to "just work" out of the box |

---

## 🚀 Installation

### ✨ All-In-One (Recommended for most users)

#### Maven

```xml
<dependency>
  <groupId>io.github.raghul-tech</groupId>
  <artifactId>swing-markdown-preview-all</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 🪶 Minimal (No Flexmark bundled)

#### Maven

```xml
<dependency>
  <groupId>io.github.raghul-tech</groupId>
  <artifactId>swing-markdown-preview</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.vladsch.flexmark</groupId>
  <artifactId>flexmark-all</artifactId>
  <version>0.64.8</version>
</dependency>
```

---

## 💡 Features
✅ GitHub-style Markdown rendering
✅ Live updates
✅ Theme switching
✅ Optional Flexmark bundling
✅ Fat jar available
✅ Java 8+ compatible

---

## 🧩 Usage Examples by Class
- Below are examples of how to use each main class:

### 🎯 1. `SwingMarkdownTabbedPreview`
- Use this when you want to add a Markdown preview tab to your own JTabbedPane.

- ✅ Perfect for apps with multiple tabs (like editors).

#### ✨ Example:
```java
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.*;
import io.github.raghultech.markdown.swing.preview.SwingMarkdownTabbedPreview;

public class ExampleSwingTab {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Markdown Preview Tabs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Create your tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Editor 1", new JScrollPane(new JTextArea()));
        tabbedPane.addTab("Editor 2", new JScrollPane(new JTextArea()));

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Create preview and attach it
        File markdownFile = new File("README.md");
        SwingMarkdownTabbedPreview preview = new SwingMarkdownTabbedPreview(tabbedPane, markdownFile);
        preview.launchPreviewTab();

        frame.setVisible(true);
    }
}
```

### 🎯 2. `SwingMarkdownScrollPanePreview`
- Use this when you want a JScrollPane with preview content you can embed in any layout.

- ✅ Great if you want scrolling built in.

### ✨ Example:
```java
import java.awt.Desktop;
import java.io.File;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import io.github.raghultech.markdown.swing.preview.SwingMarkdownScrollPanePreview;

public class ExampleSwingScrollPane {
    public static void main(String[] args) {
        File markdownFile = new File("README.md");

        // Optionally use your own JEditorPane for custom behavior
        /*
         * if u want u can add a JEditorPane or leave it will take care
         */ 
        JEditorPane myPane = new JEditorPane();
        myPane.setContentType("text/html");
        myPane.setEditable(false);
        myPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        SwingMarkdownScrollPanePreview preview = new SwingMarkdownScrollPanePreview(markdownFile);
        preview.setEditorPane(myPane);

        JScrollPane previewPane = preview.createPreview();

        JFrame frame = new JFrame("Markdown ScrollPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(previewPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
```

### 🎯 3. `SwingMarkdownPanelPreview`
- Use this if you need a JPanel you can put anywhere.

- ✅ Ideal for embedding in custom UIs.

### ✨ Example:

```java
import java.io.File;
import javax.swing.*;
import io.github.raghultech.markdown.swing.preview.SwingMarkdownPanelPreview;

public class ExampleSwingPanel {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Markdown Panel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        File file = new File("README.md");
        SwingMarkdownPanelPreview previewPanel = new SwingMarkdownPanelPreview(file);

        frame.getContentPane().add(previewPanel);
        frame.setVisible(true);
    }
}
```

### 🎯 4. `SwingMarkdownWindowPreview`
- Use this when you want a standalone window that displays your Markdown.

- ✅ Great for “Preview” buttons.

### ✨ Example:

```java
import java.io.File;
import javax.swing.JFrame;
import io.github.raghultech.markdown.swing.preview.SwingMarkdownWindowPreview;

public class ExampleSwingWindow {
    public static void main(String[] args) {
        File file = new File("README.md");

        SwingMarkdownWindowPreview preview = new SwingMarkdownWindowPreview(file);

        preview.setWindowTitle("My Markdown Viewer");
        preview.setWindowSize(900, 700);
        preview.launchPreview();
    }
}
```

---

## 🏗️ Example Projects
- You’ll find ready-to-run examples in the examples/ directory:

	- [`ExampleSwingPanel.java`](examples/ExampleSwingPanel.java) – Embed preview as a JPanel

	- [`ExampleSwingScrollPane.java`](examples/ExampleSwingScrollPane.java) – Use preview inside a JScrollPane

	- [`ExampleSwingTab.java`](examples/ExampleSwingTab.java) – Add preview as a new tab in JTabbedPane

	- [`ExampleSwingWindow.java`](examples/ExampleSwingWindow.java) – Show preview in a standalone window

✅ To run an example:

1. Download or clone this repository.

2. Navigate to examples/.

3. Compile and run the desired file.

---

## 🎨 MarkdownTheme
- Simple enum to toggle light or dark theme.

### ✅ Available values:

- 🔹 Example:

```java
preview.isDarkMode(true);
```

You can call isDarkMode() any time—your preview updates immediately.

---

## 📂 How to Use the JAR

### Compile:

```bash
javac -cp swing-markdown-preview-all-1.0.0.jar MyPreviewApp.java
```

### Run:
> Windows:
```bash
java -cp .;swing-markdown-preview-all-1.0.0.jar MyPreviewApp
```
> macOS/Linux:
```bash
java -cp .:swing-markdown-preview-all-1.0.0.jar MyPreviewApp
```
---

## 🔍 Documentation

- 📚 [Javadoc](https://javadoc.io/doc/io.github.raghul-tech/swing-markdown-preview)

- 📝 [Changelog](CHANGELOG.md)

- ❓ [Issue Tracker](https://github.com/raghul-tech/Swing-Markdown-Preview/issues)

---

## 🆕 Changelog

- see [CHANGELOG.md](CHANGELOG.md) for release history.

---

## 🤝 Contributing
- We welcome all contributions!

	- 🐛 Bug fixes

	- ✨ Features

	- 📝 Documentation improvements

	- 🧪 Example enhancements

👉 [Contributing Guide](CONTRIBUTING.md)

---

## 🐞 Report a Bug
- Found an issue? [Open an Issue](https://github.com/raghul-tech/Swing-Markdown-Preview/issues) with clear details.

---

## 📄 License
- This project is licensed under the [MIT License](LICENSE).

---

## ☕ Support
- If you love this project, you can [Buy Me a Coffee](https://buymeacoffee.com/raghultech) ❤️

