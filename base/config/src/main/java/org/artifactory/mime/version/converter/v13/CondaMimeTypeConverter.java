package org.artifactory.mime.version.converter.v13;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;

/**
 * Adding Conda To mime type.
 *
 * @author Omri Ziv
 */
public class CondaMimeTypeConverter extends MimeTypeConverterBase {
    @Override
    public void convert(Document doc) {
        MimeType mimeType = new MimeTypeBuilder("application/x-conda")
                .extensions("conda")
                .index(true)
                .archive(true)
                .css("conda").build();
        addIfNotExist(doc, mimeType);
    }
}
