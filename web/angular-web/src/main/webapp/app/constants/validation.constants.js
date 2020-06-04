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
    "adminGeneral": {
        "min": "Value must be between 0 and 2,147,483,647",
        "max": "Value must be between 0 and 2,147,483,647",
        "dateFormatExpression": "Invalid date format"
    },
    "folderDownload": {
        "min": "Number of downloads must be bigger than 0"
    },
    "adminBackup": {
        "name": "Invalid backup name",
        "xmlName": "Invalid backup name"
    },
    "adminMail": {
        "min": "Port must be between 1 and 65535",
        "max": "Port must be between 1 and 65535"
    },
    "proxies": {
        "min": "Port must be between 1 and 65535",
        "max": "Port must be between 1 and 65535",
    },
    "users": {
        "validator": "Passwords do not match",
        "minlength": "Password must contain at least 4 characters",
        "maxlength": "Username cannot be longer than 64 characters",
        "invalidUsername": "Username cannot contain uppercase letters"
    },
    "groups": {
        "maxlength": "Group name cannot be longer than 64 characters"
    },
    "maintenance": {
        "min": "Value must be between 0 and 99",
        "max": "Value must be between 0 and 99"
    },
    "crowd": {
        "min": "Value must be between 0 and 9999999999999",
        "max": "Value must be between 0 and 9999999999999",
        "url": "Invalid URL"
    },
    "ldapSettings": {
        "ldapUrl": "Invalid LDAP URL"
    },
    "gridFilter": {
        "maxlength": "Filter field exceed max length"
    },
    "properties": {
        "validCharacters": "Name cannot include the following characters * < > ~ ! @ # $ % ^ & ( ) + = - { } [ ] ; , ` / \\",
        "predefinedValues": "Predefined values for the selected type cannot be empty",
        "name": "Name must start with a letter and cannot contain spaces or special characters",
        "xmlName": "Name must start with a letter and cannot contain spaces or special characters",
        "notPrefixedWithNumeric": "Name must start with a letter and cannot contain spaces or special characters"
    },
    "repoLayouts": {
        "pathPattern": "Pattern must contain at-least the following tokens 'module', 'baseRev' and 'org' or 'orgPath'"
    },
    "bintray": {
        "required": "API Key / Username cannot be empty"
    },
    "licenses": {
        "validateLicense": "License name contains illegal characters"
    },
    "propertySet": {
        "name": "Property set name must start with a letter and contain only letters, digits, dashes or underscores",
        "xmlName": "Property set name must start with a letter and contain only letters, digits, dashes or underscores"
    },
    "reverseProxy": {
        "port": "Port is not available"
    },
    "distRepo": {
        "existRuleName": "Rule name already in use",
    }
};