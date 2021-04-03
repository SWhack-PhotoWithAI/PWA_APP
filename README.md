# 📸 PhotoWithAI 📸 


<br>

생각한 대로 사진이 잘 찍히지 않아 답답했던 경험이 있으신가요? <br> 여러 사진 중 어떤 사진을 SNS에 올려야 할지 고민하다가 시간이 오래 걸린 경험이 있으신가요?

<br><br>
## PhotoWithAI 가 도와드릴게요!


#### :heavy_check_mark: 최고의 사진을 찍을 수 있도록 실시간으로 사진 구도 가이드를 제공합니다.
#### :heavy_check_mark: 사용자가 선택한 여러 사진 중 최고의 사진을 선정하여 사용자의 고민 시간을 덜어줍니다. 
#### :heavy_check_mark: AI 기반 자동 필터 시스템으로 내 사진을 만화처럼 바꿔보세요!
    
<br><br>

## Click Here 🔽🔽  


### [![PhotoWithAI](http://img.youtube.com/vi/h6MuqbK-nLM/0.jpg)](https://youtu.be/h6MuqbK-nLM?t=0s)  

#### Youtube: https://www.youtube.com/watch?v=h6MuqbK-nLM

***


<br/>



### :bulb: 핵심기능
    기능 1) 실시간 사진 구도 가이드 
    기능 2) 비슷한 여러 사진들 중 가장 잘 찍은 사진 
        2.1) 배경 사진 
        2.2) 인물 사진
    기능 3) 사진 cartoonization

### :bulb: 사용한 AI 모델(pretrained)
    * 사진 구도 가이드 -> keypointrcnn_resnet50_fpn
    * 인물 중심 평가 -> keypointrcnn_resnet50_fpn
    * 배경 중심 평가 -> NIMA: Neural IMage Assessment
    
    
### :bulb: 사용한 Open API 
    * AInize White-box-Cartoonization 
    https://ainize.ai/psi1104/White-box-Cartoonization?branch=master
    
    
### :bulb: Branch 설명
    * main -> 안드로이드 구현
    * A_1 -> AI모델 튜닝 및 알고리즘 구현
    * A_2 -> Flask활용 서버로부터 데이터 받아 코드 실행
    * backend -> Backend 서버
       

***
<br/><br/><br/>


# 👩‍💻 개발일지 👨‍💻   (해커톤 기간 2/4 ~ 2/6)  
</br>

## ⏱ [2/4]

### 1. 작업방향(기능 구체화)

    기능 1) 사진 구도 가이드
    기능 2) 비슷한 여러 사진들 중 가장 잘 찍은 사진을 선택해줌 
            2.1) 배경 사진 (인물 없는 풍경사진)
            2.2) 인물 사진


### 2. 작업 진행상황

    2.1 AI 
      * google opensource NIMA 활용, 학습된 모델 front와 연결중 
      * 인물 사진은 데이터셋을 찾아 직접 학습시켜 구현할 계획

    2.2 FrontEnd
     * 완료된 디자인작업물로 화면 그리기 작업
     * 디바이스 갤러리 접근 등 모바일 핵심기능 구현중

    2.3 Backend
    * heroku로 서버구축완료
    * 앱에서 서버로 이미지 다중파일 처리 작업중 
    
    2.4 1차 프로토 타입( 기능2 흐름 )
    
![image](https://user-images.githubusercontent.com/50574738/106924960-f88e8100-6752-11eb-9420-3394bea86d45.png)


### 3. ~10:00 am

    * main -> 멘토링 
    * A_1 -> Good/Bad case 인물 사진 데이터 셋 모으기
    * A_2 -> 각 파트 작업 이어서 진행


<br/><br/>

## ⏱ [2/5 pm 01:00]

### 1. 기능 상세화
    * 기능 1) AI 카메라 (take a photo with AI)
        - 비율, face detection, foot detection 활용 예정
    * 기능 2) 사진 셀렉 기능(pick better one with AI)
        - 풍경/인물 사진 중 선택
        - 선택한 5장(예)의 사진 중에서 가장 잘 찍은 사진 하나를 추천(like-갤러리 내 기능, 인스타 연동)
        
### 2. 작업 진행상황

    * 이미지 연동- get, post 요청하여 body로 내용 넣어서 서버로 이미지 전송 확인
    * AI: opencv - key point detection in video sequences
    * 확정된 기능 디자인 수정

    
<br/><br/>

## ⏱ [2/5 pm 11:00]

### 작업 진행상황
    * AI 
        1) 이미지 분석 알고리즘 구현 완료, pytorch -> Java 코드 변환작업 중
        2) NIMA Model Flask 서버에 배포 완료 -> 적용할 수 있도록 NIMA 알고리즘 수정 중
        
    * Android
        1) UI 작업 완료(디자인 수정 제외)
        2) 사용자가 pick 한 사진들 중 BEST 뽑아주는 기능 구현 완료
        3) 실시간 카메라로 사용자의 자세를 잡아주는 모델 안드로이드에서 사용할 수 있도록 개발 중
        
    * Backend
        1) Rest API 구축하여 프론트와 서버에서 GET 호출 가능한 상태
        2) 이미지에 필터 효과를 입힐 수 있는 기능 Open API 사용하여 개발 중
        
    *  Design
        1) 기타 디자인 수정완료
        2) BEST PICK, AI CAMERA(+guide message) 작업 중 
   ![image](https://user-images.githubusercontent.com/50574738/107045681-21714d80-6809-11eb-9831-a29f6120363e.png)
   
   
***  
<br/><br/><br/>

# 💁‍♀️ NOW (해커톤 기간 이후)
<br/>

### ✔️ 기능 추가

    * 실시간 카메라 프레임을 받아와서 사진 구도에 대한 가이드 제공
    * 사진 cartoonize (convert image into a cartoon style)

### ✔️ 코드 리팩토링

### ✔️ 다양한 버그 수정

### ✔️ 속도 향상 
    * GPU 사용
    * multi threading
    

    
