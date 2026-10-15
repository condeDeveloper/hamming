package br.com.conde.hamming;

import java.util.Locale;

/** O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem. */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("duplo") || comando.equals("tudo")) duplo();
        if (comando.equals("distancia") || comando.equals("tudo")) distancia();
        if (comando.equals("custo") || comando.equals("tudo")) custo();
        if (comando.equals("comparacao") || comando.equals("tudo")) comparacao();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("TODO ERRO DE UM BIT E CORRIGIDO?");
        System.out.println("Todas as 16 palavras vezes todas as posicoes de erro, sem"
                + " amostragem.");
        System.out.println("O juiz e o dado original: nao basta detectar, o dado entregue tem"
                + " de ser o que entrou.");
        System.out.println();

        System.out.printf("  %-22s %8s %12s %12s %18s%n",
                "codigo", "casos", "corrigidos", "detectados", "entregou errado");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf("  %-22s %8d %12d %12d %18d%n",
                    linha.codigo(), linha.casos(), linha.corrigidos(), linha.detectados(),
                    linha.entregouErrado());

        System.out.println();
    }

    private static void duplo() {
        System.out.println();
        System.out.println("O QUE ACONTECE COM DOIS BITS TROCADOS");
        System.out.println("Todas as palavras vezes todos os pares de posicoes.");
        System.out.println();

        System.out.printf("  %-22s %8s %12s %18s %14s%n",
                "codigo", "casos", "detectados", "corrigiu errado", "passou batido");

        for (Medidas.Duplo linha : Medidas.duplos())
            System.out.printf("  %-22s %8d %12d %18d %14d%n",
                    linha.codigo(), linha.casos(), linha.detectados(),
                    linha.corrigiuErrado(), linha.passouBatido());

        System.out.println();
        System.out.printf(Locale.ROOT,
                "  O Hamming simples corrige para o lugar errado em %.1f%% dos erros"
                        + " duplos.%n", 100 * Medidas.duplo("Hamming")
                        .fracaoCorrigidaErrado());
        System.out.println("  Ele nao falha em detectar: ele ACERTA a deteccao e erra a"
                + " conclusao, com confianca.");
        System.out.println();
    }

    private static void distancia() {
        System.out.println();
        System.out.println("A DISTANCIA MINIMA, MEDIDA PAR A PAR");
        System.out.println("Todas as palavras validas contra todas as outras. Nada e lido de"
                + " tabela.");
        System.out.println();

        System.out.printf("  %-22s %12s %12s %24s%n",
                "codigo", "distancia", "pares", "o que isso compra");

        for (Medidas.Distancia linha : Medidas.distancias())
            System.out.printf("  %-22s %12d %12d %24s%n",
                    linha.codigo(), linha.medida(), linha.pares(),
                    "detecta " + (linha.medida() - 1) + ", corrige "
                            + (linha.medida() - 1) / 2);

        System.out.println();
    }

    private static void custo() {
        System.out.println();
        System.out.println("QUANTO CUSTA DE BIT DE PARIDADE");
        System.out.println("O menor r tal que 2^r seja pelo menos m + r + 1.");
        System.out.println();

        System.out.printf("  %12s %14s %14s %18s%n",
                "bits de dado", "paridade", "palavra", "aproveitamento");

        for (Medidas.Custo linha : Medidas.custos())
            System.out.printf(Locale.ROOT, "  %12d %14d %14d %17.1f%%%n",
                    linha.bitsDeDado(), linha.bitsDeParidade(), linha.bitsDaPalavra(),
                    100 * linha.aproveitamento());

        System.out.println();
        System.out.println("  A redundancia cresce com o logaritmo: quanto maior o bloco,"
                + " mais barata ela fica.");
        System.out.println();
    }

    private static void comparacao() {
        System.out.println();
        System.out.println("CONTRA A PARIDADE SIMPLES E CONTRA NADA");
        System.out.println();

        System.out.printf("  %-22s %10s %14s %16s %18s%n",
                "codigo", "bits", "distancia", "corrige 1 bit?", "pega 2 bits?");

        for (Codigo codigo : Codigo.todos(Medidas.PEQUENO)) {
            Medidas.Correcao um = Medidas.correcao(codigo.nome());
            Medidas.Duplo dois = Medidas.duplo(codigo.nome());

            System.out.printf("  %-22s %10d %14d %16s %18s%n",
                    codigo.nome(), codigo.bitsDaPalavra(),
                    Medidas.distancia(codigo.nome()).medida(),
                    um.entregouErrado() == 0 ? "sim" : "nao",
                    dois.detectados() == dois.casos() ? "sim, todos"
                            : dois.detectados() + " de " + dois.casos());
        }

        System.out.println();
    }
}
