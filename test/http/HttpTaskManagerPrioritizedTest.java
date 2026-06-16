package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client = HttpClient.newHttpClient();

    HttpTaskManagerPrioritizedTest() throws IOException {}

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
    void testGetEmptyPrioritized() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testPrioritizedReturnsSortedTasks() throws IOException, InterruptedException {
        Task late = new Task("LateTask", "Desc");
        late.setStartTime(LocalDateTime.of(2024, 1, 1, 14, 0));
        late.setDuration(Duration.ofMinutes(30));
        manager.addTask(late);

        Task early = new Task("EarlyTask", "Desc");
        early.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        early.setDuration(Duration.ofMinutes(30));
        manager.addTask(early);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
        int earlyIdx = response.body().indexOf("EarlyTask");
        int lateIdx = response.body().indexOf("LateTask");
        assertTrue(earlyIdx < lateIdx, "EarlyTask должна идти первой в prioritized");
    }
}
