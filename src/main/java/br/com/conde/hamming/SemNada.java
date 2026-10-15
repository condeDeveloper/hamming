package br.com.conde.hamming;

/**
 * SEM NADA: manda os bits e torce.
 *
 * <p>Ela esta aqui para ser a referencia de zero. Toda palavra e valida, entao nenhum erro e
 * detectado nunca, e a tabela da comparacao mede o que os outros compram em cima disso.
 */
public final class SemNada implements Codigo {

    private final int bitsDeDado;

    public SemNada(int bitsDeDado) {
        this.bitsDeDado = bitsDeDado;
    }

    @Override
    public String nome() {
        return "sem nada";
    }

    @Override
    public int bitsDeDado() {
        return bitsDeDado;
    }

    @Override
    public int bitsDaPalavra() {
        return bitsDeDado;
    }

    @Override
    public Palavra codificar(long dados) {
        return new Palavra(bitsDeDado, dados);
    }

    @Override
    public Leitura decodificar(Palavra recebida) {
        return new Leitura(Resultado.LIMPA, recebida.valor(), 0);
    }
}
