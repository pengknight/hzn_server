package cn.wgygroup.hzn_server.services.impl;

import cn.wgygroup.hzn_server.services.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author zsh
 * @company wlgzs
 * @create 2018-12-15 16:16
 * @Describe
 */
@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation=Paths.get("./tmp");

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            System.out.println("Could not initialize storage"+e);
               // throw new StorageException("Could not initialize storage", e);
        }
    }


    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            if (file.isEmpty()) {
                System.out.println("Failed to store empty file" + filename);
                //throw new StorageException("Failed to store empty file" + filename);
            }
            if (filename.contains("..")) {
                System.out.println("Cannot store file with relative path outside current directory "
                        + filename);
                // This is a security check
               // throw new StorageException("Cannot store file with relative path outside current directory " + filename);
            }
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to store file" + filename+e);
            //throw new StorageException("Failed to store file" + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            System.out.println("Failed to read stored files"+e);
            //throw new StorageException("Failed to read stored files", e);
        }
        return null;
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);

            Resource resource = new UrlResource(file.toUri());
            boolean exists = resource.exists();
            boolean readable = resource.isReadable();

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                System.out.println("Could not read file: " + filename);
                //throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            System.out.println("Could not read file: " + filename);
            //throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
        return null;
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
