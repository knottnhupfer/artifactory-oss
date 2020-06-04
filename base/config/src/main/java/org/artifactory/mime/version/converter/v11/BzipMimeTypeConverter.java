package org.artifactory.mime.version.converter.v11;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class BzipMimeTypeConverter extends MimeTypeConverterBase {
    @Override
    public void convert(Document doc) {
        MimeType bz2 = new MimeTypeBuilder("application/x-bzip2").extensions("bz2, tar.bz2").archive(true).index(false)
                .build();
        addIfNotExist(doc, bz2);
    }

}
