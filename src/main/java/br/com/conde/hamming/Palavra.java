package br.com.conde.hamming;

/**
 * UMA PALAVRA DE BITS, guardada num inteiro longo.
 *
 * <p>O bit de indice 1 e o primeiro da palavra, nao o de indice 0. Isso parece capricho e e
 * o contrario: a numeracao a partir de 1 e o que faz o codigo de Hamming funcionar, porque a
 * posicao de cada bit, escrita em binario, DIZ de quais paridades ele participa.
 */
public record Palavra(int bits, long valor) {

    public boolean em(int posicao) {
        return ((valor >> (posicao - 1)) & 1) == 1;
    }

    public Palavra trocando(int posicao) {
        return new Palavra(bits, valor ^ (1L << (posicao - 1)));
    }

    public int peso() {
        return Long.bitCount(valor);
    }

    /** Em quantos bits duas palavras diferem. */
    public static int distancia(Palavra uma, Palavra outra) {
        return Long.bitCount(uma.valor() ^ outra.valor());
    }

    @Override
    public String toString() {
        StringBuilder saida = new StringBuilder();

        for (int posicao = bits; posicao >= 1; posicao--) saida.append(em(posicao) ? '1' : '0');

        return saida.toString();
    }
}
