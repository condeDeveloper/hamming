package br.com.conde.hamming;

/**
 * HAMMING, 1950: as posicoes de potencia de dois guardam as paridades.
 *
 * <p>A ideia e de uma economia que ainda impressiona. Numere os bits da palavra a partir de
 * 1. As posicoes 1, 2, 4, 8 sao de paridade; as outras levam dado. O bit de paridade da
 * posicao {@code 2^i} cobre todas as posicoes cujo indice tem o bit i ligado.
 *
 * <p>Com isso, quando um bit se perde, as paridades que falham sao exatamente as que tem
 * aquele bit ligado no indice: somando as posicoes das paridades que falharam, sai o INDICE
 * do bit errado, em binario. O codigo nao procura o erro, ele calcula o endereco dele.
 *
 * <p>Isso da distancia minima 3, que e o que permite corrigir um erro. E e tambem o que faz
 * dois erros virarem um desastre silencioso, porque a soma de duas colunas da matriz e outra
 * coluna valida: a tabela do duplo mede isso.
 */
public final class Hamming implements Codigo {

    private final int bitsDeDado;
    private final int bitsDeParidade;

    public Hamming(int bitsDeDado) {
        this.bitsDeDado = bitsDeDado;
        this.bitsDeParidade = paridadesNecessarias(bitsDeDado);
    }

    /** O menor r tal que 2^r seja pelo menos m + r + 1. */
    public static int paridadesNecessarias(int bitsDeDado) {
        int r = 1;

        while ((1 << r) < bitsDeDado + r + 1) r++;

        return r;
    }

    @Override
    public String nome() {
        return "Hamming";
    }

    @Override
    public int bitsDeDado() {
        return bitsDeDado;
    }

    @Override
    public int bitsDaPalavra() {
        return bitsDeDado + bitsDeParidade;
    }

    public int bitsDeParidade() {
        return bitsDeParidade;
    }

    public static boolean ehPosicaoDeParidade(int posicao) {
        return (posicao & (posicao - 1)) == 0;
    }

    @Override
    public Palavra codificar(long dados) {
        int n = bitsDaPalavra();

        long valor = 0;

        int proximoDado = 0;

        for (int posicao = 1; posicao <= n; posicao++) {
            if (ehPosicaoDeParidade(posicao)) continue;

            if (((dados >> proximoDado) & 1) == 1) valor |= 1L << (posicao - 1);

            proximoDado++;
        }

        Palavra parcial = new Palavra(n, valor);

        for (int i = 0; i < bitsDeParidade; i++) {
            int posicao = 1 << i;

            if (paridadeDe(parcial, posicao, n)) valor |= 1L << (posicao - 1);

            parcial = new Palavra(n, valor);
        }

        return new Palavra(n, valor);
    }

    /** A paridade das posicoes cobertas por este bit, sem contar ele proprio. */
    private boolean paridadeDe(Palavra palavra, int posicaoDaParidade, int n) {
        int uns = 0;

        for (int posicao = 1; posicao <= n; posicao++) {
            if (posicao == posicaoDaParidade) continue;

            if ((posicao & posicaoDaParidade) == 0) continue;

            if (palavra.em(posicao)) uns++;
        }

        return uns % 2 == 1;
    }

    /**
     * A SINDROME: a soma das posicoes de paridade que falharam.
     *
     * <p>Se ela for zero, nenhuma paridade falhou. Se nao for, ela E o indice do bit a
     * trocar, sem busca nenhuma.
     */
    public int sindrome(Palavra recebida) {
        int n = bitsDaPalavra();

        int sindrome = 0;

        for (int i = 0; i < bitsDeParidade; i++) {
            int posicaoDaParidade = 1 << i;

            int uns = 0;

            for (int posicao = 1; posicao <= n; posicao++) {
                if ((posicao & posicaoDaParidade) == 0) continue;

                if (recebida.em(posicao)) uns++;
            }

            if (uns % 2 == 1) sindrome |= posicaoDaParidade;
        }

        return sindrome;
    }

    public long dadosDe(Palavra palavra) {
        long dados = 0;

        int proximoDado = 0;

        for (int posicao = 1; posicao <= bitsDaPalavra(); posicao++) {
            if (ehPosicaoDeParidade(posicao)) continue;

            if (palavra.em(posicao)) dados |= 1L << proximoDado;

            proximoDado++;
        }

        return dados;
    }

    @Override
    public Leitura decodificar(Palavra recebida) {
        int sindrome = sindrome(recebida);

        if (sindrome == 0) return new Leitura(Resultado.LIMPA, dadosDe(recebida), 0);

        if (sindrome > bitsDaPalavra())
            return new Leitura(Resultado.DETECTOU, dadosDe(recebida), sindrome);

        Palavra corrigida = recebida.trocando(sindrome);

        return new Leitura(Resultado.CORRIGIU, dadosDe(corrigida), sindrome);
    }
}
