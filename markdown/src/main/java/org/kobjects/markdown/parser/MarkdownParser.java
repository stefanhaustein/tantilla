package org.kobjects.markdown.parser;

import org.kobjects.markdown.AnnotatedString;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.markdown.Text;

import java.io.BufferedReader;
import java.io.IOException;

public class MarkdownParser {

  public static Text parse(BufferedReader reader) throws IOException {
    return new MarkdownParser(reader).parse();
  }

  private final BufferedReader reader;
  private final Text.Builder builder = new Text.Builder();

  private MarkdownParser(BufferedReader reader) {
    this.reader = reader;
  }


  private Text parse() throws IOException {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    boolean inHeader = true;
    while(true) {
      String line = reader.readLine();
      if (line == null || line.trim().length() == 0) {
        inHeader = false;
        if (asb.length() != 0) {
          builder.addParagraph(asb.build());
          asb = new AnnotatedStringBuilder();
        }
        if (line == null) {
          break;
        }
      } else {
        if (inHeader && line.indexOf(':') > 0 && line.indexOf(':') < line.indexOf(' ') && line.indexOf(':') < line.trim().length() - 1) {
          int cut = line.indexOf(':');
          String key = line.substring(0, cut).trim();
          String value = line.substring(cut + 1).trim();
          builder.addMetadata(key, value);
        } else {
          inHeader = false;
          asb.append(line);
        }
      }
    }
    builder.addParagraph(asb.build());
    return builder.build();
  }

}
