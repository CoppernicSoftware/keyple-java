/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.nativese.method;

import java.util.Map;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractSelectionLocalReader;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.INativeReaderService;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

/**
 * Handle the Connect Reader keypleDTO serialization and deserialization
 */
public class RmConnectReaderTx extends RemoteMethodTx<String> {


    private final SeReader localReader;
    private final INativeReaderService slaveAPI;
    private final Map<String, String> options;

    @Override
    public RemoteMethod getMethodName() {
        return RemoteMethod.READER_CONNECT;
    }


    public RmConnectReaderTx(String sessionId, String nativeReaderName, String virtualReaderName,
            String masterNodeId, SeReader localReader, String slaveNodeId,
            INativeReaderService slaveAPI, Map<String, String> options) {
        super(sessionId, nativeReaderName, virtualReaderName, masterNodeId, slaveNodeId);
        this.localReader = localReader;
        this.slaveAPI = slaveAPI;
        this.options = options;
    }

    private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderTx.class);

    @Override
    public String parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        String nativeReaderName = keypleDto.getNativeReaderName();

        // if reader connection thrown an exception
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method connectReader", ex);
        } else {
            // if dto does not contain an exception
            try {
                /*
                 * configure slaveAPI to propagate reader events if the reader is observable
                 */

                // find the local reader by name
                ProxyReader localReader = (ProxyReader) slaveAPI.findLocalReader(nativeReaderName);

                if (localReader instanceof AbstractSelectionLocalReader) {
                    logger.debug("Register SlaveAPI as an observer for native reader {}",
                            localReader.getName());
                    ((AbstractSelectionLocalReader) localReader)
                            .addObserver((ObservableReader.ReaderObserver) slaveAPI);
                }

                // retrieve sessionId from keypleDto
                JsonObject body =
                        JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);

                // sessionId is returned here
                return body.get("sessionId").getAsString();

            } catch (KeypleReaderNotFoundException e) {
                logger.warn(
                        "While receiving a confirmation of Rse connection, local reader was not found");
                throw new KeypleRemoteException(
                        "While receiving a confirmation of Rse connection, local reader was not found");
            }
        }
    }

    @Override
    public KeypleDto dto() {

        // create response
        JsonObject body = new JsonObject();
        body.addProperty("transmissionMode", localReader.getTransmissionMode().name());
        body.addProperty("options", JsonParser.getGson().toJson(options));

        return KeypleDtoHelper.buildRequest(getMethodName().getName(), body.toString(), null,
                localReader.getName(), null, requesterNodeId, targetNodeId, id);
    }
}
