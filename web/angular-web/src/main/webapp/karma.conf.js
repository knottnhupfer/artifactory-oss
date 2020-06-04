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
var browserStack = require('./browser_stack.config');
module.exports = function (config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine', 'browserify'],
        files: [
            '../../../../war/src/main/webapp/webapp/vendorScripts*.js',
            '../../../../war/src/main/webapp/webapp/templates*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_core*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_services*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_dao*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_ui*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_directives*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_ui_components*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_views*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_filters*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_states*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_main*.js',
            'specs/spec_helper.js',
            'node_modules/jasmine-jquery/lib/jasmine-jquery.js',
            'mocks/**/**.js',
            'specs/**/**.js'
        ],
        exclude: [
            "artifactory-oss/web/war/src/main/webapp/webapp/css/**",
            "artifactory-oss/web/war/src/main/webapp/webapp/fonts/**"
        ],
        preprocessors: {
            '{specs,mocks}/**/**.js': [],
            '{specs,mocks}/**/**.browserify.js': ['browserify'],
            'mocks/tree_node_mock.browserify.js': ['browserify']
        },
        browserify: {
            debug: true,
            transform: ['babelify']
        },
        junitReporter: {
            outputDir: 'test_results'
        },
        reporters: ['progress'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,

        browserStack: {
            project: 'Artifactory Karma',
            build: process.env.BUILD_NUMBER
        },

        browserDisconnectTimeout: 20000,
        browserDisconnectTolerance: 3,
        browserNoActivityTimeout: 60000,

        // define browsers
        customLaunchers: browserStack.browsers,

        browsers: ['Chrome', 'Firefox'],

        singleRun: false
    });
};
