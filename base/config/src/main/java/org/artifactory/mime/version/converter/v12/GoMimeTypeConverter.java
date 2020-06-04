package org.artifactory.mime.version.converter.v12;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;

/**
 * Adding Go mod and info file as viewable text and json (respectively) to our mime-types
 *
 * @author Nadavy
 */
public class GoMimeTypeConverter extends MimeTypeConverterBase {
    @Override
    public void convert(Document doc) {
        MimeType mod = new MimeTypeBuilder("text/plain+mod").extensions("mod").index(false).viewable(true)
                .syntax("plain").build();
        MimeType info = new MimeTypeBuilder("application/json+info").extensions("info").index(false).viewable(true)
                .syntax("json").build();
        addIfNotExist(doc, mod);
        addIfNotExist(doc, info);
    }

}
