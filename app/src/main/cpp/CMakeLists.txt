cmake_minimum_required(VERSION 3.10.2)
project("sd")

add_library(sd SHARED
        stable_diffusion_jni.cpp
)

# 设置 include 路径（指到包含 ncnn/ 的那层）
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/ncnn/include)

# 引入 libncnn.so
add_library(ncnn SHARED IMPORTED)
set_target_properties(ncnn PROPERTIES
        IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/ncnn/lib/${ANDROID_ABI}/libncnn.so
)

# 系统库
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# 链接库
target_link_libraries(sd
        ncnn
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}
)
