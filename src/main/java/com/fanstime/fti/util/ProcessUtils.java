package com.fanstime.fti.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;

@Slf4j(topic = "fanstime")
public class ProcessUtils {

    public static void dumpOpenFiles() {
        try {
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            File dir = Paths.get(System.getProperty("user.dir"), "lsof").toFile();
            if (!dir.exists()) dir.mkdirs();
            try (FileOutputStream out = new FileOutputStream(Paths.get(dir.getAbsolutePath(),
                    "lsof_pid" + pid + "_" + System.currentTimeMillis() / 1000 + ".out").toFile())) {

                byte[] buffer = new byte[1 << 10];
                Process proc = Runtime.getRuntime().exec(new String[]{"lsof", "-p", pid});
                InputStream in = proc.getInputStream();
                while (in.read(buffer) > 0)
                    out.write(buffer);
            }
        } catch (Throwable t) {
            log.error("Failed to dump open files", t);
        }
    }
}
