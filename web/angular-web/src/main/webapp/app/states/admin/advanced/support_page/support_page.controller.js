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
 */
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";
//import CONFIG_MESSAGES from "../../../../constants/configuration_messages.constants";

export class AdminAdvancedSupportPageController {
    constructor(SupportPageDao, ServerTimeDao, JFrogTableViewOptions, $rootScope, $scope, JFrogIFrameDownload, GeneralConfigDao, RESOURCE, JFrogNotifications, JFrogModal) {

	    this.$scope = $scope;
	    this.$rootScope = $rootScope;
	    this.supportPageDao = SupportPageDao;
	    this.JFrogTableViewOptions = JFrogTableViewOptions;
	    this.GeneralConfigDao = GeneralConfigDao;

	    this.modal = JFrogModal;
	    this.serverTimeDao = ServerTimeDao;
	    this.iFrameDownload = JFrogIFrameDownload;
	    this.RESOURCE = RESOURCE;

        this.artifactoryNotifications = JFrogNotifications;

	    this.GeneralConfigDao.get().$promise.then((data) => {
	        this.dateFormat = this._getDatePartFromFormat(data.dateFormat);
		    this._init();
        });
    }

    _init() {

    	this.timePeriodConfig = {
		    maxItems: 1,
		    create: false
	    };

	    this.timePeriodOptions = [
		    {text: "Last 24 Hours", value: 1},
		    {text: "Last 3 Days", value: 3},
		    {text: "Last 5 Days", value: 5},
		    {text: "Last 7 Days", value: 7},
		    {text: "Custom Dates", value: 'CUSTOM'}
	    ];

	    this.timePeriodSelection = 1;

	    this.supportPageDao.listBundles().$promise.then((data) => {
		    this.bundles = this.formatStatus(data);
	        this.setupBundlesTable();
	    });



        this.serverTimeDao.get().$promise.then((serverTimeResource) => {
	        let serverTime = parseInt(_.map(serverTimeResource.toJSON()).join(''));

	        // get today date (based on -server time-)
	        this.today = moment(serverTime).format();
        });

        let basicDatePickerOptions = {format : this._getDatePartFromFormat(this.dateFormat), maxDate: this.today, toolbarPlacement: 'bottom'};
        this.toDateOptions = this.fromDateOptions = basicDatePickerOptions;

    }
	setupBundlesTable() {
		this.bundlesTableOptions = new this.JFrogTableViewOptions(this.$scope);
		this.bundlesTableOptions.setId('bundles-list')
		    .setObjectName('Bundle')
		    .setEmptyTableText('No Support Bundles have been created.')
		    .sortBy('create_date')
		    .reverseSortingDir()
		    .setColumns(this.getBundlesListColumns())
		    .setActions(this.getRowActions())
			.setNewEntityAction(() => {
				this.openNewBundleModal();
			});

		this.bundlesTableOptions.newEntityCustomText = "Create New Bundle";
		this.bundlesTableOptions.setData(this.bundles);
	}
	getRowActions() {
		return [
			{
				name: 'Download',
				icon: 'icon icon-download',
				callback: row => this.downloadBundle(row.id),
				tooltip: 'Download'
			},
			{
				name: 'Delete',
				icon: 'icon icon-clear',
				callback: row => this.deleteBunbdle(row.id),
				tooltip: 'Delete'
			}
		]
	}
    getBundlesListColumns() {

        return [
            {
                header: 'Name',
	            field: 'name',
                sortable: true,
	            filterable: true,
	            cellTemplate: '<div>{{row.entity.name}}</div>'
            },
	        {
		        header: 'Description',
		        field: 'description',
		        cellTemplate: '<div>{{row.entity.description}}</div>',
	        },
	        {
		        header: 'Create Date',
		        field: 'create_date',
		        cellTemplate: '<div>{{row.entity.created | date:\'yyyy-MM-dd HH:mm:ss Z\'}}</div>',
		        sortable: true
	        },
	        {
		        header: 'Status',
		        field: 'status',
		        cellTemplate: '<div>{{row.entity.formatedStatus }}</div>',
		        width: '10%'
	        }
        ]

	}
	openNewBundleModal() {
		let modalScope = this.$scope.$new();
		modalScope.title = "Create New Support Bundle";

		// defaults
		modalScope.endDate = this.today;
		modalScope.bundleData = {
			configuration: true,
			systemInfoConfiguration: true,
			systemLogsConfiguration: true,
			threadDumpConfiguration: true,
			threadDump: {
				count: 1,
				interval: 0
			}
		};

		modalScope.onChangeTimePeriod = () => {
			let timePeriodSelection = modalScope.bundleData.timePeriodSelection;

			if (timePeriodSelection !== 'CUSTOM') {
				modalScope.bundleData.endDate = moment(this.today).format();
				modalScope.bundleData.startDate = moment(this.today).subtract(timePeriodSelection - 1, 'days').format();

			} else {

				this.onChange = (data) => {
					let startDate = moment(data.startDate).format();
					let endDate = moment(data.endDate).format();

					modalScope.bundleData.startDate = startDate;
					modalScope.bundleData.endDate = endDate;


					if (data.state === 'endDate' && moment(endDate).isBefore(moment(startDate))) {
						modalScope.bundleData.startDate = endDate;
					} else if (data.state === 'startDate' && moment(endDate).isBefore(moment(startDate))) {
						modalScope.bundleData.endDate = startDate;
					}
				}

			}
		};

		modalScope.createBundle = () => {

			let json = {
				parameters: {
					configuration: modalScope.bundleData.configuration,
					system: modalScope.bundleData.systemInfoConfiguration,
					logs: {
						include: modalScope.bundleData.systemLogsConfiguration,
						start_date: moment(modalScope.bundleData.startDate).format('YYYY-MM-DD'),
						end_date: moment(modalScope.bundleData.endDate).format('YYYY-MM-DD')
					},
					thread_dump: {
						count: !modalScope.bundleData.threadDumpConfiguration ? 0 : modalScope.bundleData.threadDump.count,
						interval: modalScope.bundleData.threadDump.interval
					}
				},
				name: modalScope.bundleData.supportBundleName,
				description: modalScope.bundleData.supportBundleDescription
			};

			this.supportPageDao.generateBundle(json).$promise.then((data) => {
				this.updateBundlesList();
				this.modalInstance.close();
			});
		};

		this.modalInstance = this.modal.launchModal('support_bundle_modal', modalScope, 1000);
	}
	updateBundlesList() {
		this.supportPageDao.listBundles().$promise.then((data) => {
			this.bundles = this.formatStatus(data);
			this.bundlesTableOptions.setData(this.bundles);
		});
	}
	formatStatus(data) {
    	let options = {
    		success: 'Success',
		    in_progress: 'In progress',
		    failure: 'Failure'
	    };
    	data.forEach(item => {
    		item.formatedStatus = options[item.status] || '';
	    });

    	return data;
	}
	downloadBundle(bundleId) {
		let url = this.RESOURCE.API_URL + '/userSupport/downloadBundle/' + bundleId;
		this.iFrameDownload(url);
	}
	deleteBunbdle(bundleId) {
		this.modal.confirm(`Are you sure you want to delete this bundle?`)
		    .then(() => {
			    this.supportPageDao.deleteBundle({}, {bundleId}).$promise.then(()=> {
				    this.artifactoryNotifications.create({'info': 'Bundle deleted successfully.'});
				    this.updateBundlesList();
			    })
		    });
	}
	_getDatePartFromFormat(format) {
		let parts = this._breakFormat(format);

		let currContext = 'U';  //U = Unkown D = Date T = Time
		let unknowns = [];

		let gotMonth = false;
		for (let i in parts) {
			let part = parts[i];

			if (_.contains('dy',part.char)) {
				part.context = 'D';
			}
			else if (_.contains('hs',part.char)) {
				part.context = 'T';
			}
			else if (part.char === 'm') {
				if (gotMonth) currContext = 'U';
				part.context = !gotMonth && currContext === 'D' ? 'D' :'U';
				unknowns.push(part);
			}
			if (part.context) currContext = part.context;
			if (currContext !== 'U' && unknowns.length) {
				for (let i in unknowns) {
					unknowns[i].context = currContext;
					if (currContext === 'D') gotMonth = true;
				}
				unknowns = [];
			}
		}

		let insideDate=false;
		let justDate = [];
		for (let i in parts) {
			let part = parts[i];
			if (part.context === 'D') {
				insideDate = true;
			}
			else if (part.context === 'T') {
				insideDate = false;
			}
			if (insideDate) justDate.push(part);
		}

		let trim = 0;
		for (let i = justDate.length - 1; i>=0; i--) {
			let part = parts[i];
			if (part.context) {
				break;
			}
			else justDate.pop();
		}

		let finalResult = '';
		for (let i in justDate) {
			let part = justDate[i];
			finalResult += part.precise;
		}

		return moment().toMomentFormatString(finalResult);


	}
	_breakFormat(format) {
		let parts = [];
		while (format.length) {
			let part = this._getNextFormatPart(format);
			parts.push(part);
			format = format.substr(part.count);
		}
		return parts;
	}
	_getNextFormatPart(format) {
		let temp = format.toLowerCase();
		let char = temp.charAt(0);
		let count = 0;
		while (temp.charAt(0) === char) {
			count++;
			temp = temp.substr(1);
		}
		let precise = format.substr(0,count);
		return {
			char: char,
			count: count,
			precise: precise
		}
	}
}