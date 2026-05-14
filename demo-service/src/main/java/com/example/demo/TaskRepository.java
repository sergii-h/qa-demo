package com.example.demo;

import com.example.demo.data.Task;
import com.example.demo.data.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByStatus(TaskStatus status);
    Task findByTitle(String title);
}

