# OkexQuant 期货合约价差套利系统

#### 项目介绍
本项目是在Okex平台提供的API v3基础上开发的一套数字货币期货合约套利系统，基本原理是当一个不同期的数字货币合约之间的价差产生异常比率，将进行同时开仓买入和开仓卖出交易，等待合约之间的价差回归正常比率，再进行同时的平仓卖出和平仓买入，从而产生利润。
##### 项目功能
1. 支持所有的okex合约币种
2. 支持多策略同时运行
3. 实时的合约行情
4. 不同用户的权限控制

#### 项目github
1. [OkexQuant 后端服务](https://github.com/liudexiang3218/OkexQuant)
1. [OkexQuant_vue 前端界面项目](https://github.com/liudexiang3218/OkexQuant_vue)

#### 软件架构
<img src="https://github.com/liudexiang3218/OkexQuant/blob/master/ScreenShots/flow.png?raw=true">


#### 安装教程

1. 安装activeMQ 5的版本
1. 下载项目 git clone https://github.com/liudexiang3218/OkexQuant.git
2. 导入eclipse
3. 修改配置文件system.properties
4. jetty:run项目 

#### 使用说明

1. [在线demo](http://47.75.108.228) 香港阿里云服务器
2. 根据条件设置策略
3. 开启策略开始按钮

#### system.properties配置说明

1.  ``broker_url ``:activemq连接url
2.  ``ok_websocket_url ``: okex ws连接url
3.  ``ok_rest_url ``: okex api服务器连接
3.  ``ok_api_key ``: 您的okex开发平台上申请的api key
4.  ``ok_secret_key ``: 您的okex开发平台上申请的secret key
5.  ``ok_passphrase ``: 您的okex开发平台上申请的passphrase
6.  ``ok_coins ``: 配置项目支持的数字货币（例如：btc,ltc,eth,etc,btg,xrp,eos）

#### 开发环境

1. eclipse-2018-12
2. maven 3.5.4
3. git
4. jetty
5. activeMQ 5.15.9

#### 技术栈
1. springmvc 4.3.19
2. activemq 5.15.4
3. ehcache 2.10.6
4. java-jwt 3.4.1
5. shiro 1.4.0

#### Donation
If you find Element useful, you can buy us a cup of coffee

<img width="650" src="https://github.com/liudexiang3218/OkexQuant/blob/master/ScreenShots/qrcode.png?raw=true" alt="donation">

#### 互助微信群
