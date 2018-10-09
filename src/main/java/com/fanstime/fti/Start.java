package com.fanstime.fti;

import org.apache.commons.lang3.StringUtils;
import com.fanstime.fti.cli.CLIInterface;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.facade.Fti;
import com.fanstime.fti.facade.FtiFactory;
import com.fanstime.fti.mine.Ethash;

import java.io.IOException;
import java.net.URISyntaxException;

public class Start {

    public static void main(String args[]) throws IOException, URISyntaxException {
        CLIInterface.call(args);

        final SystemProperties config = SystemProperties.getDefault();
        final boolean actionBlocksLoader = !config.blocksLoader().isEmpty();
        final boolean actionGenerateDag = !StringUtils.isEmpty(System.getProperty("ethash.blockNumber"));

        if (actionBlocksLoader || actionGenerateDag) {
            config.setSyncEnabled(false);
        }

        if (actionGenerateDag) {
            new Ethash(config, Long.parseLong(System.getProperty("ethash.blockNumber"))).getFullDataset();
            // DAG file has been created, lets exit
            System.exit(0);
        } else {
            Fti fti = FtiFactory.createFti();

            if (actionBlocksLoader) {
                fti.getBlockLoader().loadBlocks();
            }
        }
    }

}
