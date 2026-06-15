package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import task.Subtask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            boolean hasId = path.matches("/subtasks/\\d+");

            switch (method) {
                case "GET":
                    if (hasId) {
                        int id = parseId(path);
                        sendText(h, gson.toJson(manager.getSubtask(id)));
                    } else {
                        sendText(h, gson.toJson(manager.getAllSubtasks()));
                    }
                    break;
                case "POST":
                    String body = readBody(h);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    if (subtask.getId() == 0) {
                        manager.addSubtask(subtask);
                    } else {
                        manager.updateSubtask(subtask);
                    }
                    sendCreated(h);
                    break;
                case "DELETE":
                    if (hasId) {
                        manager.deleteSubtask(parseId(path));
                        sendText(h, "{}");
                    } else {
                        sendNotFound(h, "Укажите id подзадачи");
                    }
                    break;
                default:
                    sendNotFound(h, "Метод не поддерживается");
            }
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendHasInteractions(h);
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}
