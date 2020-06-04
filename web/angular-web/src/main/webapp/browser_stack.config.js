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
module.exports = {
  browsers: {
    bs_chrome_mac: {
      base: 'BrowserStack',
      browser: 'chrome',
      os: 'OS X',
      os_version: 'Sierra'
    },
    bs_safari_mac: {
      base: 'BrowserStack',
      browser: 'safari',
      os: 'OS X',
      os_version: 'Sierra'
    },
    bs_firefox_mac: {
      base: 'BrowserStack',
      browser: 'firefox',
      os: 'OS X',
      os_version: 'Sierra'
    },
    bs_chrome_win: {
      base: 'BrowserStack',
      browser: 'chrome',
      os: 'Windows',
      os_version: '10'
    },
    bs_ie_win: {
      base: 'BrowserStack',
      browser: 'ie',
      os: 'Windows',
      os_version: '10'
    },
  }
}