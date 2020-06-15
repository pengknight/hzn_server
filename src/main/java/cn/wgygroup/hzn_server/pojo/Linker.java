package cn.wgygroup.hzn_server.pojo;

/**
 * @author zsh
 * @company wlgzs
 * @create 2018-12-15 16:14
 * @Describe
 */
public class Linker {
    private String fileUrl;
    private String fileName;

    public Linker(String fileUrl, String fileName) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    public Linker() {
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
