arch=$(uname -m)

if [ $arch == 'x86_64' ]; then
  jna_arch='x86-64'
elif [ $arch == 'aarch64' ]; then
  jna_arch='aarch64'
else
  echo "Unknown arch: $arch"
  exit
fi

# Try to do a sanity check of the working directory
# We should have the gradle wrapper in the parent directory (root)
if [ ! -e "../gradlew" ]; then
  echo "The script must be executed in the ./natives directory relative to the project root!"
  exit
fi

cd natives || exit
mkdir build
cd build || exit
cmake .. -DCMAKE_BUILD_TYPE=Release
cmake --build . -j 8
mkdir -p ../../src/main/resources/natives/linux-"$jna_arch"
cp ./libzstd-jni.so ../../src/main/resources/natives/linux-"$jna_arch"/libzstd.so
