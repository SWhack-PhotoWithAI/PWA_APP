#run Flask WebServer

#Vision Module
from NIMA.evaluate_NIMA import evaluate
from Human.face_based import main
from Human.rt_eval import main as rt_main
#Flask 

from flask_ngrok import run_with_ngrok
# from flask import Flask
from flask import Flask, render_template, request, jsonify
from flask import send_file
from werkzeug.utils import secure_filename

import json
import os
import shutil
import requests
import glob


def get_image_list(image_dir):
    if image_dir is not None:
        print("Loading images from directory : ", image_dir)
        images = glob.glob(image_dir + '/*.png')
        images += glob.glob(image_dir + '/*.jpg')
        images += glob.glob(image_dir + '/*.jpeg')

    else:
        raise RuntimeError('Either -img_dir arguments must be passed as argument')

    return images


if 'images' not in os.listdir():
  os.mkdir('images')
  
else:
  imgDir_path = 'images/'
  img_list = get_image_list(imgDir_path)
  print(img_list)
  for img_path in img_list:
    os.remove(img_path)
    
    

app = Flask(__name__)
run_with_ngrok(app)   #starts ngrok when the app is run



@app.route('/predict_background', methods = ['POST'])
def predict_background(): #image 파일은 post형식으로 넘어오기 때문에
    #제일 score값이 좋은 image name을 return 하면 될듯.
    img_dir = 'images/'
    files = request.files.getlist('image') #이미지 여러개 받아오도록 해야함
    order_dict = {}
    index = 0
    for file in files:
        img_path = img_dir + secure_filename(file.filename)
        order_dict[img_path] = index
        index += 1 
        file.save(img_path)
    
    result = evaluate()
    result_index = order_dict[result['name']]

    _dict = {}
    _dict['index'] = result_index
    return _dict

@app.route('/predict_person', methods = ['POST'])
def predict_person():
    img_dir = 'images/'
    files = request.files.getlist('image') #이미지 여러개 받아오도록 해야함
    
    order_dict = {}
    index = 0
    for file in files:
        img_path = img_dir + secure_filename(file.filename)
        order_dict[img_path] = index
        index += 1
        file.save(img_path)

    result = main()
    best_img = ''
    max_score = -1
    for img_name, score in result.items():
        if score > max_score:
            best_img = img_name
            max_score = score

    result_index = order_dict[best_img]

        
    _dict = {}
    _dict['index'] = result_index
    
    return _dict

@app.route('/predict_person_rt', methods = ['POST'])
def predict_person_rt():
    img_dir = 'images/'
    file = request.files.getlist('image')[0] #이미지 한 개 받아옴
    
   
    img_path = img_dir + secure_filename(file.filename)
    file.save(img_path)

    result = rt_main()
    os.remove(img_path)
    
    _dict = {}
    _dict['sen'] = result
   
    return _dict

@app.route('/cartoonization', methods = ['POST'])
def cartoonization():
    img_paths = []
    img_dir = 'images/'
    file = request.files.get('source') #이미지 여러개 받아오도록 해야함
    img_path = img_dir + secure_filename(file.filename)
    print(img_path)
    file.save(img_path)

    #return single image
    path = img_dir + 'test.jpg'
    url = 'https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict'
    _file = [
              ('source', (img_path, open(img_path, 'rb'), 'image/jpg'))
    ]
    _data = {'file_type' : 'image' }
    _response = requests.post(url ,data = _data, files=_file)
    print(_response)
    if _response.status_code == 200:
        with open(path, 'wb') as f:
            f.write(_response.content)
            f.close()

        return send_file(path, mimetype = 'image/jpeg')

    _dict = {}
    _dict['error'] = 'True'
    return _dict

@app.route("/")
def home():
    return "<h1>Running Flask on Google Colab!</h1>"
  
app.run()
