/*
* JBoss, Home of Professional Open Source.
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.jacorb;

import java.io.IOException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.ModelDescriptionValidator.ValidationConfiguration;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class JacORBSubsystemTestCase extends AbstractSubsystemBaseTest {

    public JacORBSubsystemTestCase() {
        super(JacORBExtension.SUBSYSTEM_NAME, new JacORBExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        //This is copied from standalone-preview.xml  you may want to try more combinations
        return
        "<subsystem xmlns=\"urn:jboss:domain:jacorb:1.0\">" +
        "    <orb name=\"JBoss\" print-version=\"off\" giop-minor-version=\"2\">" +
        "        <connection max-managed-buf-size=\"24\" outbuf-cache-timeout=\"-1\"/>" +
        "        <naming root-context=\"JBoss/Naming/root\" export-corbaloc=\"on\"/>" +
        "    </orb>" +
        "    <poa monitoring=\"off\" queue-wait=\"off\">" +
        "        <request-processors pool-size=\"2\" max-threads=\"8\"/>" +
        "    </poa>" +
        "    <interop sun=\"on\" chunk-custom-rmi-valuetypes=\"on\" strict-check-on-tc-creation=\"off\"/>" +
        "</subsystem>";
    }

    protected AdditionalInitialization createAdditionalInitialization() {
        return new AdditionalInitialization(){
            @Override
            protected OperationContext.Type getType() {
                return OperationContext.Type.MANAGEMENT;
            }

            @Override
            protected boolean isValidateOperations() {
                //TODO fix providers https://issues.jboss.org/browse/AS7-1791
                return false;
            }

            @Override
            protected ValidationConfiguration getModelValidationConfiguration() {
                //TODO fix providers https://issues.jboss.org/browse/AS7-1791
                return null;
            }
        };
    }
}
