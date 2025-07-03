package io.github.raghultech.markdown.swing.exception;

@SuppressWarnings("serial")
public class MarkdownPreviewFileException extends RuntimeException {
    public MarkdownPreviewFileException(String message) {
        super(message);
    }

    public MarkdownPreviewFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
