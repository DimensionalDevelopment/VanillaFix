package org.dimdev.vanillafix.crashes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class StacktraceDeobfuscator {
    private static final boolean DEBUG_IN_DEV = false; // Makes this MCP -> SRG for testing in dev. Don't forget to set to false when done!
    private static HashMap<String, String> srgMcpMethodMap = null;

    /**
     * If the file does not exits, downloads latest method mappings and saves them to it.
     * Initializes a HashMap between obfuscated and deobfuscated names from that file.
     */
    public static void init(File mappings, String version) {
        if (srgMcpMethodMap != null) return;

        // Download the file if necessary
        if (!mappings.exists()) {
            HttpURLConnection connection = null;
            try {
                URL mappingsURL = new URL("http://export.mcpbot.bspk.rs/mcp_snapshot_nodoc/" + version + "/mcp_snapshot_nodoc-" + version + ".zip");
                connection = (HttpURLConnection) mappingsURL.openConnection();
                connection.setDoInput(true);
                connection.connect();
                try (InputStream inputStream = connection.getInputStream()) {
                    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                    ZipEntry entry;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        if (entry.getName().equals("methods.csv")) {
                            try (FileOutputStream out = new FileOutputStream(mappings)) {
                                byte[] buffer = new byte[2048];
                                int len;
                                while ((len = zipInputStream.read(buffer)) > 0) {
                                    out.write(buffer, 0, len);
                                }
                            }
                            break;
                        }
                    }
                    if (entry == null) {
                        throw new RuntimeException("Downloaded zip did not contain methods.csv");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }

        // Read the mapping
        HashMap<String, String> srgMcpMethodMap = new HashMap<>();
        try (Scanner scanner = new Scanner(mappings)) {
            scanner.nextLine(); // Skip CSV header
            while (scanner.hasNext()) {
                String mappingLine = scanner.nextLine();
                int commaIndex = mappingLine.indexOf(',');
                String srgName = mappingLine.substring(0, commaIndex);
                String mcpName = mappingLine.substring(commaIndex + 1, commaIndex + 1 + mappingLine.substring(commaIndex + 1).indexOf(','));

                //System.out.println(srgName + " <=> " + mcpName);
                if (!DEBUG_IN_DEV) {
                    srgMcpMethodMap.put(srgName, mcpName);
                } else {
                    srgMcpMethodMap.put(mcpName, srgName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the map only if it's successful, to make sure that it's complete
        StacktraceDeobfuscator.srgMcpMethodMap = srgMcpMethodMap;
    }

    public static void deobfuscateThrowable(Throwable t) {
        Deque<Throwable> queue = new ArrayDeque<>();
        queue.add(t);
        while (!queue.isEmpty()) {
            t = queue.remove();
            t.setStackTrace(deobfuscateStacktrace(t.getStackTrace()));
            if (t.getCause() != null) queue.add(t.getCause());
            Collections.addAll(queue, t.getSuppressed());
        }
    }

    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace) {
        int index = 0;
        for (StackTraceElement el : stackTrace) {
            stackTrace[index++] = new StackTraceElement(el.getClassName(), deobfuscateMethodName(el.getMethodName()), el.getFileName(), el.getLineNumber());
        }
        return stackTrace;
    }

    public static String deobfuscateMethodName(String srgName) {
        if (srgMcpMethodMap == null) {
            return srgName; // Not initialized
        }

        String mcpName = srgMcpMethodMap.get(srgName);
        // log.debug(srgName + " <=> " + mcpName != null ? mcpName : "?"); // Can't do this, it would be a recursive call to log appender
        return mcpName != null ? mcpName : srgName;
    }

    public static void main(String[] args) {
        init(new File("methods.csv"), "20180519-1.12");
        for (Map.Entry<String, String> entry : srgMcpMethodMap.entrySet()) {
            System.out.println(entry.getKey() + " <=> " + entry.getValue());
        }
    }
}
