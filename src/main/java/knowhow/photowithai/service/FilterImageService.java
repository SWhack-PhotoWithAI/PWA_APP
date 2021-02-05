package knowhow.photowithai.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FilterImageService {

    public String uploadFile(MultipartFile file) throws IOException, UnirestException {
        String rootPath = FileSystemView.getFileSystemView().getHomeDirectory().toString();
        String basePath = rootPath + "/" + "images";
        File Folder = new File(basePath);
        if (!Folder.exists()) {
            Folder.mkdir();
        }
        String originalName = file.getOriginalFilename();
        String filePath = basePath + "/" + originalName;
        File dest = new File(filePath);
        file.transferTo(dest);

        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict")
                .field("file", file)
                .field("file_type", "image")
                .asString();
        return response.getBody();
    }
}
