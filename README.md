# English Study Bot
英単語学習ができるLINE Botです。<br>
対話形式でBotが英単語のクイズを出題します。<br>
クイズの回答結果はDBにデータとして蓄積され、データを利用した様々な種類のクイズ出題機能に活用されます（機能開発中）。<br>
例えば、過去に出題したクイズの中で正答率が低い英単語を優先してクイズ出題する機能を実装予定です。<br>

# 利用手順および動作概要
※前提として、ユーザーはLINE利用者とします。<br>
※未利用の方は、LINEをインストールしてアカウント作成することで本サービスを利用可能です。

1. 下記QRコードからLINE友だち追加します（現在はデプロイ準備中につき本サービスは未稼働）。<br>
<img src="https://user-images.githubusercontent.com/67531867/192816284-1dc89b58-1e4b-4eff-b047-c6f0bf2453a8.png" width="100pt"><br>
追加すると、下図のメッセージを受信します。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288499-ce218efb-7e64-498c-af77-fc1cdea3e1e1.png" width="300pt"><br>
2. クイズ開始<br>
「クイズ」と送信して、クイズを開始します。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288545-bca09c8c-d41e-4b11-8c9e-8eb7cf4b6d77.png" width="300pt"><br>
3. クイズ種類選択<br>
LINEのクイックリプライ機能で選択可能なクイズ種類の候補が表示されるので、実施したい種類を一つ選択します。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288557-85ace86d-9e05-456a-9767-55af7284b73e.png" width="300pt"><br>
4. クイズ回答<br>
選択した種類に応じたクイズが出題されるので、回答を考えます。<br>
LINEのクイックリプライ機能で回答の選択肢が表示されるので、回答を選んで送信します。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288568-7809df63-7ab7-4351-aaa7-96307adcde1c.png" width="300pt"><br>
5. 回答の正誤結果確認<br>
回答の正誤結果を受信します。不正解の場合は回答も受信します。<br>
以降、再び手順2に戻り、繰り返しクイズにチャレンジすることができます。<br>
<img src="https://user-images.githubusercontent.com/67531867/193288578-6906eb8a-da69-4223-96fc-2df50116a53d.png" width="300pt"> <img src="https://user-images.githubusercontent.com/67531867/193288584-35ea7bd1-1e2d-4e98-9487-277b60ce8c5c.png" width="300pt">

# 使用技術
- Java SE 17.0.3
- Spring Boot 2.7.2
- Maven 3.6.2
- PostgresQL 14.5
- JUnit 5

# 設計
- [テーブル設計書](https://docs.google.com/spreadsheets/d/1rlP4iNc9uD1PxetHrHBZZofWEijEOCvyfIoS7TIn3fo/edit?usp=sharing)
- [シーケンス図](https://drive.google.com/file/d/1GSTABbrdRAjXzpbVj43M-VlCOu5h55_Q/view?usp=sharing)
- [状態遷移図](https://drive.google.com/file/d/13wn3WXn9legSyf_IwaVfM8VTwHajra6y/view?usp=sharing)
- [状態遷移表](https://docs.google.com/spreadsheets/d/1xOJkhCtDV8zBJaq84Ska6b09Db5TRBig9HtAwPBc5QQ/edit?usp=sharing)
- [クラス図](https://drive.google.com/file/d/15kdYtEb76mrBM9kD3BoaZk0uVWJTJEKs/view?usp=sharing)

# こだわりポイント/苦労した点
### 工夫した点
- botの状態遷移処理の実装箇所（domain/service/state以下）。<br>
　今後の機能追加予定に備えて、保守性を考慮した実装としました。<br>
　具体的には、Stateパターンを参考にした処理を実装することで、新しい状態を追加する場合でも<br>既存のコードへの影響を抑制するコーディングができたと考えています。

### 苦労した点
開発の業務経験がない上にJavaでのコーディングも初めてだったため、<br>
設計・コーディング（テストコード含む）の全般において一から独力で遂行する点に苦労しました。<br>
その中でも特に下記に苦労しました。<br>
- クラスの設計・・・どのクラスにどの役割を持たせるべきか、どのクラスがあれば機能が実現できるか。
- botの状態遷移のコーディング・・・クラス設計に考慮漏れがあり、実装完了に最も時間を要しました。
- 単体テスト時のテスト時刻の設定・・・テストコードを書いて実行するまで、考慮が抜けていました。<br>LocalDateTimeのnowメソッドのみスタブ化する手法で解消出来ました。

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

### DB（PostgresQL）
- 新規テーブル作成
- カラム制約の付与（NULL制約、外部キー制約、CASCADE制約）
- SQL実行（CREATE / SELECT / UPDATE / INSERT / DELETE 文）

### 設計
- テーブル設計
- シーケンス設計
- 状態遷移設計
- クラス設計

# 今後の改善方針
## 機能面
- クイズ種類の追加
- クイズ出題形式のユーザー自身のカスタマイズ機能<br>（出題単語から意味を答えるクイズと出題された意味を持つ単語を答えるクイズの切り替え、選択肢あり/なしのクイズの切り替え等）
- ユーザー自身で英単語を登録する機能の追加（自身が苦手な単語に特化したクイズを出題させる機能の追加）

## 実装面
- 異常系の設計・実装
- 保守性考慮したログの追加、ルール決め
- バリデーションチェック
- Rest APIの認証機能追加
