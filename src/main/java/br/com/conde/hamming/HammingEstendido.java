package br.com.conde.hamming;

/**
 * HAMMING ESTENDIDO: um bit de paridade geral a mais, e tudo muda.
 *
 * <p>O bit extra cobre a palavra inteira, inclusive os outros bits de paridade. Com ele a
 * distancia minima sobe de 3 para 4, e isso compra exatamente uma coisa: distinguir um erro
 * de dois.
 *
 * <ul>
 *   <li>sindrome zero e paridade geral certa: nada aconteceu;
 *   <li>sindrome nao zero e paridade geral ERRADA: um erro, no bit que a sindrome aponta;
 *   <li>sindrome nao zero e paridade geral certa: DOIS erros, e nao da para consertar.
 * </ul>
 *
 * <p>E o terceiro caso que justifica o bit: sem ele, o decodificador acha que viu um erro e
 * troca um bit inocente, entregando dado errado com cara de certo.
 */
public final class HammingEstendido implements Codigo {

    private final Hamming interno;

    public HammingEstendido(int bitsDeDado) {
        this.interno = new Hamming(bitsDeDado);
    }

    @Override
    public String nome() {
        return "Hamming estendido";
    }

    @Override
    public int bitsDeDado() {
        return interno.bitsDeDado();
    }

    @Override
    public int bitsDaPalavra() {
        return interno.bitsDaPalavra() + 1;
    }

    @Override
    public Palavra codificar(long dados) {
        Palavra base = interno.codificar(dados);

        long geral = base.peso() % 2;

        return new Palavra(bitsDaPalavra(),
                base.valor() | (geral << interno.bitsDaPalavra()));
    }

    @Override
    public Leitura decodificar(Palavra recebida) {
        Palavra semOExtra = new Palavra(interno.bitsDaPalavra(),
                recebida.valor() & ((1L << interno.bitsDaPalavra()) - 1));

        int sindrome = interno.sindrome(semOExtra);

        boolean geralErrada = recebida.peso() % 2 == 1;

        if (sindrome == 0)
            return geralErrada
                    ? new Leitura(Resultado.CORRIGIU, interno.dadosDe(semOExtra),
                            bitsDaPalavra())
                    : new Leitura(Resultado.LIMPA, interno.dadosDe(semOExtra), 0);

        if (!geralErrada)
            return new Leitura(Resultado.DETECTOU, interno.dadosDe(semOExtra), sindrome);

        if (sindrome > interno.bitsDaPalavra())
            return new Leitura(Resultado.DETECTOU, interno.dadosDe(semOExtra), sindrome);

        Palavra corrigida = semOExtra.trocando(sindrome);

        return new Leitura(Resultado.CORRIGIU, interno.dadosDe(corrigida), sindrome);
    }
}
