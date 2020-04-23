# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             hpatchalgo

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper.cpp

             # Respiratory Rate Estimation Algorithm
             src/main/cpp/RREst/common.cpp
             src/main/cpp/RREst/lib_fft.cpp
             src/main/cpp/RREst/lib_matrix.cpp
             src/main/cpp/RREst/lib_RREst.cpp
             src/main/cpp/RREst/InvMat.cpp
             #src/main/cpp/RREst/berger.cpp
             #src/main/cpp/RREst/linearInt.cpp
             #src/main/cpp/RREst/polyfit.cpp

             # HR - HRV
             src/main/cpp/ECGHRV/ecg_freq_hrv_proc.cpp
             src/main/cpp/ECGHRV/ecg_hr_proc.cpp
             src/main/cpp/ECGHRV/ecg_hrv_pre_proc.cpp
             src/main/cpp/ECGHRV/ecg_hrv_proc.cpp
             src/main/cpp/ECGHRV/ecg_time_hrv_proc.cpp
             src/main/cpp/ECGHRV/lib_data.cpp
             src/main/cpp/ECGHRV/lib_statistics.cpp

             # Lead On/Off
             src/main/cpp/LeadOnOff/LeadOnOffDetect.cpp

             # ECG Fitness
             src/main/cpp/ECGFitness/ECGFitness_proc.cpp
             )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       hpatchalgo

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
