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
var vendorPaths = require('./app/vendor');

module.exports = {

    SOURCES: {
        APPLICATION_JS: 'app/**/*.js',
        TEMPLATES: 'app/**/**/*.html',
        REQUIRED_TEMPLATES: 'app/ui_components/artifactory_grid/templates/*.html',
        VENDOR_SCRIPTS : vendorPaths.JS,
        VENDOR_CSS : vendorPaths.CSS,
        VENDOR_ASSETS: vendorPaths.ASSETS,
        VENDOR_FONTS: vendorPaths.FONTS,
        LESS: ['app/assets/stylesheets/**/*.less', 'app/ui_components/**/*.less', 'app/directives/**/*.less'],
        LESS_MAIN_FILE: 'app/assets/stylesheets/main.less',
        INDEX : 'app/app.html',
        STYLEGUIDE: 'app/styleguide.html',
        FONTS : 'app/assets/fonts/**',
        VENDOR_JS : 'app/vendor.js',
        IMAGES : 'app/assets/images/**',
        JQUERY_UI_IMAGES : 'app/assets/images/jqueryui/**',
        JFROG_UI_IMAGES : 'node_modules/jfrog-ui-essentials/dist/images/**',
        JFROG_UI_FONTS : 'node_modules/jfrog-ui-essentials/dist/fonts/**',
        JFUIE_WEBWORKER: 'node_modules/jfrog-ui-essentials/dist/workers/**',
        MEDIUM_SVG_ICONS: 'app/assets/svgicons/*.svg'
    },

    DESTINATIONS: {
        TARGET: '../../../../war/src/main/webapp/webapp',
        TARGET_REV: [
            '../../../../war/src/main/webapp/webapp/**'
        ]
    }
};