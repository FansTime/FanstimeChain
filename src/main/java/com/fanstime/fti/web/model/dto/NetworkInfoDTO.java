package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;
import com.fanstime.fti.facade.SyncStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bynum Williams on 09.08.18.
 */
@Value
@AllArgsConstructor
public class NetworkInfoDTO {

    private final Integer activePeers;

    private final SyncStatusDTO syncStatus;

    private final String mineStatus;

    private final Integer ethPort;

    private final Boolean ethAccessible;

    private final List<MinerDTO> miners = new ArrayList();

    @Value
    @AllArgsConstructor
    public static class SyncStatusDTO {

        private final com.fanstime.fti.facade.SyncStatus.SyncStage stage;
        private final long curCnt;
        private final long knownCnt;
        private final long blockLastImported;
        private final long blockBestKnown;

        public static SyncStatusDTO instanceOf(SyncStatus status) {
            return new SyncStatusDTO(status.getStage(), status.getCurCnt(), status.getKnownCnt(),
                    status.getBlockLastImported(), status.getBlockBestKnown());
        }
    }
}


