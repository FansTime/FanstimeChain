package com.fanstime.fti.web.controller;

import com.fanstime.fti.web.contrdata.storage.StorageEntry;
import com.fanstime.fti.config.WebEnabledCondition;
import com.fanstime.fti.web.model.dto.ActionStatus;
import com.fanstime.fti.web.model.dto.ContractObjects.*;
import com.fanstime.fti.web.service.contracts.ContractsService;
import com.fanstime.fti.web.service.DisabledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.fanstime.fti.web.model.dto.ActionStatus.createErrorStatus;
import static com.fanstime.fti.web.model.dto.ActionStatus.createSuccessStatus;
import static org.apache.commons.lang3.StringUtils.lowerCase;

/**
 * Created by Bynum Williams on 18.09.18.
 */
@Slf4j
@RestController
@RequestMapping("/contracts")
@Conditional(WebEnabledCondition.class)
public class ContractsController {

    @Autowired
    ContractsService contractsService;

    @RequestMapping("/{address}/storage")
    public Page<StorageEntry> getContractStorage(@PathVariable String address,
                                                 @RequestParam(required = false) String path,
                                                 @RequestParam(required = false, defaultValue = "0") int page,
                                                 @RequestParam(required = false, defaultValue = "5") int size) {
        return contractsService.getContractStorage(address, path, new PageRequest(page, size));
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ActionStatus<ContractInfoDTO> addContractSources(@RequestBody WatchContractDTO watchContract) {
        try {
            ContractInfoDTO contract = contractsService.addContract(watchContract.address, watchContract.sourceCode);
            return createSuccessStatus(contract);
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Contract's source uploading error: ", e);
            return createErrorStatus(e.getMessage());
        }
    }

    @RequestMapping("/list")
    public List<ContractInfoDTO> getContracts() {
        return contractsService.getContracts();
    }

    @RequestMapping(value = "/{address}/delete", method = RequestMethod.POST)
    public boolean stopWatchingContract(@PathVariable String address) {
        return contractsService.deleteContract(address);
    }

    @RequestMapping(value = "/{address}/files", method = RequestMethod.POST)
    public ActionStatus<ContractInfoDTO> uploadContractFiles(
            @PathVariable String address,
            @RequestParam MultipartFile[] contracts,
            @RequestParam(required = false) String verifyRlp) {

        try {
            ContractInfoDTO contract = contractsService.uploadContract(lowerCase(address), contracts);
            log.info("Uploaded files for address: {}, contract name: {}" + address, contract.getName());
            return createSuccessStatus(contract);
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Contract's source uploading error: ", e);
            return createErrorStatus(e.getMessage());
        }
    }

    @RequestMapping(value = "/{address}/importFromExplorer", method = RequestMethod.POST)
    public ActionStatus<Boolean> importContractDataFromExplorer(@PathVariable String address) {
        try {
            final boolean result = contractsService.importContractFromExplorer(address);
            return createSuccessStatus(result);
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            return createErrorStatus(e.getMessage());
        }
    }

    @RequestMapping(value = "/{address}/clearContractStorage", method = RequestMethod.POST)
    public ActionStatus<Boolean> clearStorage(@PathVariable String address) {
        try {
            contractsService.clearContractStorage(address);
            return createSuccessStatus();
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            return createErrorStatus(e.getMessage());
        }
    }

    @RequestMapping("/indexStatus")
    public ActionStatus<IndexStatusDTO> getIndexStatus() {
        try {
            IndexStatusDTO result = contractsService.getIndexStatus();
            return createSuccessStatus(result);
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Contract's index status error: ", e);
            return createErrorStatus(e.getMessage());
        }
    }

    // Convert a predefined exception to an HTTP Status code
    @ResponseStatus(value=HttpStatus.GONE, reason="Service is disabled")  // 410
    @ExceptionHandler(DisabledException.class)
    public void disabled() {
        // Nothing to do
    }



    private static class WatchContractDTO {

        public String address;

        public String sourceCode;

    }
}
