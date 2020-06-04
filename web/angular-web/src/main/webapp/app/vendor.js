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
/**
 * those files will be loaded and concat by gulp
 * @type {{JS: string[], CSS: string[]}}
 */
const DIST_VENDORS = require('../node_modules/jfrog-ui-essentials/dist/vendor');

module.exports = {

    JS: [
        ...DIST_VENDORS.ESSENTIALS_VENDORS.js.core,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.codemirror.overlay,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.codemirror.xml,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.codemirror.js,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.codemirror.clike,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.codemirror.dialog,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.codemirror.searchCursor,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.xml2js,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.momentDateFormatParser,
        DIST_VENDORS.ESSENTIALS_VENDORS.js.optional.later,
        'node_modules/jf-angular-ui-layout/ui-layout.js',
        'node_modules/angular-hotkeys/build/hotkeys.js',
        'node_modules/selectize/dist/js/standalone/selectize.js',
        'node_modules/angular-selectize2/dist/selectize.js',
        'node_modules/tinycolor2/tinycolor.js',
        'node_modules/angularjs-color-picker/angularjs-color-picker.min.js',
        'node_modules/deep-diff/releases/deep-diff-0.3.2.min.js',
        'node_modules/jfrog-ui-essentials/dist/jfrog-ui-essentials.js',
        'node_modules/jfrog-native-ui/dist/jfrog-native-ui.js',
        'node_modules/createjs-combined/createjs-2015.11.26.min.js',
        'vendor/machine.js',
        'node_modules/d3/dist/d3.min.js',
        'node_modules/billboard.js/dist/billboard.min.js'
    ],

    CSS: [
        ...DIST_VENDORS.ESSENTIALS_VENDORS.css.core,
        DIST_VENDORS.ESSENTIALS_VENDORS.css.optional.codemirror.dialog,
        'node_modules/jf-angular-ui-layout/ui-layout.css',
        'node_modules/angularjs-color-picker/angularjs-color-picker.min.css',
        'node_modules/angular-hotkeys/build/hotkeys.css',
        'node_modules/jf-lessfonts-open-sans/dist/css/open-sans.css',
        'node_modules/selectize/dist/css/selectize.bootstrap3.css',
        'node_modules/font-awesome/css/font-awesome.css',
        'node_modules/jfrog-ui-essentials/dist/jfrog-ui-essentials.css',
        'node_modules/jfrog-native-ui/dist/jfrog-native-ui.css',
        'node_modules/billboard.js/dist/billboard.css'
    ],

    FONTS: [
        ...DIST_VENDORS.ESSENTIALS_VENDORS.fonts.core,
        'node_modules/jf-lessfonts-open-sans/dist/fonts/**/*.{svg,woff,ttf,eot}',
        'node_modules/font-awesome/fonts/*.{svg,woff,ttf,eot}'
    ],

    ASSETS: [
        ...DIST_VENDORS.ESSENTIALS_VENDORS.assets.core,
    ]
};