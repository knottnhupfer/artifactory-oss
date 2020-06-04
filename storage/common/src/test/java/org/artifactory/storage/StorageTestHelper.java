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

package org.artifactory.storage;

import org.jfrog.common.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author gidis
 */
public class StorageTestHelper {

    public static int getFileNumOfLines(String filePath) {
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ResourceUtils.getResourceAsFile(filePath)))){
            while ((br.readLine()) != null) {
                lineCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineCount;
    }

    public static int getKeyPositionLine(String filePath, String key) {
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader((ResourceUtils.getResourceAsFile(filePath))))){
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(key)) {
                    lineCount++;
                    return lineCount;
                }
                lineCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineCount;
    }
}
