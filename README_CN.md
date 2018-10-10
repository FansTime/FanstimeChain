# Fanstime Chain
FansTime Chain是FansTime 根据粉丝经济垂直生态的特点，利用Dpos+Pow的算法打造出的公链，旨在提升明星、粉丝的生态运营效率，帮助明星一键发通证，对明星实现个人IP价值的最大化，对粉丝实现见面、互动梦想及价值回报。FansTime Chain内置垂直领域的特性模组，将简化个人通证发行工作的通证一键发行，便于保护作品版权的一键签名、盗版鉴别等。同时，Fanstime公链也会内置保护粉丝权益的安全功能，如强制通证锁仓、强制分期释放等，避免极少数居心不良的通证发行者损害社区利益。
* 项目主页：https://www.fanstime.org
* 项目白皮书：https://www.fanstime.org/FansTime-tc.pdf


# 使用方法
## 启动私有链步骤
1. 进入项目根目录，执行gradlew runPrivate即可

## 添加wallet并分配balance步骤
1. 启动私有链
2. curl -X POST --data '{"jsonrpc":"2.0","method":"personal_newAccount","params":["passphrase"],"id":1}' ${host}
3. 停止私有链
4. 记录上一步返回的地址，并修改genesis-private.json文件，在alloc属性中添加 "youraddress": { "balance": "yourbalance" }
5. 清除fti数据库，数据库地址为用户目录下fti文件夹
6. 重启私有链即可

# 联系我们
通过 [Telegram](https://t.me/fanstimeofficial)联系我们

# License
Fanstime Chain 项目基于 [MIT 协议](LICENSE)进行开源。
