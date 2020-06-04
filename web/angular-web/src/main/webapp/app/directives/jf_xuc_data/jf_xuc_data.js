class jfXucDataController {

    constructor(JFrogTableViewOptions, $scope, MiniXrayDao,$q,JFrogModal) {
        this.JFrogTableViewOptions = JFrogTableViewOptions;
        this.$scope = $scope;
        this.$q = $q;
        this.modal = JFrogModal;
        this.miniXrayDao = MiniXrayDao;
    }

    $onInit() {
        this.tableViewOptions = new this.JFrogTableViewOptions(this.$scope);
        this.columns = this.getColumns();
        this.tableViewOptions
                .setColumns(this.columns)
                .setRowsPerPage(20)
                .setEmptyTableText(`No Vulnerabilities`)
                .setObjectName('Vulnerability/Vulnerabilities')
                .showFilter(false)
                .sortBy('severity')
                .setData([]);
        this.getData()
        this.showTable = true;
    }

    getColumns() {

        return [
            {
                header: 'Summary',
                field: 'summary',
                sortable: true,
                filterable: true,
                cellTemplate: '<div ng-click="grid.appScope.jfXucData.showIssueDetails(row.entity)"><a>{{row.entity.summary}}</a></div>'
            },
            {
                header: 'Severity',
                sortable: true,
                field: 'severity',
                cellTemplate: '<div class=" ui-grid-cell-contents severity {{row.entity.severity.toLowerCase()}}">{{row.entity.severity}}</div>',
            },
            {
                header: 'Component',
                field: 'component',
                cellTemplate: '<div>{{row.entity.component }}</div>',
                width: '20%'
            },
            {
                header: 'Infected Version',
                field: 'infected_version',
                cellTemplate: '<div>{{row.entity.component_versions.vulnerable_versions.join(", ") }}</div>',
                width: '10%'
            },
            {
                header: 'Fix Version',
                sortable: false,
                field: 'fix_version',
                cellTemplate: '<div><span ng-if="grid.appScope.jfXucData.hasFixVersion(row.entity)">{{row.entity.component_versions.fixed_versions.join(", ") }}</span> <span  ng-if="!grid.appScope.jfXucData.hasFixVersion(row.entity)">N/A</span></div>',
                width: '10%'
            }
        ]
    }

    hasFixVersion(row) {
        return row.component_versions && row.component_versions.fixed_versions && row.component_versions.fixed_versions.length
    }

    getData() {
        const payload = {
            package_id: this.packageId,
            version: this.version
        }
        this.miniXrayDao.getSecurityVulnerabilites(payload).$promise.then((data) => {
            console.log("recieved data", data)
            if (data && data.data) {
                this.tableViewOptions.setData(data.data);
            }
        });
    }

    showIssueDetails(row) {
        console.log("Need to show data for", row);
        let version;
        if(this.version){
             version = `:${this.version}`;
        }else{
             version =  "";
        }

        const payload = {
            "component_id": this.packageId + version ,
            "source_comp_id": row.source_comp_id,
            "source_name": row.component,
            "vulnerability_id": row.id,
        }
        this.$q.all([this.miniXrayDao.getSecurityImpactGraph(payload).$promise,this.miniXrayDao.getSecurityDetails(payload).$promise]).then((data)=>{

            let modalScope = this.$scope.$new();
            let graph = data[0];
            let alert = data[1];
            modalScope.ctrl = this;
            if (alert.versions) {
                if (alert.versions.fixed_versions) {

                    alert.fixed_versions = alert.versions.fixed_versions;
                }
                alert.versions = alert.versions.vulnerable_versions;
            }
            modalScope.itemClicked = this.itemClicked;
            this.selectedIndex = 0;
            modalScope.wideMode = false;
            modalScope.infected_comp = this.packageId.split("://")[1];


            modalScope.details = alert;
            modalScope.graph = graph;
            modalScope.details = this.orderDetailsObj(modalScope.details);
            modalScope.graph.component_id = "TEST MODAL SCOPE GRAPH"
            if (graph.impact_paths && !graph.impact_paths.length) {
                modalScope.wideMode = true;
            }
            console.log("all resolved!",data)


            this.modal.launchModal('issue_modal', modalScope).result.then(() => {

            });
        });


    }

    orderDetailsObj(detailsObj) {

        console.log('details : ', detailsObj);
        let tmpObj = _.cloneDeep(detailsObj);
        let finaObj = {};

        finaObj = {
            summary: tmpObj.summary,
            description: tmpObj.description,
            type: tmpObj.type,
            provider: tmpObj.provider,
            severity: tmpObj.severity,
            update: tmpObj.updated,
            cves: tmpObj.cves,
            package_type: tmpObj.package_type,
            references: tmpObj.references,
            infected_component: tmpObj.infected_component,
            source_version: tmpObj.source_version,
            infected_versions: tmpObj.infected_versions,
            fixed_versions: tmpObj.fixed_versions,
            watch_target: tmpObj.watch_target,
            watch_name: tmpObj.watch_name,
            matched_policies: tmpObj.policies
        }


        return _.omit(finaObj, _.isEmpty);
    }
    itemClicked($index, ctrl) {
        console.log("Item Clicked",$index,ctrl)
        ctrl.selectedIndex = $index;

    }
    getNomalizedKey(key) {

        if (key.toLowerCase() == 'cves') {
            return 'CVEs';
        }
        return _.startCase(key);
    }
    isLink(str) {
        if (typeof str != 'string' || !str) {
            return false;
        }
        str = str.trim();
        return str && str.startsWith('http://') || str.startsWith('https://') || str.startsWith('www.');
    }
    isArray(o) {
        return _.isArray(o);
    }
}

export function jfXucData() {

    return {
        restrict: 'E',
        scope: {
            packageId: '=',
            version: '=',

        },
        controller: jfXucDataController,
        controllerAs: 'jfXucData',
        templateUrl: 'directives/jf_xuc_data/jf_xuc_data.html',
        bindToController: true
    };
}
