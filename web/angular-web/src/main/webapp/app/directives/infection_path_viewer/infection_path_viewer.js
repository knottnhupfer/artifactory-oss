export function infectionPathViewer() {

    return {
        restrict: 'E',
        scope: {
            infectionPath: '=',
            caption: '=',
            type: '@?'
        },
        templateUrl: 'directives/infection_path_viewer/infection_path_viewer.html',
        controller: infectionPathViewerController,
        controllerAs: 'InfectionPathViewer',
        bindToController: true
    };
}

class infectionPathViewerController {

    constructor() {
        this.devider = 1;
    }

    $onInit() {
        if (this.infectionPath) {
            if (this.infectionPath.length > 4) {
                this.devider = 1.6;
            }
        }
    }
}