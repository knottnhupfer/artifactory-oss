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
export default function GridApiMock(gridOptions) {
  let elementMock = {
    css: jasmine.createSpy('css')
  };
  elementMock.find = jasmine.createSpy('find').and.returnValue(elementMock);
  
  return {
    grid: {
      id: '1234',
      element: elementMock,
      options: gridOptions
    },
    core: {
      getVisibleRows: jasmine.createSpy('rowsRendered').and.returnValue([]),
      on: {
        rowsRendered: jasmine.createSpy('rowsRendered'),
        sortChanged: jasmine.createSpy('sortChanged')
      }
    },
    selection: {
      on: {
        rowSelectionChanged: jasmine.createSpy('rowSelectionChanged'),
        rowSelectionChangedBatch: jasmine.createSpy('rowSelectionChangedBatch')
      },
      selectRow: jasmine.createSpy('selectRow')
    },
    draggableRows: {
      on: {
        rowDropped: jasmine.createSpy('rowDropped')
      }
    },
    pagination: {
      on: {
        paginationChanged: jasmine.createSpy('paginationChanged')
      }
    },
    colResizable: {
      on: {
        columnSizeChanged: jasmine.createSpy('columnSizeChanged')
      }
    }
  }
}