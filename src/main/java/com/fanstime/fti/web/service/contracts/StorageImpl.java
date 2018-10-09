package com.fanstime.fti.web.service.contracts;

import com.fanstime.fti.web.contrdata.storage.Storage;
import com.fanstime.fti.facade.Repository;
import com.fanstime.fti.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StorageImpl implements Storage {

    @Autowired
    Repository repository;



//    @Autowired
//    public void setRepository(Fti fti) {
//        this.repository = fti.getRepository();
//    }

    @Override
//    @Profiled
    public int size(byte[] address) {
        return repository.getStorageSize(address);
    }

    @Override
//    @Profiled
    public Map<DataWord, DataWord> entries(byte[] address, List<DataWord> keys) {
        return repository.getStorage(address, keys);
    }

    @Override
//    @Profiled
    public Set<DataWord> keys(byte[] address) {
        return repository.getStorageKeys(address);
    }

    @Override
//    @Profiled
    public DataWord get(byte[] address, DataWord key) {
        return repository.getStorageValue(address, key);
    }
}
