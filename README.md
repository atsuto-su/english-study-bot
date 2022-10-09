# 英語学習クイズボット
英語学習ができるLINE Botです。対話形式でBotが英単語のクイズを出題します。<br>
クイズの回答結果はDBにユーザー毎のデータとして蓄積され、データを利用した様々な種類のクイズ出題機能に活用されます。<br>
現在、下記のクイズ出題機能があります。
- ランダム：DBに登録されている英単語の中からランダムに出題
- 出題日古い：ユーザーへのクイズ出題日が最も古い英単語を出題
- 正答率低い：ユーザーのクイズ正答率が最も低い英単語を出題
- 誤答：ユーザーの最後の回答結果が不正解だった英単語を出題

# 利用手順および動作概要
※前提として、ユーザーはLINE利用者とします。<br>
※未利用の方は、LINEをインストールしてアカウント作成することで本サービスを利用可能です。<br>
※デプロイ環境の利用料金節約の都合上、土日や深夜時間帯はサービス停止している場合があります。<br>

1. 下記QRコードからLINE友だち追加します。<br>
<img src="https://user-images.githubusercontent.com/67531867/192816284-1dc89b58-1e4b-4eff-b047-c6f0bf2453a8.png" width="100pt"><br>
2. クイズ開始<br>
「クイズ」と送信して、クイズを開始します。<br>
<img src="https://user-images.githubusercontent.com/67531867/194766469-034f6101-0c9f-4aa1-bdfe-927d48d104f2.png" width="300pt"><br>
3. クイズ種類選択<br>
LINEのクイックリプライ機能で選択可能なクイズ種類の候補が表示されるので、実施したい種類を一つ選択します。<br>
<img src="https://user-images.githubusercontent.com/67531867/194765106-383986da-5458-4450-8c96-a20c7228c9ee.png" width="300pt"><br>
4. クイズ回答<br>
選択した種類に応じたクイズが出題されるので、回答を考えます。<br>
LINEのクイックリプライ機能で回答の選択肢が表示されるので、回答を選んで送信します。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288568-7809df63-7ab7-4351-aaa7-96307adcde1c.png" width="300pt"><br>
5. 回答の正誤結果確認<br>
回答の正誤結果を受信します。不正解の場合は回答も受信します。<br>
以降は手順2に戻って繰り返すことで、クイズに繰り返しチャレンジすることができます。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288578-6906eb8a-da69-4223-96fc-2df50116a53d.png" width="300pt"> <img src="https://user-images.githubusercontent.com/67531867/193288584-35ea7bd1-1e2d-4e98-9487-277b60ce8c5c.png" width="300pt">

# 使用技術
- Java SE 17.0.3
- Spring Boot 2.7.2
- Maven 3.6.2
- PostgreSQL 14.5
- JUnit 5

# 設計
- [テーブル設計書](https://docs.google.com/spreadsheets/d/1rlP4iNc9uD1PxetHrHBZZofWEijEOCvyfIoS7TIn3fo/edit?usp=sharing)
- [シーケンス図](https://drive.google.com/file/d/1GSTABbrdRAjXzpbVj43M-VlCOu5h55_Q/view?usp=sharing)
- [状態遷移図](https://drive.google.com/file/d/13wn3WXn9legSyf_IwaVfM8VTwHajra6y/view?usp=sharing)
- [状態遷移表](https://docs.google.com/spreadsheets/d/1xOJkhCtDV8zBJaq84Ska6b09Db5TRBig9HtAwPBc5QQ/edit?usp=sharing)
- [クラス図](https://drive.google.com/file/d/15kdYtEb76mrBM9kD3BoaZk0uVWJTJEKs/view?usp=sharing)

# 工夫した点/苦労した点
### 工夫した点
- botの状態遷移処理で保守性と可読性を考慮した実装<br>
Stateパターンを参考にしてContextクラスと各Stateクラスを用意して各状態の処理を独立化しました。<br>
これにより、新しい状態を追加する場合でも既存のコードへの影響を抑制できると考えています。<br>
さらに、状態毎にクラスが分かれているため、ソースの可読性を向上できたと考えています。<br>
該当箇所のソースはこちら（[Contextクラス](src/main/java/net/myapp/englishstudybot/domain/service/quiz/QuizBotContext.java)と[各Stateクラス](src/main/java/net/myapp/englishstudybot/domain/service/quiz/state)）。
- クイズ出題機能の変更容易さを考慮したクラス設計<br>
クイズ出題機能（ランダム、出題日古い等）のロジック実装は一つのクラスに集約化しました。<br>
これにより、今後クイズ機能ロジックの追加/変更/削除が生じた際の改修範囲を抑制できるため、<br>
変更が比較的容易な実装ができたと考えています。<br>
該当箇所のソースは[こちら](/src/main/java/net/myapp/englishstudybot/domain/service/quiz/QuizGenerator.java)。

### 苦労した点
開発の業務経験がない上にJavaでのコーディングも初めてだったため、<br>
設計・コーディング（テストコード含む）の全般において一から独力で遂行する点に苦労しました。<br>
その中でも特に下記に苦労しました。<br>
- クラスの設計：<br>
どのクラスにどの役割を持たせるべきか、どのクラスがあれば機能が実現できるか悩み、時間を要しました。
- botの状態遷移のコーディング：<br>
クラス設計に考慮漏れがあり、実装完了に最も時間を要しました。
- 単体テスト時のテスト時刻の設定：<br>
テストコードを書いて実行するまで考慮が漏れており、解消に時間を要しました。<br>
調査して複数手段を検討した結果、LocalDateTimeのnowメソッドのみスタブ化する手法で解消しました。

# 本成果物の開発を通じて経験したこと
### Git
- ソース管理に必要な各種コマンド操作（add, commit, push, pull, branch, checkout, merge, stash, etc.）

### Javaのコーディング、Spring Bootを利用した開発
- DIの活用
- CRUDの実装
- Spring JDBCを利用したDBアクセス
- Rest APIの作成
- 独自業務ロジックの実装
- メッセージ定義の外部化とMesssageSource利用した取得
- Controller/Service/Repositoryの各層を意識した実装
- JUnit5で単体テスト、結合テストの実装
- Mockitoを利用したスタブの作成

### Java, Spring のその他ライブラリ
- DBのマイグレーションとしてFlyway
- ログ出力としてSLF4J
- テストのアサーションライブラリとしてAssertJ

### DB（PostgreSQL）
- 新規テーブル作成
- カラム制約の付与（NULL制約、外部キー制約、CASCADE制約）
- SQL実行（CREATE / SELECT / UPDATE / INSERT / DELETE 文）

### 設計
- テーブル設計
- シーケンス設計
- 状態遷移設計
- クラス設計

### デプロイ
- Herokuへのデプロイ

# 今後の改善方針
## 機能面
- クイズ出題形式のユーザーカスタマイズ機能の追加<br>（出題単語から意味を答えるクイズと出題された意味を持つ単語を答えるクイズの切り替え、選択肢あり/なしのクイズの切り替え等）
- ユーザー自身で英単語を登録する機能の追加。これにより、自身が苦手な単語に特化したクイズを出題可能にする。

## 実装面
- 異常系の設計・実装
- 保守性考慮したログの追加、ルール決め
- バリデーションチェック
- Rest APIの認証機能追加
