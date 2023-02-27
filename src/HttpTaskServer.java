import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import service.FileBackedTasksManager;
import service.Managers;
import service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    static FileBackedTasksManager taskManager = new FileBackedTasksManager(new File("httpFile.csv"));

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler());
        System.out.println("Поехали");
        httpServer.start();
    }

    static class TasksHandler implements HttpHandler {
        private int id = 0; //для вычисления айди
        private String taskType = null; //для вычисления типа задачи
        private int uriLenght = 0; //для определения длинны пути
        private String body = null; // для создания задачи

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("пытаемся запустить");
            uriSplit(httpExchange);

            String method = httpExchange.getRequestMethod();

            switch (method) {
                case "DELETE":
                    if ((uriLenght == 2)) { //если "удаление" и длинна пути всего 2, то удаляются все задачи из типа
                        switch (taskType) {
                            case "task":
                                taskManager.deleteTask();
                                System.out.println("удалили таски");
                                break;
                            case "epic":
                                taskManager.deleteEpic();
                                System.out.println("удалили эпики");
                                break;
                            case "subtask":
                                taskManager.deleteSubTask();
                                System.out.println("удалили сабтаски");
                                break;
                        }
                    } else {
                        switch (taskType) { //здесь уже нужен айди
                            case "task":
                                taskManager.deleteTaskForId(id);
                                System.out.println("удалили таск");
                                break;
                            case "epic":
                                taskManager.deleteEpicForId(id);
                                System.out.println("удалили эпик");
                                break;
                            case "subtask":
                                taskManager.deleteSubTaskForId(id);
                                System.out.println("удалили саб");
                                break;
                        }
                    }
                case "GET":
                    switch (uriLenght) {
                        case 1://по схеме задания надо вроде возвращать сортированнаый список
                            taskManager.getPrioritizedTasks();
                            System.out.println("удалили таск");
                            break;
                        case 2://здесь просто возвращаются все задачи из типа и история
                            switch (taskType) {
                                case "task":
                                    taskManager.getTasks();
                                    System.out.println("получили таск");
                                    break;
                                case "epic":
                                    taskManager.getEpics();
                                    System.out.println("получили эпик");
                                    break;
                                case "subtask":
                                    taskManager.getSubTasks();
                                    System.out.println("получили саб");
                                    break;
                                case "history":
                                    taskManager.getHistory();
                                    System.out.println("получили историю");
                                    break;
                            }
                            break;
                        case 3://а здесь уже по айди
                            switch (taskType) {
                                case "task":
                                    taskManager.getTask(id);
                                    System.out.println("получили таск ай");
                                    break;
                                case "epic":
                                    taskManager.getEpic(id);
                                    System.out.println("получили эпик ай");
                                    break;
                                case "subtask":
                                    taskManager.getSubTask(id);
                                    System.out.println("получили саб ай");
                                    break;
                            }
                            break;

                    }
                case "POST":
                    taskManager.fromString(body); /*наверно можно было использовать этот метод,
                     иначе если создавать новый, то он будет точной копией */
                    System.out.println("создали задачу");
                    break;
            }
        }


        public void uriSplit(HttpExchange httpExchange) throws IOException {
            System.out.println("здесь? запустить");
            URI requestURI = httpExchange.getRequestURI();
            String path = requestURI.getPath();
            String[] action = path.split("/");
            if (action.length == 2) { //если длинна всего 2, то колличество возможных вариантов действий сокращается
                uriLenght = 1;
                System.out.println("1");
            } else if (action.length == 3) { //если 3, то надо узнать тип задачи с которой будут что-то делать
                taskType = action[2];
                InputStream inputStream = httpExchange.getRequestBody();
                body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                uriLenght = 2;
                System.out.println("2");
            } else if (action.length == 4) { //здесь уже и айди нужен
                taskType = action[2];
                String split = action[3];
                int number = split.indexOf("=");
                id = Integer.parseInt(split.substring(number + 1));
                uriLenght = 3;
                System.out.println("3");
            }
        }

        }
    }


