package br.com.conde.hamming;

/**
 * PARIDADE SIMPLES: um bit a mais, dizendo se o numero de uns e par.
 *
 * <p>Ela detecta qualquer numero IMPAR de bits trocados e nao detecta nenhum numero par. E
 * nao corrige nada, porque saber que ha um erro nao diz onde ele esta.
 *
 * <p>A distancia minima dela e 2: duas palavras validas diferem em pelo menos dois bits, o
 * que basta para detectar um e nao basta para corrigir.
 */
public final class ParidadeSimples implements Codigo {

    private final int bitsDeDado;

    public ParidadeSimples(int bitsDeDado) {
        this.bitsDeDado = bitsDeDado;
    }

    @Override
    public String nome() {
        return "paridade simples";
    }

    @Override
    public int bitsDeDado() {
        return bitsDeDado;
    }

    @Override
    public int bitsDaPalavra() {
        return bitsDeDado + 1;
    }

    @Override
    public Palavra codificar(long dados) {
        long paridade = Long.bitCount(dados) % 2;

        return new Palavra(bitsDaPalavra(), dados | (paridade << bitsDeDado));
    }

    @Override
    public Leitura decodificar(Palavra recebida) {
        long dados = recebida.valor() & ((1L << bitsDeDado) - 1);

        if (recebida.peso() % 2 == 0) return new Leitura(Resultado.LIMPA, dados, 0);

        return new Leitura(Resultado.DETECTOU, dados, 0);
    }
}
