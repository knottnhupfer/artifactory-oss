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
export default {
    globalSearchCriteria: [
        {
            id: 'limitRepo',
            label: 'Limit to Specific Repositories',
            field: 'selectedRepositories',
            type: 'array',
            mandatory: false,
            default: false
        }
    ],
    searchTypes: [
        {
            searchTypeName: 'Quick',
            endPoint: 'quick',
            staticPayload: {
                search: 'quick'
            },
            searchCriteria: [
                {
                    label: 'Value',
                    field: 'query',
                    type: 'string',
                    mandatory: true,
                    default: true
                },
                'limitRepo'
            ]
        },
        {
            searchTypeName: 'Archive',
            endPoint: 'class',
            staticPayload: {
                search: 'class'
            },
            searchCriteria: [
                {
                    label: 'Name',
                    field: 'name',
                    type: 'string',
                    mandatory: false,
                    default: true
                },
                {
                    label: 'Path',
                    field: 'path',
                    type: 'string',
                    mandatory: false,
                    default: false
                },
                {
                    label: 'Search Class Resources Only',
                    field: 'searchClassOnly',
                    type: 'boolean',
                    mandatory: false,
                    default: false
                },
                {
                    label: 'Exclude Inner Classes',
                    field: 'excludeInnerClasses',
                    type: 'boolean',
                    mandatory: false,
                    default: false
                },
                'limitRepo'
            ]
        },
        {
            searchTypeName: 'Property',
            endPoint: 'property',
            staticPayload: {
                search: 'property'
            },
            searchCriteria: [
                {
                    label: 'Property',
                    field: 'propertyKeyValues',
                    type: 'keyVal',
                    multi: true,
                    mandatory: true,
                    default: true
                },
                {
                    label: 'Property Set',
                    field: 'propertySetKeyValues',
                    type: 'keyValSet',
                    multi: true,
                    mandatory: true,
                    default: false
                },
                'limitRepo'
            ]
        },
        {
            searchTypeName: 'Checksum',
            endPoint: 'checksum',
            staticPayload: {
                search: 'checksum'
            },
            searchCriteria: [
                {
                    label: 'Checksum',
                    field: 'checksum',
                    type: 'string',
                    mandatory: true,
                    default: true
                },
                'limitRepo'
            ]
        },
        {
            searchTypeName: 'Remote',
            endPoint: 'remote',
            staticPayload: {
                search: 'remote'
            },
            searchCriteria: [
                {
                    label: 'Search for',
                    field: 'searchKey',
                    type: 'string',
                    mandatory: true,
                    default: true
                }
            ]
        },
        {
            searchTypeName: 'Trash',
            endPoint: 'trash',
            staticPayload: {
                search: 'trash'
            },
            searchCriteria: [
                {
                    label: 'Query',
                    field: 'query',
                    type: 'string',
                    mandatory: true,
                    default: true
                },
                {
                    label: 'Checksum Search',
                    field: 'isChecksum',
                    type: 'boolean',
                    mandatory: false,
                    default: false
                }
            ]
        }
    ]
}
