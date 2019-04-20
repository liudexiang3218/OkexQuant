# OkexQuant 期货合约价差套利系统

#### 项目介绍
本项目是在Okex平台提供的API v3基础上开发的一套数字货币期货合约套利系统，基本原理是当一个不同期的数字货币合约之间的价差产生异常比率，将进行同时开仓买入和开仓卖出交易，等待合约之间的价差回归正常比率，再进行同时的平仓卖出和平仓买入，从而产生利润。
##### 项目功能
1. 支持所有的okex合约币种
2. 支持多策略同时运行
3. 实时的合约行情
4. 不同用户的权限控制

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

#### 开发环境

1. eclipse-2018-12
2. maven 3.5.4
3. git
4. jetty
5. activeMQ 5.15.9


#### Donation
If you find Element useful, you can buy us a cup of coffee

<img width="650" src="https://github.com/liudexiang3218/OkexQuant/blob/master/ScreenShots/qrcode.png?raw=true" alt="donation">