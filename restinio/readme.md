# restinio benchmark

## Build

```cmd
git clone https://github.com/zzuummaa/web_benchmark.git
git clone https://github.com/fmtlib/fmt.git
git clone https://github.com/chriskohlhoff/asio.git
git clone https://github.com/Stiffstream/restinio.git
git clone https://github.com/Tencent/rapidjson.git
git clone https://github.com/Stiffstream/json_dto.git

cd fmt
mkdir build && cd build
cmake -DFMT_TEST=OFF -DFMT_CUDA_TEST=OFF -DFMT_DOC=OFF ..
sudo cmake --build . --target install --config Release -- -j 4

cd ../../restinio
git reset --hard 0052518f5692f8f051031e06d933b726191be97e
git apply ../web_benchmark/restinio/restinio/cmake.patch
mkdir build && cd build
cmake -DASIO_DIR="../../asio/asio" -DRESTINIO_TEST=OFF -DRESTINIO_SAMPLE=OFF -DRESTINIO_INSTALL_SAMPLES=OFF \
                                   -DRESTINIO_BENCH=OFF -DRESTINIO_INSTALL_BENCHES=OFF -DRESTINIO_FIND_DEPS=ON \
                                   -DRESTINIO_ALLOW_SOBJECTIZER=OFF ../dev
sudo cmake --build . --target install --config Release -- -j 4

cd ../../rapidjson
mkdir build && cd build
cmake -DRAPIDJSON_BUILD_DOC=OFF -DRAPIDJSON_BUILD_EXAMPLES=OFF -DRAPIDJSON_BUILD_TESTS=OFF -DRAPIDJSON_BUILD_CXX17=ON ..
sudo cmake --build . --target install --config Release -- -j 4

cd ../../json_dto
mkdir build && cd build
cmake -DJSON_DTO_TEST=OFF -DJSON_DTO_FIND_DEPS=ON -DJSON_DTO_SAMPLE=OFF ../dev
sudo cmake --build . --target install --config Release -- -j 4

cd ../../web_benchmark/restinio
mkdir build && cd build
cmake -DASIO_DIR="/home/user/asio/asio" ..
cmake --build . --target all --config Release -- -j 4
```