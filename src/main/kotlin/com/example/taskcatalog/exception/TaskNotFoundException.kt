package com.example.taskcatalog.exception

class TaskNotFoundException(taskId: Long) : RuntimeException("Task with id=$taskId was not found")
