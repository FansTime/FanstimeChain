package com.fanstime.fti.web.contrdata.config;

import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.datasource.DbSource;
import com.fanstime.fti.datasource.leveldb.LevelDbDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.fanstime.fti.web.contrdata")
public class ContractDataConfig {

    @Bean
    public SystemProperties systemProperties() {
        return SystemProperties.getDefault();
    }

    @Bean
    public DbSource<byte[]> storageDict() {
        DbSource<byte[]> dataSource = new LevelDbDataSource("storageDict");
        dataSource.init();
        return dataSource;
    }
}
