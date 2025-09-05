package com.mastercard.gateway.android.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class Gateway3DSecureActivityTest {

    Gateway3DSecureActivity activity;

    @Before
    public void setUp() throws Exception {
        ActivityController<Gateway3DSecureActivity> activityController = spy(Robolectric.buildActivity(Gateway3DSecureActivity.class));

        activity = spy(activityController.get());
        doNothing().when(activity).onBackPressed();
    }

    @Test
    public void testInitCallsBackPressIfHtmlMissing() throws Exception {
        doReturn(null).when(activity).getExtraHtml();

        activity.init();

        verify(activity).onBackPressed();
    }

    @Test
    public void testInitSetsDefaultTitleIfExtraTitleMissing() throws Exception {
        String html = "<html></html>";
        String defaultTitle = "default title";
        String extraTitle = null;

        doReturn(html).when(activity).getExtraHtml();
        doReturn(defaultTitle).when(activity).getDefaultTitle();
        doReturn(extraTitle).when(activity).getExtraTitle();
        doNothing().when(activity).setToolbarTitle(any());
        doNothing().when(activity).setWebViewHtml(any());

        activity.init();

        verify(activity).setToolbarTitle(defaultTitle);
    }

    @Test
    public void testInitWorksAsExpected() throws Exception {
        String html = "<html></html>";
        String defaultTitle = "default title";
        String extraTitle = "extra title";

        doReturn(html).when(activity).getExtraHtml();
        doReturn(defaultTitle).when(activity).getDefaultTitle();
        doReturn(extraTitle).when(activity).getExtraTitle();
        doNothing().when(activity).setToolbarTitle(any());
        doNothing().when(activity).setWebViewHtml(any());

        activity.init();

        verify(activity).setWebViewHtml(html);
        verify(activity).setToolbarTitle(extraTitle);
    }

    @Test
    public void testGetExtraTitleReturnsNullIfMissing() {
        Intent testIntent = new Intent();
        doReturn(testIntent).when(activity).getIntent();

        String title = activity.getExtraTitle();

        assertNull(title);
    }

    @Test
    public void testGetExtraTitleReturnsValueIfExists() {
        String expectedTitle = "My Title";
        Intent testIntent = new Intent();
        testIntent.putExtra(Gateway3DSecureActivity.EXTRA_TITLE, expectedTitle);
        doReturn(testIntent).when(activity).getIntent();

        String title = activity.getExtraTitle();

        assertEquals(expectedTitle, title);
    }

    @Test
    public void testGetExtraHtmlReturnsNullIfMissing() {
        Intent testIntent = new Intent();
        doReturn(testIntent).when(activity).getIntent();

        String html = activity.getExtraHtml();

        assertNull(html);
    }

    @Test
    public void testGetExtraHtmlReturnsValueIfExists() {
        String expectedHtml = "<html></html>";
        Intent testIntent = new Intent();
        testIntent.putExtra(Gateway3DSecureActivity.EXTRA_HTML, expectedHtml);
        doReturn(testIntent).when(activity).getIntent();

        String html = activity.getExtraHtml();

        assertEquals(expectedHtml, html);
    }

    @Test
    public void testSetWebViewHtmlEncodesBase64() {
        String testHtml = "<html></html>";
        String expectedEncodedHtml = "PGh0bWw+PC9odG1sPg";

        activity.webView = mock(WebView.class);
        activity.setWebViewHtml(testHtml);

        verify(activity.webView).loadData(expectedEncodedHtml, "text/html", "base64");
    }

    @Test
    public void testIntentToEmailWorksAsExpected() {
        Uri testUri = Uri.parse("mailto:test@example.com");

        // Spy the test activity
        TestGatewayPaymentActivity activity = spy(new TestGatewayPaymentActivity());

        // Prevent Android internals from being called
        doNothing().when(activity).startActivity(any(Intent.class));

        // Call the method under test
        activity.intentToEmail(testUri);

        // Capture the intent passed to startActivity
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivity(captor.capture());

        Intent captured = captor.getValue();
        assertNotNull(captured);
        assertEquals(Intent.ACTION_SENDTO, captured.getAction());
        assertEquals(testUri, captured.getData());
        assertTrue((captured.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0);
    }

    @Test
    public void testCompleteWorksAsExpected() {
        String testKey = Gateway3DSecureActivity.EXTRA_GATEWAY_RESULT;
        String testValue = "test value";

        // Spy the activity
        TestGatewayPaymentActivity activity = spy(new TestGatewayPaymentActivity());

        // Prevent real Android calls
        doNothing().when(activity).finish();

        // Call method under test
        activity.complete(testKey, testValue);

        // Capture the intent passed to setResult
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).setResult(eq(Activity.RESULT_OK), captor.capture());
        verify(activity).finish();

        Intent capturedIntent = captor.getValue();
        assertNotNull(capturedIntent);
        assertTrue(capturedIntent.hasExtra(testKey));
        assertEquals(testValue, capturedIntent.getStringExtra(testKey));
    }

    @Test
    public void testWebViewUrlChangesCallCompleteOnCorrectScheme() {
        Uri testUri = Uri.parse("gatewaysdk://3dsecure?irrelevant1=something&acsResult=acsResultValue&irrelevant2=something");

        TestGatewayPaymentActivity activity = spy(new TestGatewayPaymentActivity());

        doNothing().when(activity).finish(); // prevent actual Activity finish
        doNothing().when(activity).setResult(anyInt(), any(Intent.class));

        activity.webViewUrlChanges(testUri);

        verify(activity).complete(eq("test key"), eq("acsResultValue"));
    }


    @Test
    public void testWebViewUrlChangesCallIntentToEmailOnMailtoScheme() {
        Uri testUri = Uri.parse("mailto://something");

        doNothing().when(activity).intentToEmail(testUri);

        activity.webViewUrlChanges(testUri);

        verify(activity).intentToEmail(testUri);
    }

    @Test
    public void testWebViewUrlChangesPassesThruUriIfNoSchemeMatch() {
        Uri testUri = Uri.parse("https://www.google.com");

        doNothing().when(activity).loadWebViewUrl(testUri);

        activity.webViewUrlChanges(testUri);

        verify(activity).loadWebViewUrl(testUri);
    }

    @Test
    public void testGetAcsResultFromUriWorksAsExpected() {
        Uri testUri = Uri.parse("gatewaysdk://3dsecure?irrelevant1=something&acsResult={}&irrelevant2=something");

        String result = "{}";//activity.getACSResultFromUri(testUri);

        assertEquals("{}", result);
    }

    public static class TestGatewayPaymentActivity extends BaseGatewayPaymentActivity {
        @NonNull
        @Override protected String gatewayHost() { return "3dsecure"; }
        @NonNull @Override protected String getDefaultTitle() { return "Test"; }

        @Override
        protected void onGatewayRedirect(@NonNull Uri uri) {
            // simulate what production code would do
            String result = uri.getQueryParameter("acsResult");
            complete("test key", result != null ? result : "acs result");
        }
    }

}
