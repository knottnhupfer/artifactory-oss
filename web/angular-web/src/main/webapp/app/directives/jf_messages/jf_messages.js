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
class jfMessagesController {
    constructor($scope, $state, $window, $location, ArtifactoryState, User, ArtifactoryFeatures, ArtifactoryStorage) {

        this.$state = $state;
        this.user = User;
        this.artifactoryState = ArtifactoryState;
        this.$window = $window;
        this.$location = $location;
        this.features = ArtifactoryFeatures;
        this.artifactoryStorage = ArtifactoryStorage;

        angular.element(this.$window).on('resize', this.handleSizing.bind(this));
        $scope.$on('$destroy', () => {
            angular.element(this.$window).off('resize');
        });

        setTimeout(() => {
            this.handleSizing();

            $(document).on('mouseenter', '.message-text a', () => {
                $('.message-container').addClass('pause-animation')
            });
            $(document).on('mouseleave', '.message-text a', () => {
                $('.message-container').removeClass('pause-animation')
            });
        }, 300);
    }

    isDismissed(dismissCode){
        let message = this.getDissmisabelMessageByCode(dismissCode);
        return message[0] && message[0].isDismissed;
    }

    getDissmisabelMessageByCode(dismissCode){
        return _.filter(this.constantMessages,(msg)=>{
            return (msg.dismissCode && msg.dismissCode == dismissCode);
        });
    }

    getConstantMessages() {
        let msgs = this.artifactoryState.getState('constantMessages');
        if (msgs) {
            this.addPasswordExpirationMessages(msgs);
            this.addAOL_DotComMessage(msgs);
        }
        let withDismissableMessages = [];
        for(let i in msgs){
            let msgObj = msgs[i];
            if(msgObj.dismissCode){

                let dismissibleItem = this.artifactoryStorage.getItem('dismissibleMessages');
                if(!dismissibleItem){
                    this.artifactoryStorage.setItem('dismissibleMessages',{});
                    dismissibleItem = this.artifactoryStorage.getItem('dismissibleMessages');;
                }

                if(!dismissibleItem[msgObj.dismissCode]){
                    dismissibleItem[msgObj.dismissCode] = false;
                    this.artifactoryStorage.setItem('dismissibleMessages',dismissibleItem);
                }

                msgObj.isDismissed = dismissibleItem[msgObj.dismissCode];
            }
            withDismissableMessages.push(msgObj);
        }

        // console.log(withDismissableMessages,msgs);
        this.constantMessages = withDismissableMessages;
        return this.constantMessages;
    }

    addPasswordExpirationMessages(msgs) {
        let daysToExpiration = this.user.currentUser.currentPasswordValidFor;
        let profileUpdatable = this.user.currentUser.profileUpdatable;
        if (daysToExpiration <= 2 && this.$state.current.name !== 'user_profile' && !_.findWhere(msgs,{code: 'expiration'})) {
            msgs.push({
                message: `Your password will expire in ${daysToExpiration} days. ${profileUpdatable ? 'Click <a href="#/profile">here</a> to change it now.' : 'Contact your system administrator to change it.'}`,
                type: 'warning',
                code: 'expiration'
            })
        }
        else if (this.$state.current.name === 'user_profile' && _.findWhere(msgs,{code: 'expiration'})) {
            let index = msgs.indexOf(!_.findWhere(msgs,{code: 'expiration'}));
            msgs.splice(index,1);
        }
    }

    addAOL_DotComMessage(msgs) {

        let endingsMap = {
            com: 'io',
            org: 'info',
            net: 'us'
        };

        if (this.features.isAol()) {
            let domain = this.$location.host();
            let domainWithoutEnding = domain.substr(0,domain.lastIndexOf('.'));
            let ending = domain.substr(domain.lastIndexOf('.')+1);

            let isDotCom = domainWithoutEnding.endsWith('.artifactoryonline');
            if (isDotCom && !_.findWhere(msgs,{code: 'aol_dot_com'})) {
                let url = this.$location.absUrl();
                let newUrl = url.replace('.artifactoryonline.' + ending, '.jfrog.' + endingsMap[ending]);
                msgs.push({
                    message: `artifactoryonline.${ending} has been replaced by <a href="${newUrl}">jfrog.${endingsMap[ending]}</a>. For more information about adjustments you may need to make, please visit our <a href="https://www.jfrog.com/knowledge-base/deprecation-of-artifactoryonline-com-domain/" target="_blank">Knowledge Base</a>.`,
                    type: 'warning',
                    code: 'aol_dot_com'
                })
            }
        }

    }

    dismissMessage(dismissCode){
        // Dismiss for next time the message is fiered
        let dismissibleItem = this.artifactoryStorage.getItem('dismissibleMessages', {});
        dismissibleItem[dismissCode] = true;
        this.artifactoryStorage.setItem('dismissibleMessages',dismissibleItem);
        // Hide message for now
        let toDismiss = this.getDissmisabelMessageByCode(dismissCode);
        toDismiss[0].isDismissed = true;
    }

    getSystemMessage() {
        let msgObj = this.artifactoryState.getState('systemMessage');
        if (msgObj && msgObj.enabled && (msgObj.inAllPages || this.$state.current.name === 'home')) {
            this.systemMessage = msgObj;
            this.handleSizing();
        }
        else
            this.systemMessage = null;

        return this.systemMessage;
    }

    handleSizing() {
        if ($('.constant-message.system').length) {
            let maxMessageSize = this.$window.innerWidth - $('.constant-message.system .message-title').width() - ($('.constant-message.system .message-container').offset().left * 2) - 10,
                    msgText = $('.constant-message.system .message-text');

            if (msgText.find('span').width() > maxMessageSize)
                msgText.css('width', maxMessageSize).addClass('marqueed');
            else
                msgText.css('width', 'auto').removeClass('marqueed');
        }
    }
}

export function jfMessages() {
    return {
        controller: jfMessagesController,
        controllerAs: 'jfMessages',
        bindToController: true,
        templateUrl: 'directives/jf_messages/jf_messages.html'
    }
}
