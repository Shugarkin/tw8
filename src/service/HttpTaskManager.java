package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

public class HttpTaskManager extends FileBackedTasksManager {
    URI uri;
    KVTaskClient taskClient;
    public HttpTaskManager(URI uri) {
        this.uri = uri;
        this.taskClient = new KVTaskClient(uri);
    }

    @Override
    protected void save() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : super.getTasks()) {
            stringBuilder.append(task);
        }
        for (Epic epic : super.getEpics()) {
            stringBuilder.append(epic);
        }
        for (SubTask subTask : super.getSubTasks()) {
            stringBuilder.append(subTask);
        }
        stringBuilder.append(super.getHistory());

        String allTasks = stringBuilder.toString();
        //key и таски


        try {
            taskClient.put(String.valueOf(100), allTasks);
        } catch (Exception e) {
            e.getMessage();
            System.out.println("Возникли проблемы с отправкой на сервер");
        }
    }

    protected void load() throws IOException, InterruptedException {
        taskClient.load(String.valueOf(100));
    }
}
