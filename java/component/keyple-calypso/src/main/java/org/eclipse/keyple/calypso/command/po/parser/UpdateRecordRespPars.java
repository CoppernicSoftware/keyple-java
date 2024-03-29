/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Update Record response parser. See specs: Calypso / page 96 / 9.4.11 - Update Record
 */
public final class UpdateRecordRespPars extends AbstractPoResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6400, new StatusProperties(false, "Too many modifications in session"));
        m.put(0x6700, new StatusProperties(false, "Lc value not supported"));
        m.put(0x6981, new StatusProperties(false,
                "Command forbidden on cyclic files when the record exists and is not record 01h and on binary files"));
        m.put(0x6982, new StatusProperties(false,
                "Security conditions not fulfilled (no session, wrong key, encryption required)"));
        m.put(0x6985, new StatusProperties(false,
                "Access forbidden (Never access mode, DF is invalidated, etc..)"));
        m.put(0x6986, new StatusProperties(false, "Command not allowed (no current EF)"));
        m.put(0x6A82, new StatusProperties(false, "File not found"));
        m.put(0x6A83, new StatusProperties(false,
                "Record is not found (record index is 0 or above NumRec)"));
        m.put(0x6B00, new StatusProperties(false, "P2 value not supported"));
        m.put(0x9000, new StatusProperties(true, "Successful execution"));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * Instantiates a new UpdateRecordRespPars.
     */
    public UpdateRecordRespPars(ApduResponse response) {
        super(response);
    }
}
