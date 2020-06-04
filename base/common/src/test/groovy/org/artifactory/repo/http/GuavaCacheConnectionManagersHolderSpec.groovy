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

package org.artifactory.repo.http

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import spock.lang.Specification

/**
 * @author Gal Ben Ami
 */
class GuavaCacheConnectionManagersHolderSpec extends Specification {

    def "explicit add remove"() {
        given:
        ConnectionManagersHolder holder = new GuaveCacheConnectionManagersHolder()

        when:
        holder.put("a", new PoolingHttpClientConnectionManager())
        holder.put("b", new PoolingHttpClientConnectionManager())
        holder.put("c", new PoolingHttpClientConnectionManager())

        then:
        holder.size() == 3

        when:
        holder.remove("a")
        holder.remove("b")

        then:
        holder.size() == 1

    }

    def "test gc cleans unreferenced keys"() {
        given:
        List keys = [new String("a"), new String("b"), new String("c")]
        ConnectionManagersHolder holder = new GuaveCacheConnectionManagersHolder()


        when:
        keys.each { String key ->
            holder.put(key, new PoolingHttpClientConnectionManager())
        }

        then:
        holder.size() == 3

        when: "setting the keys to null, so it will get collected by gc. Enforce gc"
        keys = null
        System.gc()
        System.finalize()
        ((GuaveCacheConnectionManagersHolder) holder).connections.cleanUp()

        then: "holder vals are getting cleaned"
        holder.values().size() == 0

    }
}