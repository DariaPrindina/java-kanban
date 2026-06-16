package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import task.Task;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            boolean hasId = path.matches("/tasks/\\d+");

            switch (method) {
                case "GET":
                    if (hasId) {
                        int id = parseId(path);
                        sendText(h, gson.toJson(manager.getTask(id)));
                    } else {
                        sendText(h, gson.toJson(manager.getAllTasks()));
                    }
                    break;
                case "POST":
                    String body = readBody(h);
                    Task task = gson.fromJson(body, Task.class);
                    if (task.getId() == 0) {
                        manager.addTask(task);
                    } else {
                        manager.updateTask(task);
                    }
                    sendCreated(h);
                    break;
                case "DELETE":
                    if (hasId) {
                        manager.deleteTask(parseId(path));
                        sendText(h, "{}");
                    } else {
                        sendNotFound(h, "Укажите id задачи");
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
