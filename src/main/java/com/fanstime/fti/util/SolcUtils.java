package com.fanstime.fti.util;

import com.fanstime.fti.solidity.compiler.SolidityCompiler;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Bynum Williams on 20.01.18.
 */
public class SolcUtils {

    public static String getSolcVersion() {
        try {
            // optimistic parsing of version string
            final String versionOutput = SolidityCompiler.runGetVersionOutput();
            final Matcher matcher = Pattern.compile("(\\d+.\\d+.\\d+)").matcher(versionOutput);
            matcher.find();
            return matcher.group(0);
        } catch (Exception e) {
            LoggerFactory.getLogger("general").error("Problem reading solidity version", e);
            return null;
        }
    }
}
