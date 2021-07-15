package org.extract.Data;

public class TableRows {

    private boolean complete;
    private long eeTaskId;
    private long taskId;
    private String taskDescription;
    private String taskType;
    private String taskFor;
    private String completedBy;
    private String timeCompleted;
    private boolean startTask;

    public TableRows(boolean complete, long eeTaskId, long taskId, String taskDescription, String taskType,
                     String taskFor, String completedBy, String timeCompleted, boolean startTask) {
        this.complete = complete;
        this.eeTaskId = eeTaskId;
        this.taskId = taskId;
        this.taskDescription = taskDescription;
        this.taskType = taskType;
        this.taskFor = taskFor;
        this.completedBy = completedBy;
        this.timeCompleted = timeCompleted;
        this.startTask = startTask;
    }


    @Override
    public String toString() {
        return  "complete=" + complete +
                ", eeTaskId=" + eeTaskId +
                ", taskId=" + taskId +
                ", taskDescription='" + taskDescription + '\'' +
                ", taskType='" + taskType + '\'' +
                ", taskFor='" + taskFor + '\'' +
                ", completedBy='" + completedBy + '\'' +
                ", timeCompleted='" + timeCompleted + '\'' +
                ", startTask=" + startTask;
    }
}
