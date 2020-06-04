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
 * Created by tomere on 12/23/2016.
 */
export class SaveArtifactoryHaLicenses {
    constructor(HaLicensesDao) {
        this.haLicensesDao = HaLicensesDao;
    }

    splitText(text) {
        // let splitted = this.splitLicensesTextByWrappingStrings(text);
        // return splitted.length !== 0 ? splitted : this.splitLicensesTextByDelimiters(text);
        let cleanText = this.removeComments(text);
        return this.splitLicensesTextByDelimiters(cleanText);
    }

    removeComments(text){
        return text.replace(/#+((?:.)+?)*/g,'');
    }

    splitLicensesTextByDelimiters(text) {
        let splittedText = text.split(/[,;]+|\n{2,}|(?:\r\n){2,}/g);
        if (splittedText[splittedText.length - 1] == "") {
            splittedText.pop();
        }
        return splittedText;
    }

    toLicensesObjArray(splittedText, key) {
        let res = [];
        for (let i in splittedText) {
            let textBlock = {};
            textBlock[key] = splittedText[i];
            res.push(textBlock);
        }
        return res;
    }

    toLicensesJson(rawText) {
        let splittedText = this.splitText(rawText),
            licensesObjArray = this.toLicensesObjArray(splittedText, "licenseKey"),
            licensesJson = {
                'licenses': licensesObjArray
            };

        return licensesJson;
    }

    saveLicenses(options,rawText) {
        let licensesJson = this.toLicensesJson(rawText);
        return this.haLicensesDao.add(options,licensesJson).$promise;
    }

/*
    splitLicensesTextByWrappingStrings(text) {
        let splittedText = [];

        if (text.indexOf('#Start License Key #') === 0) {
            return splittedText;
        }

        let pattern = /#Start License Key #\d+((?:.|\n|\r\n)+?)#End License Key #\d+/g;
        let match;
        while ((match = pattern.exec(text)) !== null) {
            splittedText.push(match[1]);
        }

        return splittedText;
    }*/

}