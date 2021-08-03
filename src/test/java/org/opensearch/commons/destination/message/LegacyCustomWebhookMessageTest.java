/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.commons.destination.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.StreamInput;

public class LegacyCustomWebhookMessageTest {

    @Test
    public void testBuildingLegacyCustomWebhookMessage() {
        LegacyCustomWebhookMessage message = new LegacyCustomWebhookMessage.Builder("custom_webhook")
                .withMessage("Hello world").withUrl("https://amazon.com").build();

        assertEquals("custom_webhook", message.destinationName);
        assertEquals(LegacyDestinationType.CUSTOMWEBHOOK, message.destinationType);
        assertEquals("Hello world", message.getMessageContent());
        assertEquals("https://amazon.com", message.getUrl());
    }

    @Test
    public void testRoundTrippingLegacyCustomWebhookMessageWithUrl() throws IOException {
        LegacyCustomWebhookMessage message = new LegacyCustomWebhookMessage.Builder("custom_webhook")
                .withMessage("Hello world").withUrl("https://amazon.com").build();
        BytesStreamOutput out = new BytesStreamOutput();
        message.writeTo(out);

        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        LegacyCustomWebhookMessage newMessage = new LegacyCustomWebhookMessage(in);

        assertEquals(newMessage.destinationName, message.destinationName);
        assertEquals(newMessage.destinationType, message.destinationType);
        assertEquals(newMessage.getMessageContent(), message.getMessageContent());
        assertEquals(newMessage.getUrl(), message.getUrl());
    }

    @Test
    public void testRoundTrippingLegacyCustomWebhookMessageWithHost() throws IOException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("token", "sometoken");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-token", "sometoken");
        LegacyCustomWebhookMessage message = new LegacyCustomWebhookMessage.Builder("custom_webhook")
                .withMessage("Hello world").withHost("hooks.chime.aws").withPath("incomingwebhooks/abc")
                .withMethod(HttpPost.METHOD_NAME).withQueryParams(queryParams).withHeaderParams(headers)
                .withPort(8000).withScheme("https").build();
        BytesStreamOutput out = new BytesStreamOutput();
        message.writeTo(out);

        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        LegacyCustomWebhookMessage newMessage = new LegacyCustomWebhookMessage(in);

        assertEquals(newMessage.destinationName, message.destinationName);
        assertEquals(newMessage.destinationType, message.destinationType);
        assertEquals(newMessage.getMessageContent(), message.getMessageContent());
        assertEquals(newMessage.getHost(), message.getHost());
        assertEquals(newMessage.getMethod(), message.getMethod());
        assertEquals(newMessage.getPath(), message.getPath());
        assertEquals(newMessage.getQueryParams(), message.getQueryParams());
        assertEquals(newMessage.getHeaderParams(), message.getHeaderParams());
        assertEquals(newMessage.getPort(), message.getPort());
        assertEquals(newMessage.getScheme(), message.getScheme());
    }

    @Test
    public void testContentMissingMessage() {
        try {
            new LegacyCustomWebhookMessage.Builder("custom_webhook")
                    .withUrl("https://amazon.com").build();
            fail("Building legacy custom webhook message without message should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Message content is missing", e.getMessage());
        }
    }

    @Test
    public void testMissingDestinationName() {
        try {
            new LegacyCustomWebhookMessage.Builder(null)
                    .withMessage("Hello world").withUrl("https://amazon.com").build();
            fail("Building legacy custom webhook message with null destination name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Channel name must be defined", e.getMessage());
        }
    }

    @Test
    public void testUnsupportedHttpMethods() {
        try {
            new LegacyCustomWebhookMessage.Builder("custom_webhook")
                    .withMessage("Hello world").withUrl("https://amazon.com").withMethod(HttpGet.METHOD_NAME).build();
            fail("Building legacy custom webhook message with unsupported http methods should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid method supplied. Only POST, PUT and PATCH are allowed", e.getMessage());
        }
    }

    @Test
    public void testURLandHostNameMissingOrEmpty() {
        try {
            new LegacyCustomWebhookMessage.Builder("custom_webhook")
                    .withMessage("Hello world").withMethod(HttpGet.METHOD_NAME).build();
            fail("Building legacy custom webhook message missing or empty url and host name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Either fully qualified URL or host name should be provided", e.getMessage());
        }

        try {
            new LegacyCustomWebhookMessage.Builder("custom_webhook")
                    .withMessage("Hello world").withUrl("").withMethod(HttpGet.METHOD_NAME).build();
            fail("Building legacy custom webhook message with missing or empty url and host name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Either fully qualified URL or host name should be provided", e.getMessage());
        }

        try {
            new LegacyCustomWebhookMessage.Builder("custom_webhook")
                    .withMessage("Hello world").withHost("").withMethod(HttpGet.METHOD_NAME).build();
            fail("Building legacy custom webhook message with missing or empty url and host name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Either fully qualified URL or host name should be provided", e.getMessage());
        }

        try {
            new LegacyCustomWebhookMessage.Builder("custom_webhook")
                    .withMessage("Hello world").withUrl("").withHost("").withMethod(HttpGet.METHOD_NAME).build();
            fail("Building legacy custom webhook message with missing or empty url and host name should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Either fully qualified URL or host name should be provided", e.getMessage());
        }
    }
}
