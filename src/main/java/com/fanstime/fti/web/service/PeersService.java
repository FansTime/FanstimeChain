package com.fanstime.fti.web.service;

import com.fanstime.fti.config.WebEnabledCondition;
import com.fanstime.fti.facade.Fti;
import com.fanstime.fti.core.connect.rlpx.discover.NodeStatistics;
import com.fanstime.fti.web.model.dto.PeerDTO;
import com.maxmind.geoip.Country;
import com.maxmind.geoip.LookupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

/**
 * Service for rendering list of peers and geographic activity in fti network.
 *
 * Created by Bynum Williams on 25.07.16.
 */
@Service
@Slf4j(topic = "fanstime")
@Conditional(WebEnabledCondition.class)
public class PeersService {

    private Optional<LookupService> lookupService = Optional.empty();

    private final Map<String, Locale> localeMap = new HashMap<>();


    @Autowired
    private ClientMessageService clientMessageService;

    @Autowired
    private Fti fti;

    @Autowired
    private Environment env;


//    @PostConstruct
//    private void postConstruct() {
//        // gather blocks to calculate hash rate
//        fti.addListener(new FtiListenerAdapter() {
//            @Override
//            public void onRecvMessage(Channel channel, Message message) {
//                // notify client about new block
//                // using PeerDTO as it already has both country fields
//                if (message.getCommand() == FtiMessageCodes.NEW_BLOCK) {
//                    clientMessageService.sendToTopic("/topic/newBlockFrom", createPeerDTO(
//                            channel.getPeerId(),
//                            channel.getInetSocketAddress().getHostName(),
//                            0l, 0.0,
//                            0,
//                            true,
//                            null,
//                            Optional.of(channel.getFtiHandler().getBestKnownBlock())
//                                    .map(b -> b.getNumber())
//                                    .orElse(0L)
//                    ));
//                }
//            }
//        });
//
//        createGeoDatabase();
//    }


    private String getPeerDetails(NodeStatistics nodeStatistics, String country, long maxBlockNumber) {
        final String countryRow = "Country: " + country;

        if (nodeStatistics == null || nodeStatistics.getClientId() == null) {
            return countryRow;
        }

        final String delimiter = "\n";
        final String blockNumber = "Block number: #" + NumberFormat.getNumberInstance(Locale.US).format(maxBlockNumber);
        final String clientId = StringUtils.trimWhitespace(nodeStatistics.getClientId());
        final String details = "Details: " + clientId;
//        final String supports = "Supported protocols: " + nodeStatistics.capabilities
//                .stream()
//                .filter(c -> c != null)
//                .map(c -> StringUtils.capitalize(c.getName()) + ": " + c.getVersion())
//                .collect(joining(", "));

        final String[] array = clientId.split("/");
        if (array.length >= 4) {
            final String type = "Type: " + array[0];
            final String os = "OS: " + StringUtils.capitalize(array[2]);
            final String version = "Version: " + array[3];

            return String.join(delimiter, type, os, version, countryRow, "", details, "", blockNumber);
        } else {
            return String.join(delimiter, countryRow, details, "", blockNumber);
        }
    }

    private PeerDTO createPeerDTO(String peerId, String ip, long lastPing, double avgLatency, int reputation,
                                  boolean isActive, NodeStatistics nodeStatistics, long maxBlockNumber) {
        // code or ""

        final Optional<Country> country = lookupService.map(service -> service.getCountry(ip));
        final String country2Code = country
                .map(c -> c.getCode())
                .orElse("");

        // code or ""
        final String country3Code = iso2CountryCodeToIso3CountryCode(country2Code);

        return new PeerDTO(
                peerId,
                ip,
                country3Code,
                country2Code,
                lastPing,
                avgLatency,
                reputation,
                isActive,
                getPeerDetails(nodeStatistics, country.map(Country::getName).orElse("Unknown location"), maxBlockNumber));
    }

    /**
     * Create MaxMind lookup service to find country by IP.
     * IPv6 is not used.
     */
    private void createGeoDatabase() {
        final String[] countries = Locale.getISOCountries();
        final Optional<String> dbFilePath = Optional.ofNullable(env.getProperty("maxmind.file"));

        for (String country : countries) {
            Locale locale = new Locale("", country);
            localeMap.put(locale.getISO3Country().toUpperCase(), locale);
        }
        lookupService = dbFilePath
                .flatMap(path -> {
                    try {
                        return Optional.ofNullable(new LookupService(
                                path,
                                LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE));
                    } catch(IOException e) {
                        log.error("Problem finding maxmind database at " + path + ". " + e.getMessage());
                        log.error("Wasn't able to create maxmind location service. Country information will not be available.");
                        return Optional.empty();
                    }
                });
    }

    private String iso2CountryCodeToIso3CountryCode(String iso2CountryCode){
        Locale locale = new Locale("", iso2CountryCode);
        try {
            return locale.getISO3Country();
        } catch (MissingResourceException e) {
            // silent
        }
        return "";
    }

}
