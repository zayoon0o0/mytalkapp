//
// Created by Zain Abudaqqa on 7/28/25.
//
#include <jni.h>
#include <string.h>
#include "chat.h"
#include <pthread.h>
#include <malloc.h>
#include <android/log.h>
#define LOG_TAG "NativeListUsers"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


int extern sock;
char extern name;


void receive_text(void *arg, void *activity_ptr, char *msg, int length) {
    if (length <= 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Invalid length: %d", length);
        return;
    }
    JNIEnv* env = (JNIEnv*)arg;
    jobject activityObj = *(jobject*)activity_ptr;
    jclass activityClass = (*env)->GetObjectClass(env, activityObj);
    jmethodID mid = (*env)->GetMethodID(env, activityClass, "receiveText", "([B)V");
    if (mid == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Method ID not found");
        return;
    }
    jbyteArray javaMessage = (*env)->NewByteArray(env, length);
    if (javaMessage == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to allocate byte array");
        return;
    }
    (*env)->SetByteArrayRegion(env, javaMessage, 0, length, (const jbyte*)msg);
    (*env)->CallVoidMethod(env, activityObj, mid, javaMessage);
    (*env)->DeleteLocalRef(env, javaMessage);
    (*env)->DeleteLocalRef(env, activityClass);
    receive(receive_text, env, &activityObj);
}
JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ChatActivity_recieve_1message(JNIEnv *env, jobject thiz) {
    jobject global_thiz = (*env)->NewGlobalRef(env, thiz);
    receive(receive_text, env, &global_thiz);
    (*env)->DeleteGlobalRef(env, global_thiz);
}

JNIEXPORT void JNICALL
Java_com_myapps_mytalk_DMActivity_recieve_1message(JNIEnv *env, jobject thiz) {
    jobject global_thiz = (*env)->NewGlobalRef(env, thiz);
    receive(receive_text, env, &global_thiz);
    (*env)->DeleteGlobalRef(env, global_thiz);
}


JNIEXPORT void JNICALL
Java_com_myapps_mytalk_MainActivity_connectToServer(JNIEnv* env, jobject obj,
                                                    jbyteArray username) {
    const uint8_t length = (*env)->GetArrayLength(env, username);
    const char* useername = (*env)->GetByteArrayElements(env, username, NULL);
    connect_to_server(useername, length);
//    receive(receive_text, env);
}

JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ChatActivity_connectToServer(JNIEnv* env, jobject obj,
                                                    jbyteArray username) {
    const uint8_t length = (*env)->GetArrayLength(env, username);
    const char* useername = (*env)->GetByteArrayElements(env, username, NULL);
    connect_to_server(useername, length);
    receive(receive_text, env, &obj);
}


JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ChatActivity_send_1message(JNIEnv *env, jobject thiz, jbyteArray message) {
    if (sock < 0) {
        connect_to_server((const char *) name, strlen((const char *const) name));
        return;
    }
    int length = (*env)->GetArrayLength(env, message);
    jbyte* bytes = (*env)->GetByteArrayElements(env, message, NULL);
    if (bytes == NULL) return;
    send_message((const char*)bytes, length);
    (*env)->ReleaseByteArrayElements(env, message, bytes, JNI_ABORT);
    jobject global_thiz = (*env)->NewGlobalRef(env, thiz);
    receive(receive_text, env, &global_thiz);
}


JNIEXPORT void JNICALL
Java_com_myapps_mytalk_DMActivity_send_1primessage(JNIEnv *env, jobject thiz, jbyteArray message) {
    if (sock < 0) {
        connect_to_server((const char *) name, strlen((const char *const) name));
        return;
    }
    int length = (*env)->GetArrayLength(env, message);
    jbyte* bytes = (*env)->GetByteArrayElements(env, message, NULL);
    if (bytes == NULL) return;
    send_privitemessage((const char*)bytes, length);
    (*env)->ReleaseByteArrayElements(env, message, bytes, JNI_ABORT);
    jobject global_thiz = (*env)->NewGlobalRef(env, thiz);
    receive(receive_text, env, &global_thiz);
}

JNIEXPORT jobjectArray JNICALL
Java_com_myapps_mytalk_ListActivity_listUsers(JNIEnv *env, jobject thiz) {
    char *user_list = list_users_raw();
    if (!user_list) return NULL;
    int count = 0;
    for (char *p = user_list; *p; p += strlen(p) + 1) {
        count++;
    }
    LOGI("listUsers called, count=%d", count);
    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray userArray = (*env)->NewObjectArray(env, count, stringClass, NULL);
    int idx = 0;
    for (char *p = user_list; *p; p += strlen(p) + 1) {
        jstring userStr = (*env)->NewStringUTF(env, p);
        (*env)->SetObjectArrayElement(env, userArray, idx++, userStr);
    }
    free(user_list);
    return userArray;
}


JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ListActivity_send_1message(JNIEnv *env, jobject thiz, jbyteArray message) {
    if (sock < 0) {
        connect_to_server((const char *) name, strlen((const char *const) name));
        return;
    }
    int length = (*env)->GetArrayLength(env, message);
    jbyte* bytes = (*env)->GetByteArrayElements(env, message, NULL);
    if (bytes == NULL) return;
    send_message((const char*)bytes, length);
    (*env)->ReleaseByteArrayElements(env, message, bytes, JNI_ABORT);
    jobject global_thiz = (*env)->NewGlobalRef(env, thiz);
    receive(receive_text, env, &global_thiz);
}

JNIEXPORT void JNICALL
Java_com_myapps_mytalk_DMActivity_send_1message(JNIEnv *env, jobject thiz, jbyteArray message) {
    if (sock < 0) {
        connect_to_server((const char *) name, strlen((const char *const) name));
        return;
    }
    int length = (*env)->GetArrayLength(env, message);
    jbyte* bytes = (*env)->GetByteArrayElements(env, message, NULL);
    if (bytes == NULL) return;
    send_message((const char*)bytes, length);
    (*env)->ReleaseByteArrayElements(env, message, bytes, JNI_ABORT);
    jobject global_thiz = (*env)->NewGlobalRef(env, thiz);
    receive(receive_text, env, &global_thiz);
}


JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ConnService_close(JNIEnv *env, jobject thiz) {
    closesock();
}


JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ConnService_recieveMessage(JNIEnv *env, jobject thiz) {
    receive(receive_text, env, &thiz);
}



JNIEXPORT void JNICALL
Java_com_myapps_mytalk_ConnService_connectToServer(JNIEnv *env, jobject thiz, jbyteArray username) {
    const uint8_t length = (*env)->GetArrayLength(env, username);
    const char* useername = (*env)->GetByteArrayElements(env, username, NULL);
    connect_to_server(useername, length);
    receive(receive_text, env, &thiz);
}

