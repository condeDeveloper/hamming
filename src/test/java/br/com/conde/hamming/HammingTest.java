package br.com.conde.hamming;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** As pecas e as medidas. Tudo exaustivo. */
class HammingTest {

    @Nested
    @DisplayName("as pecas")
    class AsPecas {

        @Test
        @DisplayName("a posicao de paridade e potencia de dois, e so ela")
        void asPosicoesDeParidade() {
            assertThat(Hamming.ehPosicaoDeParidade(1)).isTrue();
            assertThat(Hamming.ehPosicaoDeParidade(2)).isTrue();
            assertThat(Hamming.ehPosicaoDeParidade(4)).isTrue();
            assertThat(Hamming.ehPosicaoDeParidade(8)).isTrue();

            assertThat(Hamming.ehPosicaoDeParidade(3)).isFalse();
            assertThat(Hamming.ehPosicaoDeParidade(6)).isFalse();
            assertThat(Hamming.ehPosicaoDeParidade(7)).isFalse();
        }

        @Test
        @DisplayName("quatro bits de dado pedem tres de paridade, e da o (7,4)")
        void oClassicoSeteQuatro() {
            Hamming codigo = new Hamming(4);

            assertThat(codigo.bitsDeParidade()).isEqualTo(3);
            assertThat(codigo.bitsDaPalavra()).isEqualTo(7);

            assertThat(Hamming.paridadesNecessarias(11)).isEqualTo(4);
            assertThat(Hamming.paridadesNecessarias(26)).isEqualTo(5);
            assertThat(Hamming.paridadesNecessarias(57)).isEqualTo(6);
        }

        @Test
        @DisplayName("codificar e decodificar sem erro devolve o dado, nos quatro codigos")
        void aIdaEVolta() {
            for (Codigo codigo : Codigo.todos(4))
                for (long dados = 0; dados < 16; dados++) {
                    Codigo.Leitura leitura = codigo.decodificar(codigo.codificar(dados));

                    assertThat(leitura.resultado())
                            .as(codigo.nome() + " com " + dados)
                            .isEqualTo(Codigo.Resultado.LIMPA);

                    assertThat(leitura.dados()).isEqualTo(dados);
                }
        }

        /**
         * A SINDROME NAO PROCURA O ERRO: ELA CALCULA O ENDERECO DELE.
         *
         * <p>Trocando o bit da posicao k, as paridades que falham sao exatamente as que tem
         * aquele bit ligado no indice. Somadas, elas dao k em binario.
         */
        @Test
        @DisplayName("a sindrome e exatamente o indice do bit trocado")
        void aSindromeEhOEndereco() {
            Hamming codigo = new Hamming(4);

            for (long dados = 0; dados < 16; dados++) {
                Palavra limpa = codigo.codificar(dados);

                assertThat(codigo.sindrome(limpa))
                        .as("sem erro, sindrome zero")
                        .isZero();

                for (int posicao = 1; posicao <= 7; posicao++)
                    assertThat(codigo.sindrome(limpa.trocando(posicao)))
                            .as("dado " + dados + ", bit " + posicao)
                            .isEqualTo(posicao);
            }
        }

