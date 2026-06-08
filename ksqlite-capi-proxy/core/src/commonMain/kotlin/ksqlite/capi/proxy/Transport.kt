package ksqlite.capi.proxy

public interface Writer {

    public fun write(bytes: ByteArray)

    public fun writeByte(value: Byte)

    public fun writeShort(value: Short)

    public fun writeInt(value: Int)

    public fun writeLong(value: Long)

    public fun writeFloat(value: Float)

    public fun writeDouble(value: Double)
}

public interface Reader {

    public fun read(byteCount: Int)

    public fun readByte(): Byte

    public fun readShort(): Short

    public fun readInt(): Int

    public fun readLong(): Long

    public fun readFloat(): Float

    public fun readDouble(): Double
}

public interface Transport {

    public suspend fun transmit(block: Writer.() -> Unit)

    public suspend fun receive(block: Reader.() -> Unit)
}