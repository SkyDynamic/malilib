package fi.dy.masa.malilib.network.payload;

/**
 * Foolproof method for listing available Payload Types.
 * All types not listed here are rejected.
 */
public enum PayloadType
{
    // TODO Add to this list if you wish to create more Payloads
    CARPET_HELLO,
    MALILIB_BYTEBUF,
    SERVUX_BLOCKS,
    SERVUX_ENTITIES,
    SERVUX_LITEMATICS,
    SERVUX_METADATA,
    SERVUX_STRUCTURES;

    public boolean exists(PayloadType type)
    {
        for (final PayloadType p : PayloadType.values())
        {
            if (p == type)
                return true;
        }

        return false;
    }
}
