package com.fanstime.fti.web.contrdata.storage.dictionary;

import lombok.extern.slf4j.Slf4j;
import com.fanstime.fti.db.ByteArrayWrapper;
import com.fanstime.fti.vm.DataWord;
import com.fanstime.fti.vm.OpCode;
import com.fanstime.fti.vm.VM;
import com.fanstime.fti.vm.VMHook;
import com.fanstime.fti.vm.program.Program;
import com.fanstime.fti.vm.program.Stack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.stream.Collectors.toMap;
import static com.fanstime.fti.util.ByteUtil.toHexString;

@Slf4j
@Component
public class StorageDictionaryVmHook implements VMHook {

    @Autowired
    private StorageDictionaryDb dictionaryDb;
    @Autowired
    private List<Layout.DictPathResolver> pathResolvers;
    private java.util.Stack<StorageKeys> storageKeysStack = new java.util.Stack<>();
    private java.util.Stack<Sha3Index> sha3IndexStack = new java.util.Stack<>();

    @PostConstruct
    public void initVmHook() {
        VM.setVmHook(this);
    }

    private byte[] getContractAddress(Program program) {
        return program.getOwnerAddress().getLast20Bytes();
    }

    @Override
    public void startPlay(Program program) {
        try {
            storageKeysStack.push(new StorageKeys());
            sha3IndexStack.push(new Sha3Index());
        } catch (Throwable e) {
            log.error("Error within handler: ", e);
        }
    }

    @Override
    public void step(Program program, OpCode opcode) {
        try {
            Stack stack = program.getStack();
            switch (opcode) {
                case SSTORE:
                    DataWord key = stack.get(stack.size() - 1);
                    DataWord value = stack.get(stack.size() - 2);

                    storageKeysStack.peek().add(key, value);
                    break;
                case SHA3:
                    DataWord offset = stack.get(stack.size() - 1);
                    DataWord size = stack.get(stack.size() - 2);
                    byte[] input = program.memoryChunk(offset.intValue(), size.intValue());

                    sha3IndexStack.peek().add(input);
                    break;
            }
        } catch (Throwable e) {
            log.error("Error within handler: ", e);
        }
    }

    @Override
    public void stopPlay(Program program) {
        try {
            final byte[] address = getContractAddress(program);

            final StorageKeys storageKeys = storageKeysStack.pop();
            final Sha3Index sha3Index = sha3IndexStack.pop();

            final Map<Layout.Lang, StorageDictionary> dictByLang = pathResolvers.stream()
                    .collect(toMap(Layout.DictPathResolver::getLang, r -> dictionaryDb.getDictionaryFor(r.getLang(), address)));

            storageKeys.forEach((key, removed) -> {
                pathResolvers.forEach(resolver -> {
                    StorageDictionary.PathElement[] path = resolver.resolvePath(key.getData(), sha3Index);
                    StorageDictionary dictionary = dictByLang.get(resolver.getLang());
                    dictionary.addPath(path);
                });
            });

            dictByLang.values().forEach(StorageDictionary::store);

            if (storageKeysStack.isEmpty()) {
                dictionaryDb.flush();
            }
        } catch (Throwable e) {
            log.error("Error within handler address[" + toHexString(getContractAddress(program)) + "]: ", e);
        }
    }

    private static class StorageKeys {

        private static final DataWord REMOVED_VALUE = new DataWord(0);

        private final Map<ByteArrayWrapper, Boolean> keys = new HashMap<>();

        public void add(DataWord key, DataWord value) {
            keys.put(new ByteArrayWrapper(key.clone().getData()), isRemoved(value));
        }

        public void forEach(BiConsumer<? super ByteArrayWrapper, ? super Boolean> action) {
            keys.forEach(action);
        }

        private Boolean isRemoved(DataWord value) {
            return REMOVED_VALUE.equals(value);
        }
    }
}