        @Test
        @DisplayName("a distancia entre duas palavras conta os bits diferentes")
        void aDistanciaConta() {
            assertThat(Palavra.distancia(new Palavra(4, 0b0000), new Palavra(4, 0b1111)))
                    .isEqualTo(4);

            assertThat(Palavra.distancia(new Palavra(4, 0b0101), new Palavra(4, 0b0100)))
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("tabela 1: todo erro de um bit")
    class ACorrecao {

        @Test
        @DisplayName("o Hamming corrige os 112 casos, e o estendido os 128")
        void osDoisCorrigemTudo() {
            assertThat(Medidas.correcao("Hamming").casos()).isEqualTo(112);
            assertThat(Medidas.correcao("Hamming").corrigidos()).isEqualTo(112);
            assertThat(Medidas.correcao("Hamming").entregouErrado()).isZero();

            assertThat(Medidas.correcao("Hamming estendido").casos()).isEqualTo(128);
            assertThat(Medidas.correcao("Hamming estendido").corrigidos()).isEqualTo(128);
        }

        @Test
        @DisplayName("a paridade simples detecta os 80 e nao corrige nenhum")
        void aParidadeSoDetecta() {
            assertThat(Medidas.correcao("paridade simples").detectados()).isEqualTo(80);
            assertThat(Medidas.correcao("paridade simples").corrigidos()).isZero();
            assertThat(Medidas.correcao("paridade simples").entregouErrado()).isZero();
        }

        @Test
        @DisplayName("sem nada, os 64 casos entregam dado errado sem avisar")
        void semNadaTudoPassa() {
            assertThat(Medidas.correcao("sem nada").casos()).isEqualTo(64);
            assertThat(Medidas.correcao("sem nada").entregouErrado()).isEqualTo(64);
            assertThat(Medidas.correcao("sem nada").detectados()).isZero();
        }
    }

    @Nested
    @DisplayName("tabela 2: dois bits trocados")
    class ODuplo {

        /**
         * O NUMERO QUE DA NOME AO REPOSITORIO.
         *
         * <p>Nos 336 casos de erro duplo, o Hamming simples corrige para o lugar errado em
         * TODOS. Ele nao deixa de detectar: ele detecta, conclui com confianca, e entrega
         * dado corrompido com cara de corrigido.
         */
        @Test
        @DisplayName("o Hamming corrige errado em 100% dos 336 erros duplos")
        void oHammingCorrigeErradoSempre() {
            Medidas.Duplo linha = Medidas.duplo("Hamming");

            assertThat(linha.casos()).isEqualTo(336);
            assertThat(linha.corrigiuErrado()).isEqualTo(336);
            assertThat(linha.detectados()).isZero();
            assertThat(linha.fracaoCorrigidaErrado()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("e o bit extra detecta os 448, sem corrigir nenhum")
        void oEstendidoDetectaTudo() {
            Medidas.Duplo linha = Medidas.duplo("Hamming estendido");

            assertThat(linha.casos()).isEqualTo(448);
            assertThat(linha.detectados()).isEqualTo(448);
            assertThat(linha.corrigiuErrado()).isZero();
        }

        @Test
        @DisplayName("a paridade simples nao ve nenhum dos 160")
        void aParidadeNaoVeODuplo() {
            assertThat(Medidas.duplo("paridade simples").detectados()).isZero();
            assertThat(Medidas.duplo("paridade simples").passouBatido()).isEqualTo(160);
        }
    }

    @Nested
    @DisplayName("tabela 3: a distancia minima")
    class ADistancia {

        @Test
        @DisplayName("1, 2, 3 e 4, medidas nos 120 pares de cada codigo")
        void asQuatroDistancias() {
            assertThat(Medidas.distancia("sem nada").medida()).isEqualTo(1);
            assertThat(Medidas.distancia("paridade simples").medida()).isEqualTo(2);
            assertThat(Medidas.distancia("Hamming").medida()).isEqualTo(3);
            assertThat(Medidas.distancia("Hamming estendido").medida()).isEqualTo(4);

            for (Medidas.Distancia linha : Medidas.distancias())
                assertThat(linha.pares()).isEqualTo(120);
        }

        /**
         * A DISTANCIA EXPLICA AS DUAS TABELAS ANTERIORES, SEM MAIS NADA.
         *
         * <p>Com distancia d, da para corrigir a parte inteira de (d-1)/2 erros. Para o
         * Hamming isso e 1, e para o estendido tambem: o bit extra nao compra correcao, ele
         * compra a capacidade de saber que nao da para corrigir.
         */
        @Test
        @DisplayName("os dois corrigem 1 bit, e so o estendido pega o segundo")
        void aDistanciaExplicaTudo() {
            assertThat((Medidas.distancia("Hamming").medida() - 1) / 2).isEqualTo(1);
            assertThat((Medidas.distancia("Hamming estendido").medida() - 1) / 2)
                    .isEqualTo(1);

            assertThat(Medidas.distancia("Hamming").medida() - 1)
                    .as("detecta dois, mas so se nao tentar corrigir")
                    .isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("tabela 4: o custo em bits")
    class OCusto {

        @Test
        @DisplayName("o aproveitamento vai de 57,1% a 90,5% conforme o bloco cresce")
        void oAproveitamentoSobe() {
            assertThat(Medidas.custos()).hasSize(4);

            assertThat(Medidas.custos().get(0).bitsDaPalavra()).isEqualTo(7);
            assertThat(Medidas.custos().get(3).bitsDaPalavra()).isEqualTo(63);

            double anterior = 0;

            for (Medidas.Custo linha : Medidas.custos()) {
                assertThat(linha.aproveitamento())
                        .as(linha.bitsDeDado() + " bits de dado")
                        .isGreaterThan(anterior);

                anterior = linha.aproveitamento();
            }
        }

        @Test
        @DisplayName("a palavra e sempre 2^r menos 1")
        void aPalavraEhPotenciaMenosUm() {
            for (Medidas.Custo linha : Medidas.custos())
                assertThat(linha.bitsDaPalavra())
                        .as(linha.bitsDeDado() + " bits de dado")
                        .isEqualTo((1 << linha.bitsDeParidade()) - 1);
        }
    }
}
