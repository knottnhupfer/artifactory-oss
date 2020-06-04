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
'use strict';
import FooterMock from '../../mocks/footer_mock.browserify';

let FooterDao, $httpBackend, $scope, jqueryElement;

describe('unit test:jf_disable_feature directive', () => {
  function compileDirective() {
    $scope = compileHtml('<div jf-hide-for-aol></div>');
    jqueryElement = $('[jf-hide-for-aol]');
  }
  
  function setup(_FooterDao_, _$httpBackend_) {
  	FooterDao = _FooterDao_;
  	$httpBackend = _$httpBackend_;
	}

  function getFooterData() {
  	FooterDao.get(true);
  	$httpBackend.flush();
  }

  function expectToBeHidden() {
		expect(jqueryElement).toBeHidden();
  }
  function expectToBeVisible() {
		expect(jqueryElement).toBeVisible();
  }

  beforeEach(m('artifactory.templates', 'artifactory.directives'));
  beforeEach(inject(setup));

  describe('AOL', () => {
  	new FooterMock().mockAol();
  	beforeEach(getFooterData);
	  it('should hide the element', () => {
			compileDirective();
			expectToBeHidden();
	  });
  });

  describe('not AOL', () => {
  	new FooterMock();
  	beforeEach(getFooterData);
	  it('should not hide the element', () => {
			compileDirective();
			expectToBeVisible();
	  });
  });
});
