package knowhow.photowithai.controller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import knowhow.photowithai.service.FilterImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class FilterImageController {

    @Autowired
    FilterImageService filterImageService;

    @PostMapping("/filter")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file) throws UnirestException, IOException {
//        String msg = filterImageService.uploadFile(file);
//        System.out.println(msg);
        return "hello";
    }
}
