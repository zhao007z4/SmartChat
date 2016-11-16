#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include "sns.h"
#include "sys/system_properties.h"

#define ENCODE_KEY	36172
#define PROPERTY_KEY_MAX   32
#define PROPERTY_VALUE_MAX  92

typedef int int32;
typedef unsigned short uint16;
static char acTmpbuf[1024] = {0};

int loglevel = ANDROID_LOG_WARN;
//Simple verification.if haven't "isDebug",stop
jboolean bExist = JNI_TRUE;
jclass  clsString;
static char* pBrand=NULL;
static char* pProduct=NULL;
static char* pAes =NULL;
static char* pmmName =NULL;

#define LOG    "sns" // 这个是自定义的LOG的标识
//#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
//#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__) // 定义LOGI类型
//#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
//#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型
//#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__) // 定义LOGF类型


static char* pKeyDecode =NULL;
const char key[19] ={0x00,0x10,0xD9,0x27,0x77,0x88,0x8F,0x21,0xD0,0x2F,0x81,0x28,0x2C,0xDD,0xD0,0xD5,0x2D,0xD5,0x0};
const char path[26]= {0x00,0x17,0x9C,0x7C,0x74,0x86,0x81,0x73,0x96,0x30,0xAD,0x48,0x41,0xAF,0xB3,0x96,0x55,0xBE,0xF7,0xBA,0xBB,0xFB,0xB8,0xBC,0xB5,0x0};

extern char* jniParseKey();
void log_android(int prio, const char *fmt, ...) {
//    if (prio >= loglevel) {
//        char line[1024];
//        va_list argptr;
//        va_start(argptr, fmt);
//        vsprintf(line, fmt, argptr);
//        __android_log_print(prio, LOG, line);
//        va_end(argptr);
//    }
}

jobject jniGlobalRef(JNIEnv *env, jobject cls) {
    jobject gcls = (*env)->NewGlobalRef(env, cls);
    if (gcls == NULL)
        log_android(ANDROID_LOG_ERROR, "Global ref failed (out of memory?)");
    return gcls;
}

jclass jniFindClass(JNIEnv *env, const char *name) {
    jclass cls = (*env)->FindClass(env, name);
    if (cls == NULL)
        log_android(ANDROID_LOG_ERROR, "Class %s not found", name);
    else
        jniCheckException(env);
    return cls;
}

jmethodID jniGetMethodID(JNIEnv *env, jclass cls, const char *name, const char *signature) {
    jmethodID method = (*env)->GetMethodID(env, cls, name, signature);
    if (method == NULL) {
        log_android(ANDROID_LOG_ERROR, "Method %s %s not found", name, signature);
        jniCheckException(env);
    }
    return method;
}

jfieldID jniGetFieldID(JNIEnv *env, jclass cls, const char *name, const char *type) {
    jfieldID field = (*env)->GetFieldID(env, cls, name, type);
    if (field == NULL)
        log_android(ANDROID_LOG_ERROR, "Field %s type %s not found", name, type);
    return field;
}

jobject jniNewObject(JNIEnv *env, jclass cls, jmethodID constructor, const char *name) {
    jobject object = (*env)->NewObject(env, cls, constructor);
    if (object == NULL)
        log_android(ANDROID_LOG_ERROR, "Create object %s failed", name);
    else
        jniCheckException(env);
    return object;
}

int jniCheckException(JNIEnv *env) {
    jthrowable ex = (*env)->ExceptionOccurred(env);
    if (ex) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        (*env)->DeleteLocalRef(env, ex);
        return 1;
    }
    return 0;
}

jstring c2js(JNIEnv* env, const char* pat)
{
    if(pat==NULL)
    {
        return NULL;
    }
    jclass strClass = (*env)->FindClass(env,"java/lang/String");
    jmethodID ctorID = (*env)->GetMethodID(env,strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env,strlen(pat));
    (*env)->SetByteArrayRegion(env,bytes, 0, strlen(pat), (jbyte*)pat);
    jstring encoding = (*env)->NewStringUTF(env,"utf-8");
    return (jstring)(*env)->NewObject(env,strClass, ctorID, bytes, encoding);
}

