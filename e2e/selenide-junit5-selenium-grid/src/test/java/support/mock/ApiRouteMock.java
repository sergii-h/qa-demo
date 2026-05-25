package support.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.TaskResponse;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.openqa.selenium.remote.http.Contents.utf8String;

public class ApiRouteMock {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TASKS_PATH = "/v1/tasks";

    private final NetworkInterceptor interceptor;
    private final List<TaskResponse> getTasksResponse = new CopyOnWriteArrayList<>();
    private final Map<String, TaskResponse> getTaskResponses = new ConcurrentHashMap<>();
    private final Map<String, Boolean> getIsValidResponses = new ConcurrentHashMap<>();
    private final Map<String, TaskResponse> createTaskResponses = new ConcurrentHashMap<>();
    private final Map<String, TaskResponse> updateTaskResponses = new ConcurrentHashMap<>();
    private final Set<String> deleteTaskIds = ConcurrentHashMap.newKeySet();

    public ApiRouteMock(WebDriver driver) {
        Filter filter = next -> req -> matchRequest(req).orElseGet(() -> next.execute(req));
        interceptor = new NetworkInterceptor(driver, filter);
    }

    public void getTasks(List<TaskResponse> tasks) {
        getTasksResponse.clear();
        getTasksResponse.addAll(tasks);
    }

    public void getTask(String id, TaskResponse response) {
        getTaskResponses.put(id, response);
    }

    public void getIsValid(String id, boolean isValid) {
        getIsValidResponses.put(id, isValid);
    }

    public void createTask(TaskResponse response) {
        createTaskResponses.put(response.getId(), response);
    }

    public void updateTask(String id, TaskResponse response) {
        updateTaskResponses.put(id, response);
    }

    public void deleteTask(String id) {
        deleteTaskIds.add(id);
    }

    private Optional<HttpResponse> matchRequest(HttpRequest request) {
        String uri = request.getUri();
        HttpMethod method = request.getMethod();

        if (method == HttpMethod.GET && uri.matches(".*/v1/tasks/?$") && !getTasksResponse.isEmpty()) {
            return Optional.of(jsonResponse(200, getTasksResponse));
        }

        if (method == HttpMethod.GET && uri.contains("/v1/tasks/isValid/")) {
            String id = extractId(uri, "/v1/tasks/isValid/");
            if (getIsValidResponses.containsKey(id)) {
                return Optional.of(jsonResponse(200, getIsValidResponses.get(id)));
            }
        }

        if (method == HttpMethod.GET && uri.matches(".*/v1/tasks/[^/]+$")) {
            String id = extractId(uri, TASKS_PATH + "/");
            if (getTaskResponses.containsKey(id)) {
                return Optional.of(jsonResponse(200, getTaskResponses.get(id)));
            }
        }

        if (method == HttpMethod.POST && uri.matches(".*/v1/tasks/?$") && !createTaskResponses.isEmpty()) {
            TaskResponse response = createTaskResponses.values().iterator().next();
            return Optional.of(jsonResponse(201, response));
        }

        if (method == HttpMethod.PUT && uri.matches(".*/v1/tasks/[^/]+$")) {
            String id = extractId(uri, TASKS_PATH + "/");
            if (updateTaskResponses.containsKey(id)) {
                return Optional.of(jsonResponse(200, updateTaskResponses.get(id)));
            }
        }

        if (method == HttpMethod.DELETE && uri.matches(".*/v1/tasks/[^/]+$")) {
            String id = extractId(uri, TASKS_PATH + "/");
            if (deleteTaskIds.contains(id)) {
                return Optional.of(new HttpResponse().setStatus(204));
            }
        }

        return Optional.empty();
    }

    private static String extractId(String uri, String prefixMarker) {
        int index = uri.indexOf(prefixMarker);
        if (index < 0) {
            return uri.substring(uri.lastIndexOf('/') + 1);
        }
        return uri.substring(index + prefixMarker.length());
    }

    private static HttpResponse jsonResponse(int status, Object body) {
        try {
            return new HttpResponse()
                    .setStatus(status)
                    .addHeader("Content-Type", "application/json")
                    .setContent(utf8String(OBJECT_MAPPER.writeValueAsString(body)));
        } catch (JsonProcessingException e) {
            throw new InternalError(e);
        }
    }

    public void close() {
        interceptor.close();
    }
}
