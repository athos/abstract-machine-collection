# SECDマシン

**S**(Stack), **E**(Environment), **C**(Code), **D**(Dump)の4つのレジスタをもつ抽象機械。
ラムダ計算をベースにした言語のコンパイルターゲットとしてよく使われる。スタックベース。

初出のLandinの論文([PDF](https://www.cs.cmu.edu/~crary/819-f09/Landin64.pdf))では、
SECDマシンはラムダ項を直接扱っている。WikipediaなどにSECDマシンとして説明されている、
命令セットをもったSECDマシンは、Peter HendersonのLispkit Lispの実装に使われたものが由来らしい。

各命令のオリジナルの定義は *Functional Programming: Application and Implementation* にあるらしいが未見。
命令セットと各命令に対する実際の操作はLispKitのマニュアル([PDF](http://www.cs.ox.ac.uk/files/3300/PRG32%20vol%202.pdf))
で確認できる。

ここでは、ウェブ上で情報の見つかった以下を参考にして実装している：

- zachallaun/secd
    - https://github.com/zachallaun/secd
- LispKit Lisp - Think Stitch - PRINCIPIA
    - http://www.principia-m.com/ts/0117/index-jp.html
