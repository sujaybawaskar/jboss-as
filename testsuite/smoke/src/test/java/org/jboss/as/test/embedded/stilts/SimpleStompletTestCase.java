/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.test.embedded.stilts;

import static org.jboss.as.test.embedded.stilts.bundle.SimpleStomplet.DESTINATION_QUEUE_ONE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.embedded.stilts.bundle.SimpleStomplet;
import org.jboss.as.test.embedded.stilts.bundle.SimpleStompletActivator;
import org.jboss.logging.Logger;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.startlevel.StartLevel;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.StompMessages;
import org.projectodd.stilts.stomp.client.ClientSubscription;
import org.projectodd.stilts.stomp.client.ClientTransaction;
import org.projectodd.stilts.stomp.client.MessageHandler;
import org.projectodd.stilts.stomp.client.StompClient;
import org.projectodd.stilts.stomp.client.SubscriptionBuilder;
import org.projectodd.stilts.stomp.spi.StompSession;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.simple.SimpleSubscribableStomplet;

/**
 * A simple {@link Stomplet} test case.
 *
 * @author thomas.diesler@jboss.com
 * @since 09-Sep-2010
 */
@RunAsClient
@RunWith(Arquillian.class)
public class SimpleStompletTestCase {

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-stomplet");
        archive.addClasses(SimpleStompletActivator.class, SimpleStomplet.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(SimpleStompletActivator.class);
                builder.addImportPackages(StompMessage.class, StompSession.class, Stomplet.class, SimpleSubscribableStomplet.class);
                builder.addImportPackages(BundleContext.class, StartLevel.class, Logger.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testSendWithNoTx() throws Exception {

        StompClient client = new StompClient("stomp://localhost");
        client.connect();

        final List<String> outbound = new ArrayList<String>();
        final CountDownLatch outboundLatch = new CountDownLatch(2);
        SubscriptionBuilder builder = client.subscribe(DESTINATION_QUEUE_ONE);
        builder.withMessageHandler(new MessageHandler() {
            public void handle(StompMessage message) {
                String content = message.getContentAsString();
                outbound.add(content);
                outboundLatch.countDown();
            }
        });
        ClientSubscription subscription = builder.start();

        client.send(StompMessages.createStompMessage(DESTINATION_QUEUE_ONE, "msg1"));
        client.send(StompMessages.createStompMessage(DESTINATION_QUEUE_ONE, "msg2"));

        Assert.assertTrue("No latch timeout", outboundLatch.await(3, TimeUnit.SECONDS));
        Assert.assertEquals("msg1", outbound.get(0));
        Assert.assertEquals("msg2", outbound.get(1));

        subscription.unsubscribe();
        client.disconnect();
    }

    @Test
    public void testSendWithTxCommit() throws Exception {

        StompClient client = new StompClient("stomp://localhost");
        client.connect();

        final List<String> outbound = new ArrayList<String>();
        final CountDownLatch outboundLatch = new CountDownLatch(2);
        SubscriptionBuilder builder = client.subscribe(DESTINATION_QUEUE_ONE);
        builder.withMessageHandler(new MessageHandler() {
            public void handle(StompMessage message) {
                String content = message.getContentAsString();
                outbound.add(content);
                outboundLatch.countDown();
            }
        });
        ClientSubscription subscription = builder.start();

        ClientTransaction tx = client.begin();
        tx.send(StompMessages.createStompMessage(DESTINATION_QUEUE_ONE, "msg1"));
        tx.send(StompMessages.createStompMessage(DESTINATION_QUEUE_ONE, "msg2"));
        tx.commit();

        Assert.assertTrue("No latch timeout", outboundLatch.await(3, TimeUnit.SECONDS));
        Assert.assertEquals("msg1", outbound.get(0));
        Assert.assertEquals("msg2", outbound.get(1));

        subscription.unsubscribe();
        client.disconnect();
    }
}