char* Js2c(JNIEnv* env, jstring jstr)
{
    if(jstr==NULL)
    {
        return NULL;
    }

    char* rtn = NULL;
    jclass clsstring = (*env)->FindClass(env,"java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env,"utf8");

    jmethodID mid = (*env)->GetMethodID(env,clsstring,"getBytes","(Ljava/lang/String;)[B");

    jbyteArray barr=(jbyteArray)(*env)->CallObjectMethod(env,jstr,mid,strencode);

    jsize alen = (*env)->GetArrayLength(env,barr);
    jbyte* ba = (*env)->GetByteArrayElements(env,barr,JNI_FALSE);

    if(alen > 0)
    {
        rtn = (char*)malloc(alen+1); //"\0"
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }

    (*env)->ReleaseByteArrayElements(env,barr,ba,0); //
    return rtn;
}


//char *get_property(char *name)
//{
//    char prop[PROPERTY_VALUE_MAX] ={0};
//    if(__system_property_get(name, prop) != 0) {
//        return prop;
//    }
//    return NULL;
//}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    log_android(ANDROID_LOG_INFO, "JNI load");

    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        log_android(ANDROID_LOG_INFO, "JNI load GetEnv failed");
        return -1;
    }


    const char *common = "com/zyx/smarttouch/common/Aes";
    jclass clsCommon = (*env)->FindClass(env, common);
    if(clsCommon==NULL)
    {
        bExist =JNI_FALSE;
    }
    else
    {
        jfieldID idCommon = (*env)->GetStaticMethodID(env,clsCommon,"isRegedit","()Z");
        if(idCommon!=NULL)
        {
            bExist =(*env)->CallStaticBooleanMethod(env,clsCommon,idCommon);
        }
        else
        {
            bExist = JNI_FALSE;
        }

        idCommon = (*env)->GetStaticMethodID(env,clsCommon,"getBrand","()Ljava/lang/String;");
        if(idCommon!=NULL) {
            jstring sBrand = (*env)->CallStaticObjectMethod(env, clsCommon, idCommon);
            if (sBrand != NULL)
            {
                pBrand = Js2c(env,sBrand);
            }
        }
        else
        {
            pBrand = NULL;
        }

        idCommon = (*env)->GetStaticMethodID(env,clsCommon,"getProduct","()Ljava/lang/String;");
        if(idCommon!=NULL)
        {
            jstring sProduct =(*env)->CallStaticObjectMethod(env,clsCommon,idCommon);
            if (sProduct != NULL)
            {
                pProduct = Js2c(env,sProduct);
            }
        }
        else
        {
            pProduct = NULL;
        }
    }

    jclass  clsString = (*env)->FindClass(env,"java/lang/String");
    return JNI_VERSION_1_6;
}

jboolean string_equals(JNIEnv *env,jstring sSrc,jstring sDes)
{
    if (env==NULL || clsString==NULL)
    {
        return JNI_FALSE;
    }

    jclass cls = (*env)->GetObjectClass(env,sSrc);
    jmethodID mid= (*env)->GetMethodID(env,cls,"compareTo", "(Ljava/lang/String;)I");
    if (mid==0){
       return JNI_FALSE;
    }

    jint z=(*env)->CallIntMethod(env,sSrc,mid,sDes);
    if(z==0)
    {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    log_android(ANDROID_LOG_INFO, "JNI unload");

    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK)
        log_android(ANDROID_LOG_INFO, "JNI load GetEnv failed");
    else {

    }
}

jstring jni_md5(JNIEnv *env,jstring sData)
{
    if(sData==NULL || !bExist)
    {
        return NULL;
    }

    const char *aes = "com/zyx/smarttouch/common/Aes";
    jclass  clsAes = jniFindClass(env, aes);
    if(clsAes !=NULL) {

        jmethodID idCommon = (*env)->GetStaticMethodID(env,clsAes,"md5","(Ljava/lang/String;)Ljava/lang/String;");
        if(idCommon!=NULL) {
            return (*env)->CallStaticObjectMethod(env, clsAes, idCommon,sData);
        }
    }
    return NULL;
}

