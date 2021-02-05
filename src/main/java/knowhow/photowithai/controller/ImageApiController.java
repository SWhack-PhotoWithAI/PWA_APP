package knowhow.photowithai.controller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Controller
public class ImageApiController {
    @GetMapping("/")
    @ResponseBody
    String home() {
        return "home page";
    }
//    @GetMapping("/filter")
//    public String filterImage() {
//        return "filter";
//    }

//    @PostMapping("/filter")
//    @ResponseBody
//    public String filterImage(@RequestParam("files") MultipartFile mf, HttpServletRequest req, HttpServletResponse res) throws IOException, UnirestException {
//        File file= new File("src/main/resources/targetFile.jpg");
//
//        mf.transferTo(file);
//        Unirest.setTimeouts(0, 0);
//        HttpResponse<String> response = Unirest.post("https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict")
//                .field("file", file)
//                .field("file_type", "image")
//                .asString();
//        System.out.println(response.getBody());


    //        String rootPath = FileSystemView.getFileSystemView().getHomeDirectory().toString();
//        String basePath = rootPath + "/" + "images";
//        File Folder = new File(basePath);
//        if (!Folder.exists()) {
//            Folder.mkdir();
//        }
//        String originalName = file.getOriginalFilename();
//        String filePath = basePath + "/" + originalName;
//        File dest = new File(filePath);
//        file.transferTo(dest);


//
//        OkHttpClient client = new OkHttpClient().newBuilder().build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("source", String.valueOf(dest),
//                        RequestBody.create(MediaType.parse("application/octet-stream"),
//                                dest))
//                .addFormDataPart("file_type", "image")
//                .build();
//        Request request = new Request.Builder()
//                .url("https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict")
//                .method("POST", body)
//                .build();
//        Response response = client.newCall(request).execute();
//        System.out.println(response.body().string());

//        return "hello";
//    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String uploadMultiImages(@RequestParam("files") List<MultipartFile> files) throws Exception {
        //Save images in root folder
        String rootPath = FileSystemView.getFileSystemView().getHomeDirectory().toString();
        String basePath = rootPath + "/" + "images";
        File Folder = new File(basePath);
        for (MultipartFile file : files) {
            if (!Folder.exists()) {
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
