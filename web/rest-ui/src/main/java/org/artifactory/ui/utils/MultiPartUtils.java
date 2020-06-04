/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.utils;


import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.ui.exception.BadRequestException;
import org.artifactory.util.ZipUtils;
import org.jfrog.common.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Chen Keinan
 */
public class MultiPartUtils {
    private static final Logger log = LoggerFactory.getLogger(MultiPartUtils.class);

    public static final int DEFAULT_BUFF_SIZE = 8192;

    /**
     * fetch file data from request and save it to temp folder
     *
     * @param uploadDir - temp folder
     * @param fileNames
     */
    public static void saveFileDataToTemp(CentralConfigService centralConfigService,
            FormDataMultiPart formDataMultiPart, String uploadDir, List<String> fileNames, boolean isUnique) {
        int fileUploadMaxSizeMb = centralConfigService.getMutableDescriptor().getFileUploadMaxSizeMb();
        // get uploaded file map
        Map<String, List<FormDataBodyPart>> fields = formDataMultiPart.getFields();
        long sizeInBytes = getContentLengthFromMultiPart(formDataMultiPart);
        fields.forEach((name, dataBody) -> {
            List<FormDataBodyPart> formDataBodyParts = fields.get(name);
            formDataBodyParts.forEach(formDataBodyPart -> {
                // get file name and data
                InputStream inputStream = formDataBodyPart.getEntityAs(InputStream.class);
                long sizeInMb = FileUtils.bytesToMB(sizeInBytes);
                if (sizeInMb > fileUploadMaxSizeMb && fileUploadMaxSizeMb > 0) {
                    throw new BadRequestException("Uploaded file size is bigger than " + fileUploadMaxSizeMb + "MB");
                }
                String fileName = formDataBodyPart.getContentDisposition().getFileName();
                try {
                    fileName = URLDecoder.decode(fileName, "UTF-8");
                    // Prevent attempts to place this file in another folder (see RTFACT-13370)
                    fileName = new File(fileName).getName();
                    if (isUnique) {
                        fileName = UUID.randomUUID().toString() + "_" + fileName;
                    }
                    FileUtils.copyInputStreamToFile(inputStream, new File(uploadDir, fileName));
                    fileNames.add(fileName);
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage());
                }
            });
        });
    }

    /**
     * get content length from multi part
     *
     * @param formDataMultiPart - form data multi part
     * @return - content length in bytes
     */
    private static long getContentLengthFromMultiPart(FormDataMultiPart formDataMultiPart) {
        return Long.parseLong(String.valueOf(formDataMultiPart.getHeaders().get("Content-Length").get(0)));
    }

    /**
     * save file to folder on server
     * @param centralConfigService - central config service
     * @param formDataMultiPart - multi part  - file content
     * @param uploadDir - upload dir
     * @param fileName - file name
     */
    public static void saveSpecificFile(CentralConfigService centralConfigService,
                                          FormDataMultiPart formDataMultiPart, String uploadDir, String fileName) {
        int fileUploadMaxSizeMb = centralConfigService.getMutableDescriptor().getFileUploadMaxSizeMb();
        // get uploaded file map
        Map<String, List<FormDataBodyPart>> fields = formDataMultiPart.getFields();
        fields.forEach((name, dataBody) -> {
            List<FormDataBodyPart> formDataBodyParts = fields.get(name);
                // get file name and data
                byte[] fileAsBytes = formDataBodyParts.get(0).getValueAs(byte[].class);
                if (FileUtils.bytesToMeg(fileAsBytes.length) > fileUploadMaxSizeMb && fileUploadMaxSizeMb > 0) {
                    throw new BadRequestException("Uploaded file size is bigger than :" + fileUploadMaxSizeMb);
                }
                String fileLocation = uploadDir + File.separator + fileName;
                FileUtils.writeFile(fileLocation, fileAsBytes);
        });
    }

    /**
     * create temp folder if not exist
     * @param uploadDir - temp directory
     */
    public static void createTempFolderIfNotExist(String uploadDir) {
        try {
            org.apache.commons.io.FileUtils.forceMkdir(new File(uploadDir));
        } catch (IOException e) {
            throw new  RuntimeException(e);
        }
    }

    /**
     * save extracted file to temp folder
     * @param uploadedFile - uploaded file
     */
    public static void saveUploadFileAsExtracted(File uploadedFile, String destFileName) throws Exception {
        try {
            ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
            File destFolder = new File(artifactoryHome.getTempUploadDir(), destFileName);
            ZipUtils.extract(uploadedFile, destFolder);
        } catch (Exception e) {
            String errorMessage = "Error during import of " + uploadedFile.getName() + ": " + e.getMessage();
            log.error(errorMessage, e);
            throw new Exception(errorMessage);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(uploadedFile);
        }
    }

    /**
     * save snippet to temp folder
     * @param snippet - snippet
     * @return - saved snippet name
     */
    public static String saveSettingToTempFolder(String snippet){
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        InputStream inputStream = new ByteArrayInputStream(snippet.getBytes());
        String settingName = "settings" + "_" + UUID.randomUUID().toString();
        File settingFile = new File(uploadDir, settingName);
        FileUtils.copyInputStreamToFile(inputStream,settingFile);
        return settingName;
    }
}