//编码
jstring encrypt(JNIEnv *env,jstring sData)
{
    if(sData==NULL || !bExist)
    {
        return NULL;
    }

    char *cDataKey = jniParseKey();
    if(cDataKey==NULL)
    {
        return NULL;
    }

    jstring  sDataKey = (*env)->NewStringUTF(env,cDataKey);
    if(sDataKey !=NULL) {

        const char *aes = "com/zyx/smarttouch/common/Aes";
        jclass  clsAes = jniFindClass(env, aes);
        if(clsAes !=NULL)
        {
            jmethodID encryptFunc = (*env)->GetStaticMethodID(env, clsAes, "encrypt",
                                                    "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
            if(encryptFunc !=NULL)
            {
                jstring sDesData = (*env)->CallStaticObjectMethod(env,clsAes, encryptFunc, sDataKey, sData);
                (*env)->DeleteLocalRef(env, sDataKey);
                return sDesData;
            }
        }
    }
    return NULL;

}

//解码
jstring decrypt(JNIEnv *env,jstring sData)
{
    if(sData==NULL  || !bExist)
    {
        return NULL;
    }

    char *cDeskey = jniParseKey();
    if (cDeskey != NULL) {
        jstring sNewKey = (*env)->NewStringUTF(env, cDeskey);

        const char *aes = "com/zyx/smarttouch/common/Aes";
        jclass clsAes = jniFindClass(env, aes);
        if(clsAes !=NULL)
        {
            jmethodID decryptFunc = (*env)->GetStaticMethodID(env, clsAes, "decrypt",
                                                    "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
            if(decryptFunc !=NULL)
            {
                jstring sNewData = (*env)->CallStaticObjectMethod(env,clsAes, decryptFunc, sNewKey, sData);
                (*env)->DeleteLocalRef(env, sNewKey);
                return sNewData;
            }
        }
    }


    return NULL;
}

static int maxi_GetEncodeKey(void)
{
    return ENCODE_KEY;
}

static char* maxi_DecodeString(const char *ori_str, char *ret_str, int32 ring)
{
	uint16 len = 0;
	int32 mask = 0;
	char *p = ret_str;
	int32 seed = 0;
	uint16 times = 0;

	if(NULL == ori_str)
		return NULL;

	len = (uint16)((ori_str[0]) << 8) | (ori_str[1] & 0xFF);	//获取行首,字符串长度
	ori_str +=2;											    //++or_str偏移掉行首不处理
	seed = len;

	if(NULL == ret_str)
		return NULL;

	p = ret_str;
	ret_str[len] = 0;

	for(times = 0; times < len; ++times)
	{
		mask++;
		mask %= (sizeof(int32) *8);
		*p++ = *ori_str++ ^ (ring & (1<<mask) ? (++seed) : -(++seed));
	}

	return ret_str;
}


char* jniParseKey()
{
    int iLen = 0;
    if(pKeyDecode==NULL)
    {
        memset(acTmpbuf,0,sizeof(acTmpbuf));
        maxi_DecodeString(key, acTmpbuf, maxi_GetEncodeKey());
        iLen = strlen(acTmpbuf);
        pKeyDecode = (char *)malloc(iLen+1);

        memset(pKeyDecode,0,iLen+1);
        strcpy(pKeyDecode,acTmpbuf);
    }

    return pKeyDecode;
}



jstring jni_get_imei(JNIEnv *env, jobject mContext)
{
    if(mContext == 0){
        return NULL;
    }
    jclass cls_context = (*env)->FindClass(env, "android/content/Context");
    if(cls_context == 0){
        return NULL;
    }
    jmethodID getSystemService = (*env)->GetMethodID(env, cls_context, "getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
    if(getSystemService == 0){
        return NULL;
    }
    jfieldID TELEPHONY_SERVICE = (*env)->GetStaticFieldID(env, cls_context, "TELEPHONY_SERVICE", "Ljava/lang/String;");
    if(TELEPHONY_SERVICE == 0){
        return NULL;
    }
    jstring str = (*env)->GetStaticObjectField(env, cls_context, TELEPHONY_SERVICE);
    jobject telephonymanager = (*env)->CallObjectMethod(env, mContext, getSystemService, str);
    if(telephonymanager == 0){
        return NULL;
    }
    jclass cls_tm = (*env)->FindClass(env, "android/telephony/TelephonyManager");
    if(cls_tm == 0){
        return NULL;
    }
    jmethodID getDeviceId = (*env)->GetMethodID(env, cls_tm, "getDeviceId", "()Ljava/lang/String;");
    if(getDeviceId == 0){
        return NULL;
    }
    return (*env)->CallObjectMethod(env, telephonymanager, getDeviceId);
}

void jni_init(JNIEnv *env,jobject mContext)
{
    char *pKey = jniParseKey();

    memset(acTmpbuf,0,sizeof(acTmpbuf));

    jstring sImei = jni_get_imei(env,mContext);
    char *pImei = Js2c(env,sImei);
    if(pImei==NULL);
    {
        strcpy(acTmpbuf,pImei);
        free(pImei);
        pImei =NULL;
    }

    if(pKey !=NULL)
    {
        strcat(acTmpbuf,pKey);
    }

    if(pBrand !=NULL)
    {
        strcat(acTmpbuf,pBrand);
        free(pBrand);
        pBrand=NULL;
    }

    if(pProduct !=NULL)
    {
        strcat(acTmpbuf,pProduct);
        free(pProduct);
        pProduct=NULL;
    }

    jstring  sAes =c2js(env,acTmpbuf);
    sAes = encrypt(env,sAes);
    sAes = jni_md5(env,sAes);
    pAes = Js2c(env,sAes);
}

FILE *jni_open(JNIEnv *env,jstring sDir,jboolean  bWrite)
{
    FILE *fd;

    if(pmmName==NULL)
    {
        int iLen = 0;
        memset(acTmpbuf,0,sizeof(acTmpbuf));
        maxi_DecodeString(path, acTmpbuf, maxi_GetEncodeKey());
        iLen = strlen(acTmpbuf);
        pmmName = (char *)malloc(iLen+1);

        memset(pmmName,0,iLen+1);
        strcpy(pmmName,acTmpbuf);
    }

    char *pPath = Js2c(env,sDir);
    memset(acTmpbuf,0, sizeof(acTmpbuf));
    strcpy(acTmpbuf,pPath);
    free(pPath);
    strcat(acTmpbuf,pmmName);

    if(bWrite)
    {
        fd = fopen(acTmpbuf,"wb+");
    }
    else
    {
        fd = fopen(acTmpbuf,"rb");
    }


    return fd;
}

void jni_write(JNIEnv *env,jstring sDir,char* pdata)
{
    FILE *fd =jni_open(env,sDir,JNI_TRUE);
    if(fd!=NULL)
    {
        fwrite(pdata,strlen(pdata),1, fd);
        fclose(fd);
    }
}

void jni_read(JNIEnv *env,jstring sDir,char *pData,int iLen)
{
    FILE *fd =jni_open(env,sDir,JNI_FALSE);
    if(fd!=NULL)
    {
        fread(pData,iLen,1, fd);
        fclose(fd);
    }
}

JNIEXPORT jboolean JNICALL Java_com_zyx_smarttouch_gui_SnsApp_jni_1start(JNIEnv *env, jobject instance,jstring sData,jobject mContext,jstring sDir)
{
    if(pAes==NULL)
    {
        jni_init(env,mContext);
    }

    char* pdata = Js2c(env,sData);
    if(strcmp(pAes,pdata)==0)
    {
        jni_write(env,sDir,pdata);
        free(pdata);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_zyx_smarttouch_gui_SnsApp_jni_1startsys(JNIEnv *env, jobject instance,jobject mContext,jstring sDir)
{
    if(pAes==NULL)
    {
        jni_init(env,mContext);
    }

    log_android(ANDROID_LOG_ERROR, pAes);
    char acTmp[512]={0};
    jni_read(env,sDir,acTmp,1024);
    if(strcmp(pAes,acTmp)==0)
    {
       // jni_write(env,sDir,acTmp);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

//JNIEXPORT jstring JNICALL Java_com_zyx_smarttouch_gui_SnsApp_jniRequest_1data(JNIEnv *env, jobject instance,jstring sData)
//{
//    return encrypt(env,sData);
//}

//JNIEXPORT jstring JNICALL Java_com_zyx_smarttouch_common_gui_SnsApp_jniParse_1data(JNIEnv *env, jobject instance,jstring sData)
//{
//    return decrypt(env,sData);
//}






