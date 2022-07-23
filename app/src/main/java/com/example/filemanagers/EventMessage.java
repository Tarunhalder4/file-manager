package com.example.filemanagers;

public class EventMessage {
    private boolean fileDelete = false;
    private String FilePath = null;
    private boolean fileRename = false;

    public boolean isFileDelete() {
        return fileDelete;
    }

    public void setFileDelete(boolean fileDelete) {
        this.fileDelete = fileDelete;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public boolean isFileRename() {
        return fileRename;
    }

    public void setFileRename(boolean fileRename) {
        this.fileRename = fileRename;
    }


}
