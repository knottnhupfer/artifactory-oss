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
 * Created by idannaim on 8/2/15.
 */
let artifactoryState;

describe('Unit test: ArtifactoryState', () => {
    let mockState = {
        name: 'test',
        state: 'myState'
    };
    beforeEach(m('artifactory.services'));
    beforeEach(inject((ArtifactoryState) => {
        artifactoryState = ArtifactoryState;
    }));

    it('should save new state', ()=> {
        artifactoryState.setState(mockState.name, mockState.state);

        expect(artifactoryState.getState(mockState.name)).toEqual(mockState.state);
    });

    it('should remove saved state', ()=> {
        artifactoryState.setState(mockState.name, mockState.state);
        artifactoryState.removeState(mockState.name);
        expect(artifactoryState.getState(mockState.name)).not.toBeDefined();
    });

    it('should check if get undefined not get error', ()=> {
        expect(artifactoryState.getState(mockState.name)).not.toBeDefined();
    });

    it('should remove all saved states', ()=> {
        artifactoryState.setState(mockState.name, mockState.state);
        artifactoryState.clearAll();
        expect(artifactoryState.getState(mockState.name)).not.toBeDefined();
    });
});

