package knowhow.photowithai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.filechooser.FileSystemView;
import javax.xml.xpath.XPath;
import java.io.File;
import java.util.List;

@Controller
public class ImageApiController {

    @GetMapping("/upload")
    String upload() {
        return "upload";
    }

    @PostMapping("/upload")
    @ResponseBody
    String uploadMultiImages(@RequestParam("files") List<MultipartFile> files) throws Exception {
        //Save images in root folder
        String rootPath = FileSystemView.getFileSystemView().getHomeDirectory().toString();
        String basePath = rootPath + "/" + "images";
        File Folder = new File(basePath);
        for (MultipartFile file : files) {
            if(!Folder.exists()){
                Folder.mkdir();
            }
            String originalName = file.getOriginalFilename();
            String filePath = basePath + "/" + originalName;
            File dest = new File(filePath);
            file.transferTo(dest);
        }
        return "hello";
    }

}
