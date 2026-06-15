package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            boolean hasId = path.matches("/epics/\\d+");
            boolean isSubtasks = path.matches("/epics/\\d+/subtasks");

            switch (method) {
                case "GET":
                    if (isSubtasks) {
                        int epicId = Integer.parseInt(path.split("/")[2]);
                        sendText(h, gson.toJson(manager.getSubtasksOfEpic(epicId)));
                    } else if (hasId) {
                        sendText(h, gson.toJson(manager.getEpic(parseId(path))));
                    } else {
                        sendText(h, gson.toJson(manager.getAllEpics()));
                    }
                    break;
                case "POST":
                    Epic epic = gson.fromJson(readBody(h), Epic.class);
                    manager.addEpic(epic);
                    sendCreated(h);
                    break;
                case "DELETE":
                    if (hasId) {
                        manager.deleteEpic(parseId(path));
                        sendText(h, "{}");
                    } else {
                        sendNotFound(h, "Укажите id эпика");
                    }
                    break;
                default:
                    sendNotFound(h, "Метод не поддерживается");
            }
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}
