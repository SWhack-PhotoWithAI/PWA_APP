# keras
from keras.models import Model
from keras.layers import Dense, Dropout
from keras.optimizers import Adam
from keras.callbacks import ModelCheckpoint
#from keras.preprocessing.image import load_img, img_to_array
from keras.applications.mobilenet import MobileNet
#from keras.applications.mobilenet import preprocess_input as mobilenet_preprocess_input
from keras.applications.nasnet import NASNetMobile
#from keras.applications.nasnet import preprocess_input as nasnet_preprocess_input
from keras.applications.inception_resnet_v2 import InceptionResNetV2
#from keras.applications.inception_resnet_v2 import preprocess_input as inception_resnet_preprocess_input
from data_loader import train_generator, val_generator

from keras import backend as K
import tensorflow as tf

## backend
#import numpy as np

## NIMA util
#from score_utils import mean_score, std_score

# etc
import argparse
import os
#import glob
#from tqdm import tqdm

def get_argument_parser():
    parser = argparse.ArgumentParser(description='Evaluate NIMA(MobileNet, NasNet, InceptionResNet)')
    parser.add_argument('-network', type=str, default='MobileNet', help='The network to use.(MobileNet, NasNet, InceptionResNet)')
    parser.add_argument('-weights', type=str, default=None, help='Pass a file name for save trained weight')
    parser.add_argument('-model', type=str, default=None, help='Pass a file name for save trained weight')
    return parser

def parse_argument(args):
    ret = {}
    ret['network'] = args.network if args.network in ('MobileNet', 'NasNet', 'InceptionResNet') else None
    ret['weights'] = args.weights
    ret['model'] = args.model

    # Print configs
    print("network    :", ret['network'])
    print("weights    :", ret['weights'])
    print("model      :", ret['model'])

    return ret

def earth_mover_loss(y_true, y_pred):
    cdf_ytrue = K.cumsum(y_true, axis=-1)
    cdf_ypred = K.cumsum(y_pred, axis=-1)
    samplewise_emd = K.sqrt(K.mean(K.square(K.abs(cdf_ytrue - cdf_ypred)), axis=-1))
    return K.mean(samplewise_emd)

def set_model(network):
    print("[Build model]")

    if network == 'MobileNet':
        print("Use MobileNet")
        base_model = MobileNet((None, None, 3), alpha=1, include_top=False, pooling='avg', weights=None)

    elif network == 'NasNet':
        print("Use NasNet")
        base_model = NASNetMobile((224, 224, 3), include_top=False, pooling='avg', weights=None)

    elif network == 'InceptionResNet':
        print("Use InceptionResNet")
        base_model = InceptionResNetV2(input_shape=(None, None, 3), include_top=False, pooling='avg', weights=None)

    else:
        raise RuntimeError('Either -network arguments must be passed as argument')
    
    x = Dropout(0.75)(base_model.output)
    x = Dense(10, activation='softmax')(x)
    model = Model(base_model.input, x)
    model.summary()
    model.compile(optimizer = Adam(lr=1e-3), loss=[earth_mover_loss], weighted_metrics=[earth_mover_loss])

    # load weights from trained model if it exists
    if os.path.exists('weights/mobilenet_weights.h5'):
        print("Load wights from trained model.")
        model.load_weights('weights/mobilenet_weights.h5')
    
    return model

def train_model(model, wights_output, model_output):
    checkpoint = ModelCheckpoint(model_output, monitor='val_loss', verbose=1, save_weights_only=True, save_best_only=True, mode='min')
    callbacks = [checkpoint]

    batchsize = 200
    epochs = 20

    model.fit_generator(train_generator(batchsize=batchsize),
                        steps_per_epoch=(250000. // batchsize),
                        epochs=epochs, verbose=1, callbacks=callbacks,
                        validation_data=val_generator(batchsize=batchsize),
                        validation_steps=(5000. // batchsize))

    # Save tf.keras weiths in HDF5 format.
    if wights_output is not None:
        model.save_weights(wights_output)

    # Save tf.keras model in HDF5 format.
    if model_output is not None:
        tf.keras.models.save_model(model, model_output)

if __name__ == "__main__":
    parser = get_argument_parser()    
    config = parse_argument(parser.parse_args())
    model = set_model(config['network'])
    train_model(model, config['weights'], config['model'])

    #score_list = prediction_score(model, config['network'], images, config['target_size'])

    #rank_list = ranking_score(score_list)
