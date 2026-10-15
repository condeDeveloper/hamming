package br.com.conde.hamming;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: tudo o que o repositorio afirma, calculado aqui e em nenhum outro lugar.
 *
 * <p>Todas as tabelas sao EXAUSTIVAS. Com quatro bits de dado sao dezesseis palavras, e
 * varrer todas elas vezes todos os padroes de erro cabe inteiro: nao ha amostragem em lugar
 * nenhum.
 */
public final class Medidas {

    /** Os tamanhos de dado medidos na tabela do custo. */
    public static final int[] DADOS = {4, 11, 26, 57};

    /** O tamanho usado nas tabelas exaustivas. */
    public static final int PEQUENO = 4;

    private Medidas() {}

    /* ===================== 1. correcao ===================== */

    public record Correcao(String codigo, int bitsDeDado, long casos, long corrigidos,
            long detectados, long entregouErrado) {}

    private static List<Correcao> correcao;

    /**
     * TODA PALAVRA, TODO ERRO DE UM BIT.
     *
     * <p>O juiz e o dado original: nao basta detectar, o dado entregue tem de ser o que
     * entrou.
     *
     * <p>A coluna que importa e a ultima. "Entregou errado" quer dizer que o decodificador
     * nao acusou nada, ou achou que corrigiu, e o dado que saiu nao e o que entrou. E o pior
     * desfecho possivel, pior do que detectar e desistir.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        for (Codigo codigo : Codigo.todos(PEQUENO)) {
            long casos = 0;
            long corrigidos = 0;
            long detectados = 0;
            long errado = 0;

            for (long dados = 0; dados < (1L << PEQUENO); dados++) {
                Palavra limpa = codigo.codificar(dados);

                for (int posicao = 1; posicao <= codigo.bitsDaPalavra(); posicao++) {
                    casos++;

                    Codigo.Leitura leitura = codigo.decodificar(limpa.trocando(posicao));

                    boolean acertou = leitura.dados() == dados;

                    if (leitura.resultado() == Codigo.Resultado.CORRIGIU && acertou)
                        corrigidos++;

                    if (leitura.resultado() == Codigo.Resultado.DETECTOU) detectados++;

                    if (!acertou && leitura.resultado() != Codigo.Resultado.DETECTOU)
                        errado++;
                }
            }

            saida.add(new Correcao(codigo.nome(), PEQUENO, casos, corrigidos, detectados,
                    errado));
        }

        return correcao = List.copyOf(saida);
    }

    public static Correcao correcao(String codigo) {
        return correcao().stream().filter(linha -> linha.codigo().equals(codigo))
                .findFirst().orElseThrow();
    }

    /* ===================== 2. erro duplo ===================== */

    public record Duplo(String codigo, long casos, long detectados, long corrigiuErrado,
            long passouBatido) {

        public double fracaoCorrigidaErrado() {
            return corrigiuErrado / (double) casos;
        }
    }

    private static List<Duplo> duplos;

    /**
     * TODA PALAVRA, TODO PAR DE BITS TROCADOS.
     *
     * <p>Aqui esta a coisa mais importante do repositorio. O Hamming simples tem distancia
     * 3: dois erros levam a palavra para dentro da vizinhanca de OUTRA palavra valida, e o
     * decodificador corrige com confianca para o lugar errado.
     */
    public static List<Duplo> duplos() {
        if (duplos != null) return duplos;

        List<Duplo> saida = new ArrayList<>();

        for (Codigo codigo : Codigo.todos(PEQUENO)) {
            long casos = 0;
            long detectados = 0;
            long errado = 0;
            long batido = 0;

            int n = codigo.bitsDaPalavra();

            for (long dados = 0; dados < (1L << PEQUENO); dados++) {
                Palavra limpa = codigo.codificar(dados);

                for (int um = 1; um <= n; um++)
                    for (int outro = um + 1; outro <= n; outro++) {
                        casos++;

                        Codigo.Leitura leitura =
                                codigo.decodificar(limpa.trocando(um).trocando(outro));

                        if (leitura.resultado() == Codigo.Resultado.DETECTOU) {
                            detectados++;
                            continue;
                        }

                        if (leitura.dados() == dados) continue;

                        if (leitura.resultado() == Codigo.Resultado.CORRIGIU) errado++;
                        else batido++;
                    }
            }

            saida.add(new Duplo(codigo.nome(), casos, detectados, errado, batido));
        }

        return duplos = List.copyOf(saida);
    }

    public static Duplo duplo(String codigo) {
        return duplos().stream().filter(linha -> linha.codigo().equals(codigo))
                .findFirst().orElseThrow();
    }

    /* ===================== 3. distancia minima ===================== */

    public record Distancia(String codigo, int medida, long pares) {}

    private static List<Distancia> distancias;

    /**
     * A DISTANCIA MINIMA, MEDIDA PAR A PAR.
     *
     * <p>Ela nao e lida de lugar nenhum: sao todas as palavras validas contra todas as
     * outras, contando em quantos bits diferem. E dela que tudo decorre, porque com
     * distancia d da para detectar d-1 erros ou corrigir a parte inteira de (d-1)/2.
     */
    public static List<Distancia> distancias() {
        if (distancias != null) return distancias;

        List<Distancia> saida = new ArrayList<>();

        for (Codigo codigo : Codigo.todos(PEQUENO)) {
            int menor = Integer.MAX_VALUE;
            long pares = 0;

            for (long um = 0; um < (1L << PEQUENO); um++)
                for (long outro = um + 1; outro < (1L << PEQUENO); outro++) {
                    pares++;

                    menor = Math.min(menor, Palavra.distancia(codigo.codificar(um),
                            codigo.codificar(outro)));
                }

            saida.add(new Distancia(codigo.nome(), menor, pares));
        }

        return distancias = List.copyOf(saida);
    }

    public static Distancia distancia(String codigo) {
        return distancias().stream().filter(linha -> linha.codigo().equals(codigo))
                .findFirst().orElseThrow();
    }

    /* ===================== 4. custo ===================== */

    public record Custo(int bitsDeDado, int bitsDeParidade, int bitsDaPalavra,
            double aproveitamento) {}

    /** Quantos bits de paridade o Hamming precisa, e quanto sobra para dado. */
    public static List<Custo> custos() {
        List<Custo> saida = new ArrayList<>();

        for (int dados : DADOS) {
            Hamming codigo = new Hamming(dados);

            saida.add(new Custo(dados, codigo.bitsDeParidade(), codigo.bitsDaPalavra(),
                    dados / (double) codigo.bitsDaPalavra()));
        }

        return saida;
    }
}
