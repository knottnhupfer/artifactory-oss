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
import fieldOptions from "../../constants/field_options.constats";

export class SetMeUpWidgetController {
    constructor(TreeBrowserDao, SetMeUpModal, JFrogEventBus, ArtifactoryState, GoogleAnalytics) {

        this.treeBrowserDao = TreeBrowserDao;
        this.ArtifactoryState = ArtifactoryState;
        this.GoogleAnalytics = GoogleAnalytics;
        this.SetMeUpModal = SetMeUpModal;
		this.noReposToFetch = false;
        let EVENTS = JFrogEventBus.getEventsDefinition();

		JFrogEventBus.register(EVENTS.REFRESH_SETMEUP_WIZARD, () => {
			this.$widgetObject.showSpinner = true;
			this.fetchAllRepoList();
		});

		this.packageTypes = _.cloneDeep(fieldOptions.repoPackageTypes);
		this.fetchAllRepoList(true);
		this.loading = false;
	}

	setOnScroll() {
		const EDGE = 10;
		setTimeout(() => {
			const scrollParent = $('.list-content-scrolling-container');
			this.scrollParent = scrollParent;
			scrollParent.on('scroll', (e) => {
				if (scrollParent[0].scrollHeight - scrollParent.scrollTop() <= scrollParent[0].clientHeight + EDGE) {
					if(!this.loading){
						this.fetchMoreRepos();
					}
				}

			});
		});
	}

	onFilterChange(){
		this.continueState=null;
		this.fetchAllRepoList();
	}

	fetchAllRepoList(init){
		this.loading = true;
		this.noReposToFetch = false;
		if(this.scrollParent){
			this.scrollParent.scrollTop(0);
		}
		const payload = {
			type: 'root',
			byRepoKey: this.repoFilter || '',
			repositoryTypes: ["LOCAL", "VIRTUAL", "REMOTE"],
		};
		this.treeBrowserDao.getSetMeUpRepos(payload).then((repos) => {
			if (init) {
				this.setOnScroll();
			}
			const filteredRepos = _.map(_.filter(repos,repo=>repo.repoType !== 'trash' &&
					repo.repoType !== 'supportBundles' &&
					repo.repoType !== 'distribution' &&
					repo.repoKey !== '_intransit'),repo=>{
				let packageType = _.find(this.packageTypes,{serverEnumName: repo.repoPkgType});
				if (packageType) repo.icon = packageType.icon;
				return repo;
			});



			this.repos = filteredRepos;
			this.continueState = repos.continueState;
			this.loading = false;
			this.$widgetObject.showSpinner = false;
		});
	}

	fetchMoreRepos(){
    	if(this.noReposToFetch) return;
		this.loading = true;
		const payload = {
			type: 'root',
			byRepoKey: this.repoFilter || '',
			repositoryTypes: ["LOCAL", "VIRTUAL", "REMOTE"],
		};

		if (this.continueState){
			_.assign(payload,{continueState:  this.continueState})
		}

		this.treeBrowserDao.getSetMeUpRepos(payload).then((repos) => {

			if(!repos.continueState){
				this.noReposToFetch = true;
				return;
			}

			const filteredRepos = _.map(_.filter(repos,repo=>repo.repoType !== 'trash' &&
					repo.repoType !== 'supportBundles' &&
					repo.repoType !== 'distribution' &&
					repo.repoKey !== '_intransit'),repo=>{
				let packageType = _.find(this.packageTypes,{serverEnumName: repo.repoPkgType});
				if (packageType) repo.icon = packageType.icon;
				return repo;
			});
			this.repos.push(...filteredRepos) ;
			this.continueState = repos.continueState;
			this.loading = false;
			this.$widgetObject.showSpinner = false;
		});
	}

    showSetMeUp(repo) {
        this.GoogleAnalytics.trackEvent('Homepage' , 'Quick set me up' , repo.repoPkgType, null, repo.repoType);
        this.SetMeUpModal.launch(repo, true);
    }

    filterHasNoMatches() {
        if (!this.repoFilter) return false;

        let count = _.filter(this.repos, (repo)=>_.contains(repo.repoKey,this.repoFilter)).length;
        return count === 0;
    }

}