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

package org.artifactory.storage.db.aql.itest.service;

import org.apache.commons.io.IOUtils;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author gidis
 */
public class AqlBuildPromotionOrientedTest extends AqlAbstractServiceTest {
    @Autowired
    private AqlServiceImpl aqlService;

    @Test
    public void itemFilteringByPromotion() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.promotion.user\":{\"$eq\": \"me\"}})"
        );

        List items = results.getResults();
        assertEquals(items.size(), 0);
    }

    @Test
    public void promotion() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "build.promotions.find({\"user\":{\"$eq\": \"me\"}})"
        );

        List promotions = results.getResults();
        assertEquals(promotions.size(), 1);
        assertBuildPromotion(results, "promoter", "me");
    }

    @Test
    public void promotionWithLazy() {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "build.promotions.find({\"user\":{\"$eq\": \"me\"}})"
        );
        AqlJsonStreamer streamer = new AqlJsonStreamer(aqlLazyResult);
        byte[] array;
            array = streamer.read();
            StringBuilder builder=new StringBuilder();
            try {
                while (array != null) {
                    builder.append(new String(array));
                    array = streamer.read();
                }
                String result = builder.toString();
                Assert.assertTrue(result.contains("\"build.promotion.comment\" : \"sending to QA\""));
                Assert.assertTrue(result.contains("\"build.promotion.created_by\" : \"promoter\""));
                Assert.assertTrue(result.contains("\"build.promotion.repo\" : \"qa-local\""));
                Assert.assertTrue(result.contains("\"build.promotion.status\" : \"promoted\""));
                Assert.assertTrue(result.contains("\"build.promotion.user\" : \"me\""));
            } finally {
                IOUtils.closeQuietly(streamer);
            }
    }

    @Test
    public void buldWithPromotionWithLazy() {
        AqlLazyResult<AqlRowResult> aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find({\"number\":{\"$eq\": \"2\"}}).include(\"promotion\")"
        );
        AqlJsonStreamer streamer = new AqlJsonStreamer(aqlLazyResult);
        byte[] array;
            array = streamer.read();
            StringBuilder builder=new StringBuilder();
            try {
                while (array != null) {
                    builder.append(new String(array));
                    array = streamer.read();
                }
                String result = builder.toString();
                Assert.assertTrue(result.contains("\"build.promotion.status\" : \"promoted\""));
                Assert.assertTrue(result.contains("\"build.promotion.created_by\" : \"tester\""));
                Assert.assertTrue(result.contains("\"build.name\" : \"bb\""));
                Assert.assertTrue(result.contains("\"build.promotion.status\" : \"rollback\""));
            } finally {
                IOUtils.closeQuietly(streamer);
            }
    }
}
