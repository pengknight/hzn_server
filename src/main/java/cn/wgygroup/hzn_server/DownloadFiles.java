package cn.wgygroup.hzn_server;


import cn.wgygroup.hzn_server.db.FileUploadDao;
import cn.wgygroup.hzn_server.db.FileUploadEntity;
import cn.wgygroup.hzn_server.db.UserDao;
import cn.wgygroup.hzn_server.db.UserEntity;
import com.qiniu.util.Auth;
import fi.solita.clamav.ClamAVClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class DownloadFiles {

    @Value("${baseUrl}")
    String baseUrl;

    @Resource
    RestTemplate restTemplate;

    @Resource
    FileUploadDao fileUploadDao;

    @Resource
    UserDao userDao;

    ClamAVClient cl = new ClamAVClient("172.17.0.1", 3310,20000);

    @Scheduled(initialDelay = 2500, fixedDelay = 5000)
    public void download() {
        //获取要下载的文件列表
        System.out.println("Download file at "+new Date().toString());
        final String url = baseUrl + "/server/download";

        FileUploadEntity[] templateForObject = restTemplate.getForObject(url, FileUploadEntity[].class);
        if (templateForObject != null && templateForObject.length > 0) {
            for (FileUploadEntity entity : templateForObject) {
                entity.setHasRead(false);
                //下载文件
                File file = download(entity);
                boolean av = av(file);
//                System.out.println("end av scan" + file.getName() + " " + new Date());
                if (av) {
                    System.out.println("文件带毒，已删除");
                    entity.setHasVirus(true);
                } else {
                    System.out.println("文件无毒");
                    entity.setHasVirus(false);
                    entity.setFilePath(file.getPath());
                }
                fileUploadDao.save(entity);
            }
//            List<FileUploadEntity> entities = Arrays.asList(templateForObject);
//            fileUploadDao.saveAll(entities);
        }
        //下载并校验、杀毒for
        //保存到本地数据库
//        System.out.println("started"+new Date().toString());
    }

    //同步人员数据
    @Scheduled(initialDelay = 15000, fixedDelay = 30000)
    public void updateUser() {
        System.out.println("SYN people at "+new Date().toString());
        //获取要下载的文件列表
        String url = baseUrl + "/server/updateUser";

        UserEntity[] templateForObject = restTemplate.getForObject(url, UserEntity[].class);
        if (templateForObject != null && templateForObject.length > 0) {
            List<UserEntity> newEntity = Arrays.asList(templateForObject);
            List<UserEntity> oldEntity = userDao.findAll();

            //删除
            List<UserEntity> delEntity = new ArrayList<>(oldEntity);
            delEntity.removeAll(newEntity);
            userDao.deleteAll(delEntity);

            //添加
            List<UserEntity> addEntity = new ArrayList<>(newEntity);
            addEntity.removeAll(oldEntity);
            userDao.saveAll(addEntity);

        }
    }

    public File download(FileUploadEntity entity) {
        //获取token
        final String domainOfBucket = "http://qiniu.pengknight.cn";
        final String accessKey = "51Rxb3jEuzsAQKJS5hpEVPvcDZsXWWCSVYBoUGpL";
        final String secretKey = "8EY5To4DfQY6DUzRqw2bYUtEwvB_JeVCHb4i7-p1";
        final long expireInSeconds = 120;//1小时，可以自定义链接过期时间
        String fileName = entity.getFileKey();
//        String userName = entity.getUserName();
        int id = entity.getId();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        Auth auth = Auth.create(accessKey, secretKey);
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);

        return restTemplate.execute(finalUrl, HttpMethod.GET, null, clientHttpResponse -> {
            //创建文件夹
            String dirPath = "./tmp";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                boolean b = dir.mkdirs();
                System.out.println("文件夹已创建" + dirPath);
            }
            //创建文件
//            String filePath = dirPath + "/" + entity.getHashCode();
            String filePath = dirPath + "/" + id+"-"+entity.getFileName();
            File ret = new File(filePath);
            if (!ret.exists()) {
                boolean newFile = ret.createNewFile();
                System.out.println("文件已创建" + filePath);
            } else {
                boolean delete = ret.delete();
                boolean newFile = ret.createNewFile();
                System.out.println("文件已覆盖" + filePath);
            }
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
    }

    private boolean av(File file) {
        FileInputStream inputStream;
        byte[] reply = new byte[0];
        try {
            inputStream = new FileInputStream(file);
            reply = cl.scan(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return !ClamAVClient.isCleanReply(reply);
    }
}
