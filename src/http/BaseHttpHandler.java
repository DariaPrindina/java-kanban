package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text, HttpStatusCode statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode.getCode(), resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendText(h, text, HttpStatusCode.OK);
    }

    protected void sendCreated(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpStatusCode.CREATED.getCode(), -1);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String message) throws IOException {
        sendText(h, "{\"error\":\"" + message + "\"}", HttpStatusCode.NOT_FOUND);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Задача пересекается по времени с существующей\"}", HttpStatusCode.NOT_ACCEPTABLE);
    }

    protected void sendInternalError(HttpExchange h, String message) throws IOException {
        sendText(h, "{\"error\":\"" + message + "\"}", HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    protected String readBody(HttpExchange h) throws IOException {
        InputStream is = h.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected int parseId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
