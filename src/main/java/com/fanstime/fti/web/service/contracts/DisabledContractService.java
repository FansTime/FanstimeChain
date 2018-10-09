package com.fanstime.fti.web.service.contracts;

import com.fanstime.fti.web.contrdata.storage.StorageEntry;
import com.fanstime.fti.web.model.dto.ContractObjects;
import com.fanstime.fti.web.service.DisabledException;
import lombok.extern.slf4j.Slf4j;
import com.fanstime.fti.datasource.DbSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j(topic = "contracts")
public class DisabledContractService implements ContractsService {

    private final static String DISABLED_MSG = "Contracts service is disabled";

    public DisabledContractService(final DbSource<byte[]> settingsStorage) {
        log.info("Contract service is disabled");
        if (ContractsServiceImpl.isContractStorageCreated(settingsStorage)) {
            log.error("Contracts service was enabled before. If you are going to disable it now, " +
                    "your contract storage data will be corrupted due to disabled tracking of new blocks.");
            log.error("Either enable contract storage or remove following directories from DB folder: " +
                    "settings, contractsStorage, contractCreation, storageDict");
            log.error("Exiting...");
            System.exit(141);
        }
    }

    @Override
    public boolean deleteContract(String address) {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public ContractObjects.ContractInfoDTO addContract(String address, String src) throws Exception {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public List<ContractObjects.ContractInfoDTO> getContracts() {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public ContractObjects.ContractInfoDTO uploadContract(String address, MultipartFile[] files) throws Exception {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public ContractObjects.IndexStatusDTO getIndexStatus() throws Exception {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public Page<StorageEntry> getContractStorage(String hexAddress, String path, Pageable pageable) {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public boolean importContractFromExplorer(String hexAddress) throws Exception {
        throw new DisabledException(DISABLED_MSG);
    }

    @Override
    public void clearContractStorage(String hexAddress) throws Exception {
        throw new DisabledException(DISABLED_MSG);
    }
}
