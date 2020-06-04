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
export function jfDisableFeature(ArtifactoryFeatures, $timeout) {
  return {
    restrict: 'A',
    link: function ($scope, $element, $attrs) {
      // console.log($scope, $element, $attrs);
      let feature = $attrs.jfDisableFeature;
      let currentLicense = ArtifactoryFeatures.getCurrentLicense();
      if (!feature) return;
      if (ArtifactoryFeatures.isHidden(feature)) {
        $($element).hide();
      }
      else if (ArtifactoryFeatures.isDisabled(feature)) {
        if (currentLicense === "OSS" || currentLicense === "ConanCE" || currentLicense === "JCR") {
          $timeout(() => {
              $($element).find("*").attr('disabled', true)
          }, 500, false);

          let license = ArtifactoryFeatures.getAllowedLicense(feature);
          // Add the correct class:
          $($element).addClass('license-required-' + license);
          $($element).addClass('license-required');

          // Add a tooltip with link to the feature page:
          let featureName = ArtifactoryFeatures.getFeatureName(feature);
          let featureLink = ArtifactoryFeatures.getFeatureLink(feature);

          let edition = currentLicense === 'ConanCE' ? `<br>Aritfactory Community Edition for C/C++` : currentLicense === 'JCR' ? 'JCR' : 'OSS';
          let tooltipText = featureLink ? `Learn more about the <a href="${featureLink}" target="_blank">${featureName}</a> feature` : `${featureName} feature is not supported in ${edition} version`;


          let generateTooltip = (element, tooltipText) => {
              element.tooltipster({
                  animation: 'fade',
                  contentAsHTML : 'true',
                  trigger: 'hover',
                  onlyOne: 'true',
                  interactive: 'true',
                  interactiveTolerance: 150,
                  position: 'top',
                  theme: 'tooltipster-default top',
                  content: tooltipText
              });
          };

          generateTooltip($($element), tooltipText);

          if (feature === 'publishedmodule') {
            setTimeout(() => {
              $($element).tooltipster('destroy');
              $element = $('.ui-grid-row').find('.ui-grid-disable-selection:first');
                generateTooltip($($element), tooltipText);
            }, 200)
          }
        } else if (feature === 'trustedkeys' && currentLicense != 'EDGE' && currentLicense != 'ENTPLUS') {
          return;
        }
        else {
          $($element).hide();
        }
      }
    }
  }
}
