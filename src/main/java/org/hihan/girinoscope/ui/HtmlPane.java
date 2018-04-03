package org.hihan.girinoscope.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("serial")
public class HtmlPane extends JEditorPane {

    private static final Logger logger = Logger.getLogger(HtmlPane.class.getName());

    public static String toHexCode(Color color) {
        return String.format("#%02X%02X%02X%02X", //
                color.getRed(), //
                color.getGreen(), //
                color.getBlue(), //
                color.getTransparency());
    }

    @NotNull
    private static String loadContent(URL url) throws IOException {
        try (InputStream input = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder file = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                file.append(line);
            }
            return file.toString();
        }
    }

    public HtmlPane(URL url) throws IOException {
        this(loadContent(url));
    }

    public HtmlPane(String text) {
        HTMLEditorKit kit = new HTMLEditorKit() {
            @Override
            public Document createDefaultDocument() {
                HTMLDocument document = (HTMLDocument) super.createDefaultDocument();
                document.setBase(Icon.class.getResource("/org/hihan/girinoscope/ui/"));
                return document;
            }
        };
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {bgcolor: white; font-family: Sans-Serif;}");

        setEditorKit(kit);
        setContentType("text/html");
        setText(text);
        setEditable(false);

        addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(final HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    final String href = getHref(event);
                    try {
                        Desktop.getDesktop().browse(new URI(href));
                    } catch (URISyntaxException | IOException e) {
                        logger.log(Level.WARNING, "Can’t open link " + href, e);
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        JEditorPane edPane = new JEditorPane();
        edPane.setContentType("text/html");

        HTMLEditorKit hek = new HTMLEditorKit();

        edPane.setEditorKit(hek);

        HTMLDocument doc = (HTMLDocument) edPane.getDocument();

        doc.insertString(0, "Test testing", null);

        Element[] roots = doc.getRootElements();
        Element body = null;
        for (int i = 0; i < roots[0].getElementCount(); i++) {
            Element element = roots[0].getElement(i);
            if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY) {
                body = element;
                break;
            }
        }

        //URL url = ClassLoader.getSystemResource("/org/hihan/girinoscope/ui/icon-64.png");
        URL url = Icon.class.getResource("/org/hihan/girinoscope/ui/icon-64.png");
        System.out.println(url.toString());
        doc.insertAfterEnd(body, "<img src='file:/home/antares/Personnel/Productions/Code/Maison/Girinoscope/target/classes/org/hihan/girinoscope/ui/icon-64.png'>");
        frame.add(edPane);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static String getHref(HyperlinkEvent event) {
        AttributeSet attributes = event.getSourceElement().getAttributes();
        return Objects.requireNonNull(getAttribute((AttributeSet) getAttribute(attributes, "a"), "href")).toString();
    }

    @Nullable
    private static Object getAttribute(AttributeSet attributes, String name) {
        for (Enumeration<?> enumeration = attributes.getAttributeNames(); enumeration.hasMoreElements();) {
            Object nameKey = enumeration.nextElement();
            if (name.equals(nameKey.toString())) {
                return attributes.getAttribute(nameKey);
            }
        }
        return null;
    }
}
