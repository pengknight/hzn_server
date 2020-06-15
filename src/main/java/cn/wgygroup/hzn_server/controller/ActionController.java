package cn.wgygroup.hzn_server.controller;

import cn.wgygroup.hzn_server.db.FileUploadDao;
import cn.wgygroup.hzn_server.db.FileUploadEntity;
import cn.wgygroup.hzn_server.db.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ActionController {


    private final Path rootLocation = Paths.get("./tmp");

    @Autowired
    public ActionController() {
    }

    @Resource
    FileUploadDao fileUploadDao;

    /**
     * 自定义登录页面
     *
     * @param error 错误信息显示标识
     * @return
     */
    @RequestMapping("/login")
    public ModelAndView login(String error) {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("error", error);
        return modelAndView;
    }

    @RequestMapping({"/main_body"})
    public ModelAndView main_body() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelAndView view = new ModelAndView("main_body");
        if ("anonymousUser".equals(principal)) {
            view.addObject("name", "anonymous");
        } else {
            UserEntity user = (UserEntity) principal;
            view.addObject("name", user.getTrueName());
            List<FileUploadEntity> uploadEntities = fileUploadDao.findByUserNameOrderByUploadTimeDesc(user.getUsername());

            Stream<FileUploadEntity> stream = uploadEntities.stream();

            List<FileUploadEntity> entities = stream.peek(entity -> entity.setFileUri(MvcUriComponentsBuilder.fromMethodName(
                    ActionController.class, "serveFile",
                    String.valueOf(entity.getId())
                    ).build().toString())
            ).collect(Collectors.toList());
            view.addObject("entities", entities);
        }
        return view;
    }

    @GetMapping("/files/{fileId}")
    @ResponseBody
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(@PathVariable String fileId) {
        org.springframework.core.io.Resource file = null;
        FileUploadEntity one = fileUploadDao.getOne(Integer.parseInt(fileId));
        String filePath = one.getFilePath();
        Path path = Paths.get(filePath);
        try {
            file = new UrlResource(path.toUri());
            String filename = file.getFilename();
            if (filename != null) {
                int i = filename.indexOf("-");
                filename = filename.substring(i+1);
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filename + "\"").body(file);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return ResponseEntity.notFound().build();
    }



}
