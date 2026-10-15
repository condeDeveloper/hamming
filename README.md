# hamming

Código de Hamming do zero em Java 21: **sem o bit extra ele corrige errado 100% dos erros
duplos, e corrige com confiança.**

Quatro códigos, do nada até o Hamming estendido, varridos no domínio inteiro.

## Tudo aqui é exaustivo

Com quatro bits de dado são dezesseis palavras. Dezesseis palavras vezes todas as posições de
erro cabem inteiras, então não há amostragem em lugar nenhum: as tabelas são o domínio.

E o juiz é o **dado original**, não a detecção. Não basta acusar erro: o dado entregue tem que
ser o que entrou.

```
$ mvn exec:java -Dexec.args="correcao"

  codigo                    casos   corrigidos   detectados    entregou errado
  sem nada                     64            0            0                 64
  paridade simples             80            0           80                  0
  Hamming                     112          112            0                  0
  Hamming estendido           128          128            0                  0
```

A paridade simples detecta os 80 e não conserta nenhum, porque saber que há um erro não diz
onde ele está. O Hamming conserta os 112.

## Como ele calcula o endereço do erro

Numere os bits a partir de **1**, não de 0. As posições 1, 2, 4, 8 guardam paridades; as outras
levam dado. O bit de paridade da posição `2^i` cobre todas as posições cujo índice tem o bit
`i` ligado.

Quando um bit se perde, as paridades que falham são exatamente as que têm aquele bit ligado no
índice. Somando as posições das que falharam sai o **índice do bit errado, em binário**.

```java
codigo.sindrome(limpa)                  // 0, nada errado
codigo.sindrome(limpa.trocando(5))      // 5
codigo.sindrome(limpa.trocando(7))      // 7
```

O código não procura o erro: ele calcula o endereço dele. Isso está testado para as 16 palavras
vezes as 7 posições.

## O que acontece com dois bits

```
$ mvn exec:java -Dexec.args="duplo"

  codigo                    casos   detectados    corrigiu errado  passou batido
  sem nada                     96            0                  0             96
  paridade simples            160            0                  0            160
  Hamming                     336            0                336              0
  Hamming estendido           448          448                  0              0
```

**336 de 336.** O Hamming simples não deixa de detectar: ele detecta, conclui com confiança,
troca um bit inocente e entrega dado corrompido com cara de corrigido.

A razão é a distância mínima, e ela também é medida aqui, par a par:

```
$ mvn exec:java -Dexec.args="distancia"

  codigo                    distancia        pares        o que isso compra
  sem nada                          1          120     detecta 0, corrige 0
  paridade simples                  2          120     detecta 1, corrige 0
  Hamming                           3          120     detecta 2, corrige 1
  Hamming estendido                 4          120     detecta 3, corrige 1
```

Com distância 3, dois erros levam a palavra para **dentro da vizinhança de outra palavra
válida**. O decodificador faz exatamente o que foi mandado fazer e chega na resposta errada.

## O bit que custa um e vale muito

O estendido acrescenta uma paridade sobre a palavra inteira. A distância sobe de 3 para 4, e
isso compra uma coisa só: **distinguir um erro de dois**.

| síndrome | paridade geral | conclusão |
|---|---|---|
| zero | certa | nada aconteceu |
| não zero | errada | um erro, no bit que a síndrome aponta |
| não zero | **certa** | dois erros, e não dá para consertar |

É a terceira linha que justifica o bit. Repare que ele **não** compra correção: os dois
corrigem um bit só. O que ele compra é a capacidade de saber que não dá para corrigir.

## O que custa de redundância

```
$ mvn exec:java -Dexec.args="custo"

  bits de dado       paridade        palavra     aproveitamento
             4              3              7              57.1%
            11              4             15              73.3%
            26              5             31              83.9%
            57              6             63              90.5%
```

A paridade cresce com o logaritmo do bloco: dobrar o bloco custa **um bit a mais**. Por isso o
Hamming de blocos pequenos parece caro e o de blocos grandes é quase de graça.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `duplo`, `distancia`, `custo` e `comparacao`.

## O que as medidas me corrigiram

**Eu ia escrever que o Hamming "detecta dois erros e corrige um". Ele não faz as duas coisas ao
mesmo tempo.** A distância 3 permite uma OU a outra: ou o decodificador assume que houve no
máximo um erro e corrige, ou assume que pode haver dois e só acusa. Quem corrige, e é o que
todo mundo implementa, erra em **336 de 336** erros duplos. A frase da literatura está certa e
descreve dois modos de operação que não convivem.

**E o modo de falha é o pior que existe.** Não é "não detectou": é detectou, concluiu e
entregou dado trocado marcado como bom. Um sistema que confia na correção não tem como saber.
Com o bit extra, os mesmos 448 casos saem como "não sei consertar", que é uma resposta com que
dá para trabalhar.

**O bit extra não melhora a correção em nada.** Os dois corrigem exatamente um bit, e a conta
`(d-1)/2` dá 1 para os dois. Eu esperava que distância 4 corrigisse mais; distância 4 só separa
melhor, e separar melhor serve para desconfiar, não para consertar.

## O que não tem aqui

**Rajada de erros.** Um risco no disco ou um estouro de cosmic ray estraga bits vizinhos, e o
Hamming é feito para erros independentes. É para isso que existem o entrelaçamento e os códigos
de Reed e Solomon, que estão em
[`paridade`](https://github.com/condeDeveloper/paridade).

**Apagamento.** Aqui o erro é um bit TROCADO em posição desconhecida. Quando se sabe qual bit
sumiu, o problema é bem mais fácil, e a conta muda inteira.

**O limite de Hamming.** Existe prova de quantas palavras cabem num código com dada distância,
e o Hamming atinge esse limite: ele é perfeito, no sentido técnico. Provar isso é outro
assunto; aqui a distância é medida par a par.

**Detecção de erro em transmissão.** Para dizer só "chegou inteiro?" um CRC é mais barato e
mais forte, e está em [`crc`](https://github.com/condeDeveloper/crc).

## Licença

MIT.
