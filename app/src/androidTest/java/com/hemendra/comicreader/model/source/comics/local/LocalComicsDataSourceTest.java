package com.hemendra.comicreader.model.source.comics.local;

import android.content.Context;
import android.os.Looper;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalComicsDataSourceTest {

    @Mock
    private Context context;

    @Mock
    private IComicsDataSourceListener listener;

    @Mock
    private File comicsCacheFile;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private LocalComicsDataSource localComicsDataSource;

    @Before
    public void setUp() {
        Looper.prepare();
        localComicsDataSource = new LocalComicsDataSource(context, listener);
        localComicsDataSource.comicsCacheFile = comicsCacheFile;
    }

    @Test
    public void loadComics_whenCacheNotAvailable() throws InterruptedException {
        localComicsDataSource.loadComics();
        Thread.sleep(1000);

        verify(listener).onFailedToLoadComics(ComicsDataSource.FailureReason.NOT_AVAILABLE_LOCALLY);
    }

    @Test
    public void loadComics_whenCacheIsAvailable() throws InterruptedException {
        when(comicsCacheFile.exists()).thenReturn(true);
        when(comicsCacheFile.length()).thenReturn(1024L);

        localComicsDataSource.loadComics();
        Thread.sleep(1000);

        verify(listener).onStartedLoadingComics();
        verify(listener).onComicsLoaded(any(), any());
    }

    @Test
    public void stopLoadingComics_test() {
        localComicsDataSource.stopLoadingComics();
        verify(listener).onStoppedLoadingComics();
    }

}