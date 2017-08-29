# AITWolfAgent

人狼知能大会　自然言語処理部門　参加エージェント　[Team: AITWolf]
====

CEDEC2017で開催された人狼知能大会-自然言語処理部門に参加したエージェントを公開します．
今後の更新はAITWolfAgent2018にてgit管理します．

## Licence

原則は[MIT](https://github.com/tcnksm/tool/blob/master/LICENCE)に準拠します．
libフォルダ内にあるライブラリ等に関しては，著作権はそれぞれの製作者にあるため，各利用条件を調べ利用することとしてください．

人狼知能大会に参加する目的でソースコードを参考または利用する場合は著作権表示しなくていいです．代わりにアカウントFollowしてくれると嬉しいです．自由に使ってください．

細かいことは
itfukui0922@icloud.com
にメールしてください．

## Description
簡単に3つのクラス説明

### com.icloud.itfukui0922.LocalHostStarter
このクラスを実行すればローカルサーバが立ち上がり，勝手にエージェントが１試合おこなう．
同パッケージにあるKanolabStarterは大会サーバ接続用．

### com.icloud.itfukui0922.AITWolfPlayer
実際に動くエージェント．役職ごとクラスが分かれてたりはしない．

### com.icloud.itfukui0922.NatulalLanguageProcessing
自然言語処理するクラス．別スレッド動作するようにCallabeをimplementsしている．
大会近くなって焦って開発してるため，ソースコードが煩雑．

## Author
LongDistanceRider

[tcnksm](https://github.com/tcnksm)

## 開発中に気づいたこと
ただただ，開発中に気づいたことを書いておく．（チラシ裏）

### AbstractRoleAssignPlayerについて
原因不明だが，AbstractRoleAssignPlayerを使ってエージェント作成してるとGameInfo.getRole()でnullが返却される．
何言ってるのかわかんないと思うけど，自分もわからん．ローカルサーバでは起きないのに，大会サーバでは起きる．
原因分かったら是非連絡を！

今回はPlayerをimplementsしてエージェント作成したところ，この問題を回避した．

### update()で時間をかけすぎ
update()で時間をかけすぎるとClientLostExceptionが発生する．（エラー文はTALKできなかった．みたいなこと言われるけど）
結局，自然言語処理などの時間のかかる処理は別スレッドに渡して，update()を早めに処理完了させることで対処した．

### プロトコル部門のサンプルエージェント
プロトコル部門で使われるサンプルエージェントと戦わせると当然ながらエラーする．
サンプルエージェントが日本語だと思って聞いてたら英語を話されてびっくりエラーって感じ．
気づくのに1時間かかった．
ローカルサーバで対戦して動かしたい場合は，何もしないエージェントを作ればいいよ．
ソースコードではcom.icloud.itfukui0922.DammyPlayer2が何もしないエージェント．

### 5人以上対戦できない
5人を想定して作ったから，15人対戦とか無理．ハードコーティングにも限界があるし，なんかいい方法ないかな．

### SVMでミス分類が多い
ミス分類したら後処理で手直し．現状はミスリードしすぎてる．発言しないから気づかれないけどログ読むとやばい．素性が単語だけなのも原因の一つか．多分リニア解析（形態素解析＋構文解析＋etc..）した方がいいんじゃないかなって思ってる．

