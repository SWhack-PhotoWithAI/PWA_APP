# PWA_APP

## Photo With AI: AI and Server Part
Neural Image Assement Model과 Human Pose Evaluation Model을 사용 가능한 Server

###사용법
1. Colab에서 run_FlaskServer.ipynb 파일을 연다.
2. 실행시킨 후 Server상에서 제공하는 URL을 Copy한다.
![image](https://user-images.githubusercontent.com/38209962/107195805-2d415780-6a35-11eb-8750-7c17fd26b900.png)

3. 해당 URL에 원하는 기능에 Post 형식으로 Image을 보낸다.
![image](https://user-images.githubusercontent.com/38209962/107196040-72fe2000-6a35-11eb-8b1f-8bee5e3161d7.png)


###기능
##1. 배경 평가
다수의 이미지를 보내면 NIMA가 평가한 후 제일 Score가 좋은 이미지의 Index를 Return한다.
![image](https://user-images.githubusercontent.com/38209962/107196513-0cc5cd00-6a36-11eb-8a88-e2952b1f83b7.png)
![image](https://user-images.githubusercontent.com/38209962/107196659-3ed72f00-6a36-11eb-86e2-4923abd7a4df.png)

### url : {Server_URL} + /predict_background

##2. 인물 평가
다수의 이미지를 보내면 Human Pose Evaluation Model 평가한 후 제일 Score가 좋은 이미지의 Index를 Return한다.
![image](https://user-images.githubusercontent.com/38209962/107196857-83fb6100-6a36-11eb-8ac3-fbafc6bcbaf3.png)
![image](https://user-images.githubusercontent.com/38209962/107196950-a2615c80-6a36-11eb-915a-c825cac156a4.png)


### url : {Server_URL} + /predict_person

##3. 필터 적용
단일 이미지를 보내면 필터 적용 후, 적용된 이미지를 Return한다. (ainize api 사용)
![image](https://user-images.githubusercontent.com/38209962/107197320-156ad300-6a37-11eb-8a39-92d71f063e23.png)
![image](https://user-images.githubusercontent.com/38209962/107197367-24518580-6a37-11eb-9ffb-bd1a772142a8.png)


### url : {Server_URL} + /cartoonization
