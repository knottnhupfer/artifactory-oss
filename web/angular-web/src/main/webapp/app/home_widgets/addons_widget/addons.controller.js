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
import FIELD_OPTIONS from '../../constants/field_options.constats';
import EVENTS from '../../constants/artifacts_events.constants';

export class AddonsController {
    constructor(HomePageDao, $timeout, $interval, JFrogEventBus) {
        this.homePageDao = HomePageDao;

        this.$timeout = $timeout;
        this.$interval = $interval;

        this.tabOptions = ['All', 'Package Management', 'Features', 'Ecosystem', 'Available'];
        this.currentType = this.tabOptions[0];

        this.getData();

        $(window).resize(() => {
            this.$timeout(() => this.calculateAddonSize(), 100);
        });
        JFrogEventBus.register(EVENTS.REFRESH_PAGE_CONTENT,()=>{
            this.$widgetObject.showSpinner = true   ;
            this.getData();
        });

        this.addonsAnimation = undefined;
        this.logColor = "color: green;";

        window.move1 = this._move1.bind(this);
        window.move3 = this._move3.bind(this);
        window.moveAll = this._moveAll.bind(this);

        this.lastDeltaX = 1;

    }

    getData() {
        this.homePageDao.get({widgetName:'addon'}).$promise.then((data)=> {
            this.addons = data.widgetData.addons;


            let remainInOneLine = ['git-lfs','sumo-logic','ivy','xray','jfrog-cli','plugin']; // This items should remain in one line
            let brake3WordsAfter2ndWord = ['s3fileStore','gcs','distribution']; // This items has 3 words and needs to brake after the second word
            let brake3WordsAfter1stWord = ['smart-repo','sso']; // This items has 3 words and needs to brake after the first word

            _.forEach(this.addons, (addon) => {
                if (!_.includes(remainInOneLine,addon.name) && !_.includes(brake3WordsAfter2ndWord,addon.name) && !_.includes(brake3WordsAfter1stWord,addon.name)) {
                    addon.displayName = addon.displayName.split(" ").join("\n")
                }
                if (_.includes(brake3WordsAfter2ndWord,addon.name)) {
                    let splitDisplayName = addon.displayName.split(' ');
                    let newDisplayName = splitDisplayName[0] + ' ' + splitDisplayName[1] + '\n' + splitDisplayName[2];

                    addon.displayName = newDisplayName;
                }
                if (_.includes(brake3WordsAfter1stWord,addon.name)) {
                    let splitDisplayName = addon.displayName.split(' ');
                    let newDisplayName = splitDisplayName[0] + '\n' + splitDisplayName[1] + ' ' + splitDisplayName[2];

                    addon.displayName = newDisplayName;
                }
            });


            this.$timeout(() => {
                this.calculateAddonSize();
                this.$widgetObject.showSpinner = false;
            }, 100);
        });
    }

    getIcon(addonName) {
        let packageData = _.find(FIELD_OPTIONS.repoPackageTypes, {value: addonName});
        return 'iconrepo-' + (packageData ? packageData.icon : addonName);
    }

    sortByCurrentType() {
        this.addons = _.filter(this.allAddons, (addon)=> {
            return addon.categories.indexOf(this._camelize(this.currentType)) !== -1;
        });
        $(".addon-icon")
            .removeClass('swelling')

        // Commented out until we get a clearance from Yoav
        // setTimeout(this.animateAddons.bind(this),100)

        this.$timeout(()=>{
            this.freezeSwitchMenu();
        });

    }

    _camelize(str) {
        return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function (letter, index) {
            return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
        }).replace(/\s+/g, '');
    }

    freezeSwitchMenu() {
        let swichMenu = $('.homepage-switch');
        swichMenu.css({
            position: 'fixed',
            width: $('.card.stretch').prop("clientWidth")
        });
        $('.addon-list').css('padding-top', 100);
    }

    calculateAddonSize() {
        let addonWrapperWidth = $('.addons-wrapper').width();
        this.numberOfItemsInLine = Math.round(addonWrapperWidth/200);
        this.tileWidth = addonWrapperWidth/this.numberOfItemsInLine;

        $('.addon-tile').width(this.tileWidth);

        this.ready = true;
    }

    addonPrevButton() {


        if ($('.addon-tile').is(':animated')) return false;

        let addonItem = $('.addon-tile').last();
        addonItem.insertBefore($('.addon-tile').first()).css('margin-left', -this.tileWidth);
        addonItem.animate({ 'margin-left': 0 }, 250);

    }
    addonNextButton() {

        if ($('.addon-tile').is(':animated')) return false;
        let addonItem = $('.addon-tile').first();

        addonItem.animate({ 'margin-left': -this.tileWidth }, 250);
        this.$timeout(() => {
            addonItem.insertAfter($('.addon-tile').last());
            $('.addon-tile').css('margin-left', '');
        }, 250);
    }

    _clearInterval() {
        if (angular.isDefined(this.addonsAnimation)) {
            this.$interval.cancel(this.addonsAnimation);
            this.addonsAnimation = undefined;
        }
    }
    _move1(time = 5000) {
        this._clearInterval();
        this.addonsAnimation = this.$interval(() => {
            if ($('.addon-tile:hover').length > 0) return false;
            this.addonNextButton();

        }, time);
        console.log('%cSwitched to move 1 at a time', this.logColor);
    }

    _move3(time = 5000) {
        this._clearInterval();
        this.addonsAnimation = this.$interval( () => {
            if ($('.addon-tile').is(':animated') || $('.addon-tile:hover').length > 0) return false;
            let addonItems = $('.addon-tile:nth-child(-n+3)');
            addonItems.eq(0).animate({ 'margin-left': -this.tileWidth * 3 }, 250 * 3);
            this.$timeout(() => {
                addonItems.insertAfter($('.addon-tile').last());
                $('.addon-tile').css('margin-left', '');
            }, 250 * 3);
        }, time);
        console.log('%cSwitched to move 3 at a time', this.logColor);
    }
    _moveAll(time = 5000) {
        this._clearInterval();

        this.addonsAnimation = this.$interval( () => {
            if ($('.addon-tile').is(':animated') || $('.addon-tile:hover').length > 0) return false;
            let addonItems = $('.addon-tile:nth-child(-n+' + this.numberOfItemsInLine +')');
            addonItems.eq(0).animate({ 'margin-left': -this.tileWidth * this.numberOfItemsInLine }, 250 * this.numberOfItemsInLine);
            this.$timeout(() => {
                addonItems.insertAfter($('.addon-tile').last());
                $('.addon-tile').css('margin-left', '');
            }, 250 * this.numberOfItemsInLine);
        }, time);

        console.log('%cSwitched to move all the line at a time', this.logColor);
    }

    onMouseWheel($event, $delta, $deltaX, $deltaY) {
        $event.preventDefault();

        if ($deltaX !== 0) {
            if (this.lastDeltaX !== undefined && Math.abs($deltaX) <= this.lastDeltaX) {
                this.lastDeltaX = Math.abs($deltaX);
                return;
            }
            else {
                this.lastDeltaX = Math.abs($deltaX);
            }
        }

        if ($deltaX < 0) {
            this.addonPrevButton();
        }
        else if ($deltaX > 0) {
            this.addonNextButton();
        }
        else if ($delta > 0) {
            this.addonPrevButton();
        }
        else if ($delta < 0) {
            this.addonNextButton();
        }
    }
}