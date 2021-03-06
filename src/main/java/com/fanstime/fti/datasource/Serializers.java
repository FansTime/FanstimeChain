package com.fanstime.fti.datasource;

import com.fanstime.fti.core.AccountState;
import com.fanstime.fti.core.BlockHeader;
import com.fanstime.fti.util.RLP;
import com.fanstime.fti.util.Value;
import com.fanstime.fti.vm.DataWord;

/**
 * Collection of common Serializers
 * Created by Jay Nicolas on 08.04.2018.
 */
public class Serializers {

    /**
     *  No conversion
     */
    public static class Identity<T> implements Serializer<T, T> {
        @Override
        public T serialize(T object) {
            return object;
        }
        @Override
        public T deserialize(T stream) {
            return stream;
        }
    }

    /**
     * Serializes/Deserializes AccountState instances from the State Trie (part of Fti spec)
     */
    public final static Serializer<AccountState, byte[]> AccountStateSerializer = new Serializer<AccountState, byte[]>() {
        @Override
        public byte[] serialize(AccountState object) {
            return object.getEncoded();
        }

        @Override
        public AccountState deserialize(byte[] stream) {
            return stream == null || stream.length == 0 ? null : new AccountState(stream);
        }
    };

    /**
     * Contract storage key serializer
     */
    public final static Serializer<DataWord, byte[]> StorageKeySerializer = new Serializer<DataWord, byte[]>() {
        @Override
        public byte[] serialize(DataWord object) {
            return object.getData();
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            return new DataWord(stream);
        }
    };

    /**
     * Contract storage value serializer (part of Fti spec)
     */
    public final static Serializer<DataWord, byte[]> StorageValueSerializer = new Serializer<DataWord, byte[]>() {
        @Override
        public byte[] serialize(DataWord object) {
            return RLP.encodeElement(object.getNoLeadZeroesData());
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            if (stream == null || stream.length == 0) return null;
            byte[] dataDecoded = RLP.decode2(stream).get(0).getRLPData();
            return new DataWord(dataDecoded);
        }
    };

    /**
     * Trie node serializer (part of Fti spec)
     */
    public final static Serializer<Value, byte[]> TrieNodeSerializer = new Serializer<Value, byte[]>() {
        @Override
        public byte[] serialize(Value object) {
            return object.asBytes();
        }

        @Override
        public Value deserialize(byte[] stream) {
            return new Value(stream);
        }
    };

    /**
     * Trie node serializer (part of Fti spec)
     */
    public final static Serializer<BlockHeader, byte[]> BlockHeaderSerializer = new Serializer<BlockHeader, byte[]>() {
        @Override
        public byte[] serialize(BlockHeader object) {
            return object == null ? null : object.getEncoded();
        }

        @Override
        public BlockHeader deserialize(byte[] stream) {
            return stream == null ? null : new BlockHeader(stream);
        }
    };

    /**
     * AS IS serializer (doesn't change anything)
     */
    public final static Serializer<byte[], byte[]> AsIsSerializer = new Serializer<byte[], byte[]>() {
        @Override
        public byte[] serialize(byte[] object) {
            return object;
        }

        @Override
        public byte[] deserialize(byte[] stream) {
            return stream;
        }
    };
}
