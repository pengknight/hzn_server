package cn.wgygroup.hzn_server.db;

import javax.persistence.*;
import java.text.SimpleDateFormat;

@Entity
public class FileUploadEntity  {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String fileName;
    String fileKey;

    @Transient
    String password;
    String userName;
    String hashCode;

    long uploadTime;

    boolean hasRead=false;
    boolean hasVirus=false;
    String filePath;

    @Transient
    String fileUri;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public String getTime(){

        SimpleDateFormat scuff=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 时间戳转换成时间

        return scuff.format(uploadTime);
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public boolean isHasRead() {
        return hasRead;
    }

    public String hasVirus(){
        if (hasVirus){
            return "有病毒";
        }else {
            return "无病毒";
        }
    }

    public boolean isHasVirus() {
        return hasVirus;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    public void setHasVirus(boolean hasVirus) {
        this.hasVirus = hasVirus;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String fileUri) {
        this.filePath = fileUri;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }
}
