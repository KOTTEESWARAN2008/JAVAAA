import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class FileServer {
    private static final int TCP_PORT = 5000;
    private static final int WEB_PORT = 8080;
    private static final Path RECEIVED_DIR = Paths.get("received").toAbsolutePath().normalize();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(RECEIVED_DIR);

        Thread tcpThread = new Thread(FileServer::startTcpServer, "file-transfer-server");
        tcpThread.setDaemon(false);
        tcpThread.start();

        startWebServer();
    }

    private static void startTcpServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("File server waiting for clients on port " + TCP_PORT + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("TCP Server Error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (Socket s = socket;
             DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()))) {

            String requestedName = Paths.get(in.readUTF()).getFileName().toString();
            long compressedSize = in.readLong();

            if (compressedSize < 0 || compressedSize > 2_000_000_000L) {
                out.writeBoolean(false);
                out.writeUTF("Invalid file size.");
                out.flush();
                return;
            }

            Path tempGzip = Files.createTempFile("upload-", ".gz");
            try {
                try (OutputStream fileOut = Files.newOutputStream(tempGzip)) {
                    byte[] buffer = new byte[8192];
                    long remaining = compressedSize;
                    while (remaining > 0) {
                        int n = in.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                        if (n == -1) throw new EOFException("Upload ended before all bytes arrived.");
                        fileOut.write(buffer, 0, n);
                        remaining -= n;
                    }
                }

                String safeName = safeFileName(requestedName);
                Path destination = uniquePath(RECEIVED_DIR, safeName);

                try (InputStream fileIn = new GZIPInputStream(Files.newInputStream(tempGzip));
                     OutputStream fileOut = Files.newOutputStream(destination)) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = fileIn.read(buffer)) != -1) fileOut.write(buffer, 0, n);
                }

                String extractMessage = extractIfArchive(destination);
                out.writeBoolean(true);
                out.writeUTF("File received successfully: " + destination.getFileName() + extractMessage);
                out.flush();

                System.out.println("File received successfully: " + destination);
                if (!extractMessage.isEmpty()) System.out.println(extractMessage.trim());
            } finally {
                Files.deleteIfExists(tempGzip);
            }
        } catch (Exception e) {
            System.out.println("Client transfer error: " + e.getMessage());
        }
    }

    private static String safeFileName(String name) {
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (cleaned.isEmpty() || cleaned.equals(".") || cleaned.equals("..")) return "received_file";
        return cleaned;
    }

    private static Path uniquePath(Path folder, String name) throws IOException {
        Path target = folder.resolve(name).normalize();
        if (!target.startsWith(folder)) throw new IOException("Invalid filename.");
        if (!Files.exists(target)) return target;
        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0) { base = name.substring(0, dot); ext = name.substring(dot); }
        int i = 1;
        while (Files.exists(folder.resolve(base + "_" + i + ext))) i++;
        return folder.resolve(base + "_" + i + ext);
    }

    private static String extractIfArchive(Path file) throws IOException {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        Path extractedRoot = RECEIVED_DIR.resolve("extracted");
        if (name.endsWith(".zip")) {
            Files.createDirectories(extractedRoot);
            String folderName = file.getFileName().toString().replaceFirst("(?i)\\.zip$", "");
            Path outputDir = uniqueDirectory(extractedRoot, safeFileName(folderName));
            Files.createDirectories(outputDir);
            int count = 0;
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file))) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zis.getNextEntry()) != null) {
                    Path outPath = outputDir.resolve(entry.getName()).normalize();
                    if (!outPath.startsWith(outputDir)) throw new IOException("Unsafe ZIP entry path.");
                    if (entry.isDirectory()) {
                        Files.createDirectories(outPath);
                    } else {
                        Files.createDirectories(outPath.getParent());
                        try (OutputStream os = Files.newOutputStream(outPath)) {
                            int n;
                            while ((n = zis.read(buffer)) != -1) os.write(buffer, 0, n);
                        }
                        count++;
                    }
                    zis.closeEntry();
                }
            }
            return "\nZIP extracted to: " + outputDir + " (" + count + " files)";
        } else if (name.endsWith(".gz")) {
            Files.createDirectories(extractedRoot);
            String outputName = file.getFileName().toString().replaceFirst("(?i)\\.gz$", "");
            if (outputName.isBlank()) outputName = "extracted_file";
            Path output = uniquePath(extractedRoot, safeFileName(outputName));
            try (InputStream is = new GZIPInputStream(Files.newInputStream(file));
                 OutputStream os = Files.newOutputStream(output)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = is.read(buffer)) != -1) os.write(buffer, 0, n);
            }
            return "\nGZIP extracted to: " + output;
        }
        return "";
    }

    private static Path uniqueDirectory(Path parent, String name) throws IOException {
        Path candidate = parent.resolve(name);
        int i = 1;
        while (Files.exists(candidate)) candidate = parent.resolve(name + "_" + i++);
        return candidate;
    }

    private static void startWebServer() throws IOException {
        HttpServer web = HttpServer.create(new InetSocketAddress(WEB_PORT), 0);
        web.createContext("/", FileServer::handleHome);
        web.createContext("/download", FileServer::handleDownload);
        web.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        web.start();
        System.out.println("Web page ready: http://localhost:" + WEB_PORT);
        System.out.println("Keep this window open while receiving files.");
    }

    private static void handleHome(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            send(exchange, 405, "Method not allowed", "text/plain; charset=UTF-8");
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset='utf-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
            .append("<title>Universal File Transfer</title><style>")
            .append("body{font-family:Arial,sans-serif;max-width:900px;margin:32px auto;padding:0 16px;background:#f5f7fb;color:#202433}")
            .append(".card{background:white;border:1px solid #dce2ee;border-radius:12px;padding:18px;margin:14px 0}")
            .append(".file{padding:12px 0;border-bottom:1px solid #eee;overflow-wrap:anywhere}")
            .append("a{display:inline-block;margin:8px 10px 0 0;color:#0759c7}small{color:#586174}")
            .append("pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#f7f8fa;padding:10px;border-radius:8px;max-height:180px;overflow:auto}")
            .append("</style></head><body><h1>Universal File Transfer</h1>")
            .append("<p>Files received by the Java TCP server appear below. Refresh this page after sending a file.</p>")
            .append("<div class='card'><b>Server status:</b> Running<br><small>TCP transfer port: 5000 · Web/download port: 8080</small><br>")
            .append("<small>Send files using FileClient.java. Any file type can be transferred.</small></div>")
            .append("<h2>Received files</h2>");

        List<Path> files = new ArrayList<>();
        if (Files.exists(RECEIVED_DIR)) {
            try (var stream = Files.walk(RECEIVED_DIR)) {
                stream.filter(Files::isRegularFile)
                      .filter(p -> !p.startsWith(RECEIVED_DIR.resolve("extracted")) || true)
                      .forEach(files::add);
            }
        }
        files.sort(Comparator.comparing(p -> RECEIVED_DIR.relativize(p).toString().toLowerCase(Locale.ROOT)));

        if (files.isEmpty()) {
            html.append("<div class='card'>No files received yet. Start FileClient.java and send a file.</div>");
        } else {
            for (Path file : files) {
                String relative = RECEIVED_DIR.relativize(file).toString().replace(File.separatorChar, '/');
                String encoded = URLEncoder.encode(relative, StandardCharsets.UTF_8);
                html.append("<div class='card file'><b>").append(escapeHtml(relative)).append("</b><br>")
                    .append("<small>").append(Files.size(file)).append(" bytes</small><br>")
                    .append("<a href='/download?name=").append(encoded).append("'>Download file</a>");
                String lower = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (lower.endsWith(".txt") || lower.endsWith(".json") || lower.endsWith(".xml")
                        || lower.endsWith(".csv") || lower.endsWith(".log")) {
                    try {
                        String content = Files.readString(file, StandardCharsets.UTF_8);
                        if (content.length() > 4000) content = content.substring(0, 4000) + "\n... preview shortened ...";
                        html.append("<details><summary>Preview file</summary><pre>")
                            .append(escapeHtml(content)).append("</pre></details>");
                    } catch (Exception ignored) { }
                }
                html.append("</div>");
            }
        }
        html.append("<p><small>Files are stored in the server's received folder. Do not expose this page to untrusted networks.</small></p>")
            .append("</body></html>");
        send(exchange, 200, html.toString(), "text/html; charset=UTF-8");
    }

    private static void handleDownload(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            send(exchange, 405, "Method not allowed", "text/plain; charset=UTF-8");
            return;
        }
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || !query.startsWith("name=")) {
            send(exchange, 400, "Missing file name.", "text/plain; charset=UTF-8");
            return;
        }
        String relative;
        try {
            relative = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            send(exchange, 400, "Invalid file name.", "text/plain; charset=UTF-8");
            return;
        }
        Path file = RECEIVED_DIR.resolve(relative).normalize();
        if (!file.startsWith(RECEIVED_DIR) || !Files.isRegularFile(file)) {
            send(exchange, 404, "File not found.", "text/plain; charset=UTF-8");
            return;
        }
        String filename = file.getFileName().toString().replace("\"", "");
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.sendResponseHeaders(200, Files.size(file));
        try (OutputStream out = exchange.getResponseBody()) {
            Files.copy(file, out);
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
