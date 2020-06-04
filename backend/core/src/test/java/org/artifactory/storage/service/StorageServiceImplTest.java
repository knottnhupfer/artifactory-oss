package org.artifactory.storage.service;

import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.fs.repo.CacheUnAvailableException;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.mockito.Mockito.*;

/**
 * @author barh
 */
public class StorageServiceImplTest {

    @Test
    void testGetCachedStorageSizeResolveFromDB() throws SQLException {
        //prepare
        StorageServiceImpl storageHalfMock = spy(StorageServiceImpl.class);
        doThrow(CacheUnAvailableException.class).when(storageHalfMock).getStorageSummaryInfoFromCache();
        InternalBinaryService internalBinaryService = mock(InternalBinaryService.class);
        when(internalBinaryService.getBinariesInfo()).thenReturn(mock(BinariesInfo.class));
        storageHalfMock.setInternalBinaryService(internalBinaryService);

        //act
        when(storageHalfMock.getCachedStorageSize()).thenCallRealMethod();

        //verify
        verify(internalBinaryService, times(1)).getBinariesInfo();
    }

    @Test
    void testGetCachedStorageSizeResolveFromCache() throws SQLException {
        //prepare
        StorageServiceImpl storageHalfMock = spy(StorageServiceImpl.class);
        StorageSummaryInfo storageSummaryInfo = mock(StorageSummaryInfo.class);
        when(storageSummaryInfo.getBinariesInfo()).thenReturn(mock(BinariesInfo.class));
        doReturn(storageSummaryInfo).when(storageHalfMock).getStorageSummaryInfoFromCache();
        InternalBinaryService internalBinaryService = mock(InternalBinaryService.class);
        storageHalfMock.setInternalBinaryService(internalBinaryService);

        //act
        when(storageHalfMock.getCachedStorageSize()).thenCallRealMethod();

        //verify
        verify(internalBinaryService, times(0)).getBinariesInfo();
    }

}