package cn.wgygroup.hzn_server.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileUploadDao extends JpaRepository<FileUploadEntity,Integer> {
    List<FileUploadEntity> findByHasReadFalse();
    List<FileUploadEntity> findByUserNameOrderByUploadTimeDesc(String user);
}
