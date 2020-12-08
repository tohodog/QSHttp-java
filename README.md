QSHttp-java
====
[![qshttp][qshttpsvg]][star]  [![License][licensesvg]][license]

开箱即用的java-http框架,GET、POST、表单、JSON、上传、下载等等统统同一行代码搞定! One Code Man!
AIP精简到极致,调用没有一行多余代码,几乎零成本使用,大道至简 

  * 一句代码联网,参数控制方便,支持同步/异步/Future,使用简单
  * 多年生产环境迭代,稳定可靠
  * 支持http/自签名双向https(get post put head...) 文件上传、下载、进度监听、自动解析,基于Okhttp的支持cookie自动管理,缓存控制
  * 详细的请求信息回调、错误类型(网络链接失败,超时,断网,解析失败,404...)
  * 详细的访问日记打印,非常方便调试
  * 支持多拦截器,可添加一些公共鉴权参数
  * 模块化设计,联网模块可更换,目前提供OkHttp和java原生两种实现

### Maven
```
<dependency>
    <groupId>com.github.tohodog</groupId>
    <artifactId>qshttp-java</artifactId>
    <version>1.5.4</version>
</dependency>
```

### 最简单的使用例子
```
QSHttp.get("http://xxx").syncExecute();
```
### HTTP调试地址
https://api.reol.top/api_test
<br/>
可接受任何请求,该接口返回用户请求信息


### GET
```
        String url = "https://api.reol.top/api_test";
        JSONObject jsonObject = QSHttp.get(url).param("name", "qshttp").syncExecute().jsonModel();
        //业务逻辑
        jsonObject.getString("xxx");
```


### POST (application/x-www-form-urlencoded)
```
        String url = "https://api.reol.top/api_test";
        String result = QSHttp.post(url).param("name", "qshttp").syncExecute().string();
```

### POST (application/json)
```
        String url = "https://api.reol.top/api_test";
        //不同类型请求只需改个方法名称即可实现
        String result = QSHttp.postJSON(url).param("name", "qshttp").syncExecute().string();
```


### Download
```
        //基于get下载
        String url = "https://api.reol.top/api_test";
        QSHttp.download(url,"/sdcard/xxx.txt").syncExecute();
```


### Upload (multipart/form-data)
```
        String url = "https://api.reol.top/api_test";
        JSONObject resultJSON = QSHttp.upload(url)
                //文本参数
                .param("userName", 10086)
                .param("password", "qwe123456")
                //文件参数
                .param("file", new File("xx.jpg"))
                .param("bytes", new byte[1024])//上传一个字节数组
                //指定上传的文件名,content-type参数
                .multipartBody("icon", "image/*", "icon.jpg", new File("xx.jpg"))
                .multipartBody(new String("icon"), "image/*", "icon2.jpg", new byte[1024])//icon文件数组上传
                .syncExecute()
                .jsonModel();
```

### 异步
```
        //回调
        QSHttp.get("http://xxx").asyncExecute(new HttpCallback() {
            @Override
            public void onSuccess(ResponseParams response) {
                
            }

            @Override
            public void onFailure(HttpException e) {

            }
        });

        //Future
        Future<ResponseParams> future = QSHttp.get("https://baidu.com").futureExecute();
        JSONObject resultJSON = future.get().jsonModel();

```



###  高级配置
```
        //使用配置初始化
        QSHttp.init(QSHttpConfig.Build()
                //配置需要签名的网站 读取assets/cers文件夹里的证书
                //支持双向认证 放入xxx.bks
                .ssl(Utils.getAssetsSocketFactory("cers", "password")
                        , "192.168.1.168")//地址参数:设置需要自签名的主机地址,不设置则只能访问证书列表里的https网站
                .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())//证书信任规则(全信任)
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(18 * 1000)
                .debug(true)
                //拦截器 添加头参数 鉴权
                .interceptor(interceptor)
                .build());

        //拦截器
        //TODO 拦截器需放到在 Application/静态变量/非内部类 里,否则外部类将会内存泄露
        static Interceptor interceptor = new Interceptor() {
                @Override
                public ResponseParams intercept(Chain chain) throws HttpException {
                    RequestParams r = chain.request()
                            .newBuild()
                            .header("Interceptor", "Interceptor")
                            //继续添加修改其他
                            .build();
                    return chain.proceed(r);//请求结果参数如有需要也可以进行修改
                }
            };
         
        //配置多个client
        QSHttp.addClient("CELLULAR", QSHttpConfig.Build(getApplication())
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(10 * 1000)
                .debug(true)
                .build());
        QSHttp.get("url").qsClient("CELLULAR").syncExecute();//该请求将使用上述的配置,走蜂窝网路
```


### 所有API一览

```
        String url = "https://api.reol.top/api_test";
                QSHttp.post(url)//选择请求的类型
                        .header("User-Agent", "QsHttp/Android")//添加请求头

                        .path(2333, "video")//构建成这样的url https://api.reol.top/api_test/2233/video

                        .param("userName", 123456)//键值对参数
                        .param("password", "asdfgh")//键值对参数
                        .param(new Bean())//键值对参数

                        .toJsonBody()//把 params 转为json;application/json
                        .jsonBody(new Bean())//传入一个对象,会自动转化为json上传;application/json

                        .requestBody("image/jpeg", new File("xx.jpg"))//直接上传自定义的内容 自定义contentType (postjson内部是调用这个实现)

                        .param("bytes", new byte[1024])//传一个字节数组,multipart支持此参数
                        .param("file", new File("xx.jpg"))//传一个文件,multipart支持此参数
                        .toMultiBody()//把 params 转为multipartBody参数;multipart/form-data


                        .parser(parser)//自定义解析,由自己写解析逻辑
                        .jsonModel(Bean.class)//使用FastJson自动解析json,传一个实体类即可

                        .resultByBytes()//请求结果返回一个字节组 默认是返回字符
                        .resultByFile(".../1.txt")//本地路径 有此参数 请求的内容将被写入文件

                        .errCache()//开启这个 [联网失败]会使用缓存,如果有的话
                        .clientCache(24 * 3600)//开启缓存,有效时间一天
                        .timeOut(10 * 1000)
                        .openServerCache()//开启服务器缓存规则 基于okhttp支持
                        //构建好参数和配置后调用执行联网
                        .syncExecute();
```
## Log
### v1.5.4(2020-12-04)
  * open source
## Other
  * 有问题请Add [issues](https://github.com/tohodog/QSHttp-java/issues)
  * 如果项目对你有帮助的话欢迎[![star][starsvg]][star]
  
[starsvg]: https://img.shields.io/github/stars/tohodog/QSHttp-java.svg?style=social&label=Stars
[star]: https://github.com/tohodog/QSHttp-java

[licensesvg]: https://img.shields.io/badge/License-Apache--2.0-red.svg
[license]: https://raw.githubusercontent.com/tohodog/QSHttp-java/master/LICENSE

[qshttpsvg]: https://img.shields.io/badge/QSHttp--java-1.5.4-blue.svg

