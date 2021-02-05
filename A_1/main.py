import cv2
import torch
import torchvision
from torchvision import models
import torchvision.transforms as T

import numpy as np
from PIL import Image
import matplotlib.pyplot as plt
from matplotlib.path import Path
import matplotlib.patches as patches
#사이즈 지정
IMG_SIZE = 480
THRESHOLD = 0.95

#모델 가져오기 ( 다운로드 받아둠)
model = models.detection.keypointrcnn_resnet50_fpn(pretrained=True).eval()
IMG_SIZE = 480
THRESHOLD = 0.95

#img load
img = Image.open('img_data.jpg')
img = img.resize((IMG_SIZE, int(img.height * IMG_SIZE / img.width)))

plt.figure(figsize=(16, 16))
trf = T.Compose([
    T.ToTensor()
])

input_img = trf(img)
out = model([input_img])[0]
#박스, 라벨, 점수, 키포인트 키포인트 스코어 점수 가 out에 딕셔너리로 저장.

codes = [
    Path.MOVETO,
    Path.LINETO,
    Path.LINETO
]

fig, ax = plt.subplots(1, figsize=(16, 16))
ax.imshow(img)

for box, score, keypoints in zip(out['boxes'], out['scores'], out['keypoints']):
    score = score.detach().numpy()

    if score < THRESHOLD:
        continue

    box = box.detach().numpy()
    keypoints = keypoints.detach().numpy()[:, :2]

    rect = patches.Rectangle((box[0], box[1]), box[2] - box[0], box[3] - box[1], linewidth=2, edgecolor='b',
                             facecolor='none')
    ax.add_patch(rect)

    # 17 keypoints
    for k in keypoints:
        circle = patches.Circle((k[0], k[1]), radius=2, facecolor='r')
        ax.add_patch(circle)

    # 경로 나타내기
    # left arm
    path = Path(keypoints[5:10:2], codes)
    line = patches.PathPatch(path, linewidth=2, facecolor='none', edgecolor='r')
    ax.add_patch(line)

    # right arm
    path = Path(keypoints[6:11:2], codes)
    line = patches.PathPatch(path, linewidth=2, facecolor='none', edgecolor='r')
    ax.add_patch(line)

    # left leg
    path = Path(keypoints[11:16:2], codes)
    line = patches.PathPatch(path, linewidth=2, facecolor='none', edgecolor='r')
    ax.add_patch(line)

    # right leg
    path = Path(keypoints[12:17:2], codes)
    line = patches.PathPatch(path, linewidth=2, facecolor='none', edgecolor='r')
    ax.add_patch(line)



#print(input_img.shape)



#안드로이드에서 카메라 켜졌을때 다음과 같은 알고리즘으로 동작
# while cap.isOpened():
#     ret, img = cap.read()
#
#     img = cv2.resize(img, (IMG_SIZE, int(img.shape[0] * IMG_SIZE / img.shape[1])))
#     img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
#
#     trf = T.Compose([
#         T.ToTensor()
#     ])
#
#     input_img = trf(img)
#
#     out = model([input_img])[0]
#
#     for box, score, keypoints in zip(out['boxes'], out['scores'], out['keypoints']):
#         score = score.detach().numpy()
#
#         if score < THRESHOLD:
#             continue
#
#         box = box.detach().numpy()
#         keypoints = keypoints.detach().numpy()[:, :2]
#
#         cv2.rectangle(img, pt1=(int(box[0]), int(box[1])), pt2=(int(box[2]), int(box[3])), thickness=2,
#                       color=(0, 0, 255))
#
#         for k in keypoints:
#             cv2.circle(img, center=tuple(k.astype(int)), radius=2, color=(255, 0, 0), thickness=-1)
#
#         cv2.polylines(img, pts=[keypoints[5:10:2].astype(int)], isClosed=False, color=(255, 0, 0), thickness=2)
#         cv2.polylines(img, pts=[keypoints[6:11:2].astype(int)], isClosed=False, color=(255, 0, 0), thickness=2)
#         cv2.polylines(img, pts=[keypoints[11:16:2].astype(int)], isClosed=False, color=(255, 0, 0), thickness=2)
#         cv2.polylines(img, pts=[keypoints[12:17:2].astype(int)], isClosed=False, color=(255, 0, 0), thickness=2)
#
#     img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
#
#     out_video.write(img)
#
#     cv2.imshow('result', img)
#     if cv2.waitKey(1) == ord('q'):
#         break
#
# out_video.release()
# cap.release()