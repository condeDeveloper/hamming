package br.com.conde.hamming;

import java.util.List;

/**
 * UM CODIGO: transforma dados em palavra, e tenta voltar.
 *
 * <p>Os tres daqui respondem a mesma pergunta com forca diferente: "este bloco chegou
 * inteiro, e se nao chegou, da para consertar?".
 */
public interface Codigo {

    String nome();

    /** Quantos bits de dado cada bloco carrega. */
    int bitsDeDado();

    /** Quantos bits o bloco inteiro tem, com a redundancia. */
    int bitsDaPalavra();

    /** Monta a palavra a partir dos bits de dado. */
    Palavra codificar(long dados);

    /** O que o receptor conclui ao olhar a palavra que chegou. */
    Leitura decodificar(Palavra recebida);

    /** O que o receptor conclui. */
    record Leitura(Resultado resultado, long dados, int bitApontado) {}

    enum Resultado {
        /** Nada errado detectado. */
        LIMPA,
        /** Detectou e corrigiu, apontando um bit. */
        CORRIGIU,
        /** Detectou e NAO conseguiu corrigir. */
        DETECTOU
    }

    static List<Codigo> todos(int bitsDeDado) {
        return List.of(new SemNada(bitsDeDado), new ParidadeSimples(bitsDeDado),
                new Hamming(bitsDeDado), new HammingEstendido(bitsDeDado));
    }
}
