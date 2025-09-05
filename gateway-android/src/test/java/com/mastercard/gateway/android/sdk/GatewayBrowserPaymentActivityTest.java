package com.mastercard.gateway.android.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class GatewayBrowserPaymentActivityTest {

    @Test
    public void testGetDefaultTitle() {
        GatewayBrowserPaymentActivity activity = new GatewayBrowserPaymentActivity();
        assertEquals("Payment", activity.getDefaultTitle());
    }

    @Test
    public void testGatewayHost() {
        GatewayBrowserPaymentActivity activity = new GatewayBrowserPaymentActivity();
        assertEquals("browserpayment", activity.gatewayHost());
    }

    @Test
    public void testOnGatewayRedirectCallsCompleteWithOrderResult() {
        Uri testUri = Uri.parse("gatewaysdk://browserpayment?irrelevant=foo&orderResult=success123");

        GatewayBrowserPaymentActivity activity = spy(new GatewayBrowserPaymentActivity());

        // Prevent Android internals
        doNothing().when(activity).finish();
        doNothing().when(activity).setResult(anyInt(), any(Intent.class));

        activity.onGatewayRedirect(testUri);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).setResult(eq(Activity.RESULT_OK), captor.capture());
        verify(activity).finish();

        Intent captured = captor.getValue();
        assertNotNull(captured);
        assertEquals("success123", captured.getStringExtra(BaseGatewayPaymentActivity.EXTRA_GATEWAY_RESULT));
    }

    @Test
    public void testOnGatewayRedirectHandlesMissingOrderResult() {
        Uri testUri = Uri.parse("gatewaysdk://browserpayment?foo=bar");

        GatewayBrowserPaymentActivity activity = spy(new GatewayBrowserPaymentActivity());

        doNothing().when(activity).finish();
        doNothing().when(activity).setResult(anyInt(), any(Intent.class));

        activity.onGatewayRedirect(testUri);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).setResult(eq(Activity.RESULT_OK), captor.capture());
        verify(activity).finish();

        Intent captured = captor.getValue();
        assertNotNull(captured);
        // if missing, getQueryParam returns null → complete(key, null)
        assertEquals(null, captured.getStringExtra(BaseGatewayPaymentActivity.EXTRA_GATEWAY_RESULT));
    }
}