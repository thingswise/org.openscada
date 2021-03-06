/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.openscada.protocol.iec60870.asdu.message;

import org.openscada.protocol.iec60870.ProtocolOptions;
import org.openscada.protocol.iec60870.asdu.ASDUHeader;
import org.openscada.protocol.iec60870.asdu.types.CommandValue;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.asdu.types.TypeHelper;

import io.netty.buffer.ByteBuf;

public abstract class AbstractSetPointCommandNormalizedValue extends AbstractInformationObjectMessage
{
    private final byte type;

    private final boolean execute;

    private final CommandValue<Double> value;

    private final boolean withTimestamp;

    public AbstractSetPointCommandNormalizedValue ( final ASDUHeader header, final InformationObjectAddress informationObjectAddress, final CommandValue<Double> value, boolean withTimestamp, final byte type, final boolean execute )
    {
        super ( header, informationObjectAddress );
        if ( value.getValue () < TypeHelper.MIN_NORMALIZED_VALUE || value.getValue () > TypeHelper.MAX_NORMALIZED_VALUE )
        {
            throw new IllegalArgumentException ( "value '" + value + "' is out of range!" );
        }
        this.value = value;
        this.type = type;
        this.execute = execute;
        this.withTimestamp = withTimestamp;
    }

    public byte getType ()
    {
        return this.type;
    }

    public CommandValue<Double> getValue ()
    {
        return this.value;
    }

    public boolean isExecute ()
    {
        return this.execute;
    }

    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, null, this.header, out );

        this.informationObjectAddress.encode ( options, out );

        out.writeShort ( (short) ( this.value.getValue () * 32768 ) );

        byte b = 0;

        b |= this.type & 0b011111111;
        b |= this.execute ? 0 : 0b100000000;

        out.writeByte ( b );

        if ( withTimestamp )
        {
            TypeHelper.encodeTimestamp ( options, out, value.getTimestamp () );
        }
    }
}
