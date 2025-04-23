@echo off
"C:\\Users\\AMIRA EL YAMAMI\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HC:\\Users\\AMIRA EL YAMAMI\\AndroidStudioProjects\\quizappnasserollah-main\\openCVLibrary\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=21" ^
  "-DANDROID_PLATFORM=android-21" ^
  "-DANDROID_ABI=arm64-v8a" ^
  "-DCMAKE_ANDROID_ARCH_ABI=arm64-v8a" ^
  "-DANDROID_NDK=C:\\Users\\AMIRA EL YAMAMI\\AppData\\Local\\Android\\Sdk\\ndk\\27.0.12077973" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\AMIRA EL YAMAMI\\AppData\\Local\\Android\\Sdk\\ndk\\27.0.12077973" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\AMIRA EL YAMAMI\\AppData\\Local\\Android\\Sdk\\ndk\\27.0.12077973\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\AMIRA EL YAMAMI\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=C:\\Users\\AMIRA EL YAMAMI\\AndroidStudioProjects\\quizappnasserollah-main\\openCVLibrary\\build\\intermediates\\cxx\\Debug\\k6o4n2e3\\obj\\arm64-v8a" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=C:\\Users\\AMIRA EL YAMAMI\\AndroidStudioProjects\\quizappnasserollah-main\\openCVLibrary\\build\\intermediates\\cxx\\Debug\\k6o4n2e3\\obj\\arm64-v8a" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BC:\\Users\\AMIRA EL YAMAMI\\AndroidStudioProjects\\quizappnasserollah-main\\openCVLibrary\\.cxx\\Debug\\k6o4n2e3\\arm64-v8a" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
