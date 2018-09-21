package com.mastercard.gateway.android.sdk;


import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class Gateway3DSecurePresenterTest {

    Gateway3DSecurePresenter presenter;

    Gateway3DSecureView mockView = mock(Gateway3DSecureView.class);

    @Before
    public void setUp() throws Exception {
        presenter = new Gateway3DSecurePresenter();
    }

    @After
    public void tearDown() throws Exception {
        reset(mockView);
    }

    @Test
    public void testAttachViewCallsCancelIfHtmlMissing() throws Exception {
        doReturn(null).when(mockView).getExtraHtml();

        presenter.attachView(mockView);

        verify(mockView).cancel();
    }

    @Test
    public void testAttachViewSetsDefaultTitleIfExtraTitleMissing() throws Exception {
        String html = "<html></html>";
        String defaultTitle = "default title";
        String extraTitle = null;

        doReturn(html).when(mockView).getExtraHtml();
        doReturn(defaultTitle).when(mockView).getDefaultTitle();
        doReturn(extraTitle).when(mockView).getExtraTitle();

        presenter.attachView(mockView);

        verify(mockView).setToolbarTitle(defaultTitle);
    }

    @Test
    public void testAttachViewWorksAsExpected() throws Exception {
        String html = "<html></html>";
        String defaultTitle = "default title";
        String extraTitle = "extra title";

        doReturn(html).when(mockView).getExtraHtml();
        doReturn(defaultTitle).when(mockView).getDefaultTitle();
        doReturn(extraTitle).when(mockView).getExtraTitle();

        assertNull(presenter.view);

        presenter.attachView(mockView);

        assertNotNull(presenter.view);

        verify(mockView).setWebViewHtml(html);
        verify(mockView).setToolbarTitle(extraTitle);
    }

    @Test
    public void testDetachViewWorksAsExpected() throws Exception {
        presenter.view = mockView;

        presenter.detachView();

        assertNull(presenter.view);
    }

    @Test
    public void testUrlChangeCallsIntentToEmailIfMailtoLink() throws Exception {
        Uri mockUri = mock(Uri.class);
        doReturn("mailto").when(mockUri).getScheme();
        presenter.view = mockView;

        presenter.webViewUrlChanges(mockUri);

        verify(mockView).intentToEmail(mockUri);
    }

    @Test
    public void testUrlChangeCallsLoadWebViewOnStandardRedirect() throws Exception {
        Uri mockUri = mock(Uri.class);
        doReturn("https").when(mockUri).getScheme();
        presenter.view = mockView;

        presenter.webViewUrlChanges(mockUri);

        verify(mockView).loadWebViewUrl(mockUri);
    }

    @Test
    public void testUrlChangeHandles3DSResultOnMatchingScheme() throws Exception {
        Uri mockUri = mock(Uri.class);
        doReturn(Gateway3DSecurePresenter.REDIRECT_SCHEME).when(mockUri).getScheme();
        presenter.view = mockView;

        Gateway3DSecurePresenter presenterSpy = spy(presenter);

        String acsResult = "{}";
        doReturn(acsResult).when(presenterSpy).getACSResultFromUri(mockUri);

        presenterSpy.webViewUrlChanges(mockUri);

        verify(mockView).complete(acsResult);
    }

    @Test
    public void testGetACSResultFromUriWorksCorrectly() {
        Uri testUri = Uri.parse("gatewaysdk://3dsecure?acsResult={}");

        String acsResult = presenter.getACSResultFromUri(testUri);

        assertEquals("{}", acsResult);
    }
}
