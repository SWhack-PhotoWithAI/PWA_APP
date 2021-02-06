# PWA_APP 

# Position
Backend 개발 - 최수연  
Android app과 통신 및 openAPI를 활용한 필터링 기능 개발  

# Technology
* SpringBoot 2.3.9(SNAPSHOT)  
* heroku(https://test-photo-with-ai.herokuapp.com/)  


# Issue  
* 스프링에서 [OpenAPI](https://ainize.ai/psi1104/White-box-Cartoonization?branch=master)의 /predict를 POST로 호출시 파일을 읽지 못함  
### Code  
```Java
String originalName = file.getOriginalFilename();  
String filePath = basePath + "/" + originalName;  
File dest = new File(filePath);  
file.transferTo(dest);  

Unirest.setTimeouts(0, 0);  
HttpResponse<String> response = Unirest.post("https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict")
                .field("file", file)
                .field("file_type", "image")
                .asString();
```
### Error message  
```  
{"messeage": "Error! Please upload another file"}  
```  


