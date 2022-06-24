package com.example.filemanagers;

public class EventMessage {
    private boolean delete = false;
    private String deleteFilePath = null;

    public String getDeleteFilePath() {
        return deleteFilePath;
    }

    public void setDeleteFilePath(String deleteFilePath) {
        this.deleteFilePath = deleteFilePath;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
