import os

from keras.models import Model
from keras.layers import Dense, Dropout
from keras.optimizers import Adam
from keras.callbacks import ModelCheckpoint
from keras.applications.mobilenet import MobileNet
from keras import backend as K
from data_loader import train_generator, val_generator
import tensorflow as tf

image_size = 224

def earth_mover_loss(y_true, y_pred):
    cdf_ytrue = K.cumsum(y_true, axis=-1)
    cdf_ypred = K.cumsum(y_pred, axis=-1)
    samplewise_emd = K.sqrt(K.mean(K.square(K.abs(cdf_ytrue - cdf_ypred)), axis=-1))
    return K.mean(samplewise_emd)

if __name__ == "__main__":
    base_model = MobileNet((image_size, image_size, 3), alpha=1, include_top=False, pooling='avg')
    
    for layer in base_model.layers:
        layer.trainable = False

    x = Dropout(0.75)(base_model.output)
    x = Dense(10, activation='softmax')(x)
    model = Model(base_model.input, x)
    
    model.summary()
    model.compile(optimizer = Adam(lr=1e-3), loss=[earth_mover_loss], weighted_metrics=[earth_mover_loss])
    
    # load weights from trained model if it exists
    if os.path.exists('weights/mobilenet_weights.h5'):
        print("Load wights from trained model.")
        model.load_weights('weights/mobilenet_weights.h5')

    model_path = 'weights/mobilenet_weights_' + '{epoch:02d}_{val_loss:.4f}.hdf5'
    checkpoint = ModelCheckpoint(model_path, monitor='val_loss', verbose=1, save_weights_only=True, save_best_only=True, mode='min')
    callbacks = [checkpoint]

    #batchsize = 200
    batchsize = 10
    #epochs = 20
    epochs = 20

    model.fit_generator(train_generator(batchsize=batchsize),
                        #steps_per_epoch=(250000. // batchsize),
                        steps_per_epoch=(200. // batchsize),
                        epochs=epochs, verbose=1, callbacks=callbacks,
                        validation_data=val_generator(batchsize=batchsize),
                        #validation_steps=(5000. // batchsize))
                        validation_steps=(50. // batchsize))

    # Save tf.keras model in HDF5 format.
    keras_file = 'weights/mobilenet_model_keras.h5'
    tf.keras.models.save_model(model, keras_file)
    #model.save('weights/mobilenet_model.h5')
