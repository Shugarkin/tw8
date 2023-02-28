import com.google.gson.Gson;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.SubTask;
import model.Task;
import service.FileBackedTasksManager;
import service.Managers;
import service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    static Gson gson;
    private HttpServer httpServer;

    static TaskManager taskManager = Managers.getFileBackedTasksManager(new File("httpFile.csv"));


    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getGson();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler());
    }

    public void start() {
        httpServer.start();
    }
    public static void main(String[] args) throws IOException {
        System.out.println("Поехали");
        TaskManager taskManager = Managers.getFileBackedTasksManager(new File("httpFile.csv"));
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
    }

    static class TasksHandler implements HttpHandler {
        private int id = 0; //для вычисления айди
        private String taskType = null; //для вычисления типа задачи
        private int uriLenght = 0; //для определения длинны пути
        private String body = null; // для создания задачи

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().getPath();
                String method = httpExchange.getRequestMethod();
                switch (method) {
                    case "DELETE":
                        if (Pattern.matches("^/tasks/task/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/task/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                taskManager.deleteTaskForId(id);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Задача " + id + " удаленна");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/epic/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/epic/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                taskManager.deleteEpicForId(id);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Эпик " + id + " удаленна");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/subtask/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/subtask/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                taskManager.deleteSubTaskForId(id);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Подзадача " + id + " удаленна");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/task/$", path)) {
                            taskManager.deleteTask();
                            System.out.println("Удаленны все задачи");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else if (Pattern.matches("^/tasks/epic/$", path)) {
                            taskManager.deleteEpic();
                            System.out.println("Удаленны все эпики");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else if (Pattern.matches("^/tasks/subtask/$", path)) {
                            taskManager.deleteSubTask();
                            System.out.println("Удаленны все подзадачи");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else {
                            System.out.println("Ошибка пути удаления задачи");
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    case "GET":
                        if (Pattern.matches("^/tasks/task/$", path)) {
                            String answer = gson.toJson(taskManager.getTasks());
                            sendText(httpExchange, answer);
                            System.out.println("Получены все задачи");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else if (Pattern.matches("^/tasks/epic/$", path)) {
                            String answer = gson.toJson(taskManager.getEpics());
                            sendText(httpExchange, answer);
                            System.out.println("Получены все эпики");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else if (Pattern.matches("^/tasks/subtask/$", path)) {
                            String answer = gson.toJson(taskManager.getSubTasks());
                            sendText(httpExchange, answer);
                            System.out.println("Получены все подзадачи");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else if (Pattern.matches("^/tasks/task/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/task/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                String answer = gson.toJson(taskManager.getTask(id));
                                sendText(httpExchange, answer);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Задача " + id + " получена");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/epic/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/epic/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                String answer = gson.toJson(taskManager.getEpic(id));
                                sendText(httpExchange, answer);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Эпик " + id + " получен");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/subtask/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/subtask/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                String answer = gson.toJson(taskManager.getSubTask(id));
                                sendText(httpExchange, answer);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Подзадача " + id + " получена");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/subtask/epic/?id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/subtask/epic/?id=", "");
                            int id = parsePathId(pathId);
                            if (id != -1) {
                                String answer = gson.toJson(taskManager.getSubtasksByEpic(id));
                                sendText(httpExchange, answer);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Подзадачи эпика " + id + " получены");
                            } else {
                                System.out.println("Неверный id -" + id);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else if (Pattern.matches("^/tasks/history$", path)) {
                            String answer = gson.toJson(taskManager.getHistory());
                            sendText(httpExchange, answer);
                            System.out.println("История получена");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else if (Pattern.matches("^/tasks/$", path)) {
                            String answer = gson.toJson(taskManager.getPrioritizedTasks());
                            sendText(httpExchange, answer);
                            System.out.println("Сортированный список получен");
                            httpExchange.sendResponseHeaders(200, 0);
                        } else {
                            System.out.println("Ошибка пути получения задачи");
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    case "POST":
                        if (Pattern.matches("^/tasks/task/", path)) {
                            InputStream input = httpExchange.getRequestBody();
                            String taskString = new String(input.readAllBytes(), DEFAULT_CHARSET);
                            Task task = gson.fromJson(taskString, Task.class);
                            if (taskManager.getTasks().contains(task)) {
                                taskManager.updateTask(task);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Задача успешно заменена");
                            } else {
                                taskManager.addTask(task);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Задача успешно добавлена");
                            }
                        } else if (Pattern.matches("^/tasks/epic/", path)) {
                            InputStream input = httpExchange.getRequestBody();
                            String epicString = new String(input.readAllBytes(), DEFAULT_CHARSET);
                            Epic epic = gson.fromJson(epicString, Epic.class);
                            if (taskManager.getEpics().contains(epic)) {
                                taskManager.updateEpic(epic);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Эпик успешно заменен");
                            } else {
                                taskManager.addEpic(epic);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Эпик успешно добавлен");
                            }
                        } else if (Pattern.matches("^/tasks/subtask/", path)) {
                            InputStream input = httpExchange.getRequestBody();
                            String subtaskString =
                                    new String(input.readAllBytes(), DEFAULT_CHARSET);
                            SubTask subtask = gson.fromJson(subtaskString, SubTask.class);
                            if (taskManager.getSubTasks().contains(subtask)) {
                                taskManager.updateSubTask(subtask);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Подзадача успешно заменена");
                            } else {
                                taskManager.addSubTask(subtask);
                                System.out.println(subtaskString);
                                httpExchange.sendResponseHeaders(200, 0);
                                System.out.println("Подзадача успешно добавлена");
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                            System.out.println("Произошли технические шоколадки");
                        }
                        break;
                    default: {
                        System.out.println("Неизвестный запрос - " + method);
                        httpExchange.sendResponseHeaders(405, 0);
                    }
                }

            } catch (Exception e) {
                e.getMessage();
            } finally {
                httpExchange.close();
            }
        }

        private int parsePathId(String path) {
            try {
                return Integer.parseInt(path);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        protected void sendText(HttpExchange h, String text) throws IOException {
            byte[] resp = text.getBytes(UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json");
            h.sendResponseHeaders(200, resp.length);
            h.getResponseBody().write(resp);
        }

    }
}
