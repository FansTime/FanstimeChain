# Fanstime Chain

# 使用方法
##启动私有链步骤
1. 进入项目根目录，执行gradlew runPrivate即可

## 添加wallet并分配balance步骤
1. 启动私有链
2. curl -X POST --data '{"jsonrpc":"2.0","method":"personal_newAccount","params":["passphrase"],"id":1}' ${host}
3. 停止私有链
4. 记录上一步返回的地址，并修改genesis-private.json文件，在alloc属性中添加 "youraddress": { "balance": "yourbalance" }
5. 清除fti数据库，数据库地址为用户目录下fti文件夹
6. 重启私有链即可
