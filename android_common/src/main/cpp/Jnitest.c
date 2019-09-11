#include <string.h>
#include <jni.h>
#include <sys/stat.h>
#include <dirent.h>

JNIEXPORT void JNICALL com_example_common_FileSystemHelp_scanDirectoriesWithCC(JNIEnv *env, jobject obj,
        jstring directory){
    DIR* dir = opendir(directory);
}