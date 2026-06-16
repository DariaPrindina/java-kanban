package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client = HttpClient.newHttpClient();

    HttpTaskManagerSubtasksTest() throws IOException {}

    @BeforeEach
    void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void testGetSubtasksReturnsEmptyList() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("SubTest", "Desc", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask.setDuration(Duration.ofMinutes(30));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.CREATED.getCode(), response.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
        assertEquals("SubTest", manager.getAllSubtasks().get(0).getName());
    }

    @Test
    void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void testAddOverlappingSubtaskReturns406() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("S1", "Desc", epic.getId());
        s1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        s1.setDuration(Duration.ofMinutes(60));
        manager.addSubtask(s1);

        Subtask s2 = new Subtask("S2", "Desc", epic.getId());
        s2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        s2.setDuration(Duration.ofMinutes(60));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(s2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.NOT_ACCEPTABLE.getCode(), response.statusCode());
    }
}
