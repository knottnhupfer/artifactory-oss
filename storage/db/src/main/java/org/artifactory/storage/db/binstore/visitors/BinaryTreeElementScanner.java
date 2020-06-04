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

package org.artifactory.storage.db.binstore.visitors;

import com.google.common.collect.Lists;
import org.jfrog.storage.binstore.ifc.model.BinaryTreeElement;

import java.util.List;

/**
 * @author gidis
 */
public class BinaryTreeElementScanner<T, Y> {

    public BinaryTreeElement<Y> scan(BinaryTreeElement<T> binaryTreeElement, BinaryTreeElementHandler<T, Y> handler) {
        BinaryTreeElement<Y> element = new BinaryTreeElement<>();
        Y data = handler.visit(binaryTreeElement);
        if (data != null) {
            element.setData(data);
            if (binaryTreeElement.getNextBinaryTreeElement() != null) {
                element.setNextBinaryTreeElement(scan(binaryTreeElement.getNextBinaryTreeElement(), handler));
            }
            List<BinaryTreeElement<Y>> list = Lists.newArrayList();
            element.setSubBinaryTreeElements(list);
            for (BinaryTreeElement<T> subElement : binaryTreeElement.getSubBinaryTreeElements()) {
                list.add(scan(subElement, handler));
            }
            return element;
        } else {
            if (binaryTreeElement.getNextBinaryTreeElement() != null) {
                return scan(binaryTreeElement.getNextBinaryTreeElement(), handler);
            }
        }
        return null;
    }
}
