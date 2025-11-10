#include <jni.h>
#include <string>

#include "ncnn/include/ncnn/net.h"

static ncnn::Net clip;
static ncnn::Net unet;
static ncnn::Net vae;

#define LOG_TAG "StableDiffusionJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static bool inited = false;

extern "C" JNIEXPORT jboolean JNICALL
Java_in_co_gorest_grblcontroller_StableDiffusionNcnn_init(JNIEnv *env, jobject thiz, jstring model_dir) {
    const char *modelDir = env->GetStringUTFChars(model_dir, 0);

    int ret = 0;
    ret |= clip.load_param((std::string(modelDir) + "/FrozenCLIPEmbedder-fp16.param").c_str());
    ret |= clip.load_model((std::string(modelDir) + "/FrozenCLIPEmbedder-fp16.bin").c_str());

    ret |= unet.load_param((std::string(modelDir) + "/UNetModel-256-MHA-fp16-opt.param").c_str());
    ret |= unet.load_model((std::string(modelDir) + "/UNetModel-MHA-fp16.bin").c_str());

    ret |= vae.load_param((std::string(modelDir) + "/AutoencoderKL-256-fp16-opt.param").c_str());
    ret |= vae.load_model((std::string(modelDir) + "/AutoencoderKL-fp16.bin").c_str());

    env->ReleaseStringUTFChars(model_dir, modelDir);

    inited = (ret == 0);
    return inited ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_in_co_gorest_grblcontroller_StableDiffusionNcnn_generateImage(JNIEnv *env, jobject thiz, jstring prompt) {
    if (!inited) return nullptr;

    // 1. 文本编码 (clip)
    // 2. 执行 unet 潜空间推理
    // 3. vae 解码为图片

    // 下面返回空数组，示范写法，实际你需要实现推理过程
    int image_size = 0;
    jbyteArray imageBytes = env->NewByteArray(image_size);
    // env->SetByteArrayRegion(imageBytes, 0, image_size, data_pointer);
    return imageBytes;
}

extern "C" JNIEXPORT void JNICALL
Java_in_co_gorest_grblcontroller_StableDiffusionNcnn_release(JNIEnv *env, jobject thiz) {
    clip.clear();
    unet.clear();
    vae.clear();
    inited = false;
}