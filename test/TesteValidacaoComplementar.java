package cabenobolso.test;

import cabenobolso.controller.PlanejamentoController;
import cabenobolso.exception.PersistenciaException;
import cabenobolso.exception.ValidacaoException;
import cabenobolso.model.*;
import cabenobolso.persistence.SerializacaoPlanejamento;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Testes complementares, independentes da suite original. */
public class TesteValidacaoComplementar {
    private static int total, aprovados;

    @FunctionalInterface
    private interface Acao { void executar() throws Exception; }

    private static BigDecimal reais(String valor) { return new BigDecimal(valor); }

    private static void conferir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void valor(BigDecimal obtido, String esperado) {
        conferir(obtido.compareTo(reais(esperado)) == 0,
                "Esperado: " + esperado + "; obtido: " + obtido);
    }

    private static void esperarErro(Class<? extends Exception> tipo, Acao acao) throws Exception {
        try {
            acao.executar();
        } catch (Exception e) {
            conferir(tipo.isInstance(e), "Esperava " + tipo.getSimpleName()
                    + ", mas recebeu " + e.getClass().getSimpleName());
            conferir(e.getMessage() != null && !e.getMessage().isBlank(), "Mensagem de erro ausente");
            return;
        }
        throw new AssertionError("Operacao invalida foi aceita");
    }

    private static void testar(String nome, Acao acao) {
        total++;
        try {
            acao.executar();
            aprovados++;
            System.out.println("[PASSOU] " + nome);
        } catch (Exception | AssertionError e) {
            System.out.println("[FALHOU] " + nome + " - " + e.getMessage());
        }
    }

    private static Planejamento referencia() throws Exception {
        Planejamento p = new Planejamento("Passeio da Turma");
        Participante a = new Participante(1, "Ana", reais("70"));
        Participante b = new Participante(2, "Bruno", reais("60"));
        Participante c = new Participante(3, "Carla", reais("70"));
        for (Participante pessoa : List.of(a,b,c)) p.cadastrarParticipante(pessoa);
        p.adicionarDespesa(new DespesaFixa("D1", "Transporte", reais("90"), List.of(a,b,c), true, 0));
        p.adicionarDespesa(new DespesaPorPessoa("D2", "Almoco", reais("20"), List.of(a,b,c), true, 0));
        p.adicionarDespesa(new DespesaFixa("D3", "Atividade recreativa", reais("30"), List.of(a,c), false, 3));
        p.adicionarDespesa(new DespesaPorPessoa("D4", "Sobremesa", reais("10"), List.of(a,b,c), false, 1));
        return p;
    }

    public static void main(String[] args) {
        testar("Campos vazios e estado preservado", () -> {
            Planejamento p = referencia();
            esperarErro(ValidacaoException.class, () -> p.cadastrarParticipante(new Participante(4, " ", reais("10"))));
            esperarErro(ValidacaoException.class, () -> p.adicionarDespesa(new DespesaFixa("D5", " ", reais("10"), p.getParticipantes(), true, 0)));
            conferir(p.getParticipantes().size() == 3 && p.getDespesas().size() == 4, "Listas alteradas");
            valor(p.calcularCustoTotal(), "210");
        });
        testar("Codigo duplicado e estado preservado", () -> {
            Planejamento p = referencia();
            esperarErro(ValidacaoException.class, () -> p.cadastrarParticipante(new Participante(1, "Duplicado", reais("10"))));
            conferir(p.getParticipantes().size() == 3 && p.buscarParticipante(1).getNome().equals("Ana"), "Cadastro original alterado");
        });
        testar("Orcamento negativo recusado", () -> {
            Planejamento p = referencia();
            esperarErro(ValidacaoException.class, () -> p.cadastrarParticipante(new Participante(4, "Teste", reais("-1"))));
            conferir(p.getParticipantes().size() == 3, "Cadastro invalido incluido");
        });
        testar("Alteracao invalida preserva nome e orcamento", () -> {
            Planejamento p = referencia();
            esperarErro(ValidacaoException.class, () -> p.alterarParticipante(1, "Nome alterado", reais("-1")));
            valor(p.buscarParticipante(1).getOrcamento(), "70");
            conferir(p.buscarParticipante(1).getNome().equals("Ana"), "Nome alterado apesar da recusa");
        });
        testar("Despesa com participante nao cadastrado", () -> {
            Planejamento p = referencia();
            Participante externo = new Participante(99, "Externo", reais("100"));
            esperarErro(ValidacaoException.class, () -> p.adicionarDespesa(new DespesaFixa("D5", "Teste", reais("10"), List.of(externo), true, 0)));
            conferir(p.getDespesas().size() == 4, "Despesa invalida incluida");
        });
        testar("Centavos por ordem de codigo, entrada embaralhada", () -> {
            Planejamento p = referencia();
            Participante a = p.buscarParticipante(1), b = p.buscarParticipante(2), c = p.buscarParticipante(3);
            Despesa d = new DespesaFixa("X", "Rateio", reais("100"), List.of(c,b,a), true, 0);
            valor(d.getValorIndividual(a), "33.34");
            valor(d.getValorIndividual(b), "33.33");
            valor(d.getValorIndividual(c), "33.33");
        });
        testar("Recuperacao do caso completo e de seus vinculos", () -> {
            Planejamento p = referencia();
            Path arq = Files.createTempFile("cabenobolso-", ".dat");
            try {
                SerializacaoPlanejamento repo = new SerializacaoPlanejamento();
                repo.salvar(p, arq.toFile());
                Planejamento r = repo.carregar(arq.toFile());
                conferir(r.getTitulo().equals(p.getTitulo()), "Titulo divergente");
                conferir(r.getParticipantes().size() == 3 && r.getDespesas().size() == 4, "Quantidade divergente");
                for (Participante original : p.getParticipantes()) {
                    Participante recuperado = r.buscarParticipante(original.getCodigo());
                    conferir(recuperado.getNome().equals(original.getNome()), "Nome divergente");
                    conferir(recuperado.getOrcamento().compareTo(original.getOrcamento()) == 0, "Orcamento divergente");
                    conferir(r.calcularGastoIndividual(recuperado).compareTo(p.calcularGastoIndividual(original)) == 0, "Rateio divergente");
                }
                for (int i = 0; i < p.getDespesas().size(); i++) {
                    Despesa d = p.getDespesas().get(i), e = r.getDespesas().get(i);
                    conferir(d.getId().equals(e.getId()) && d.getDescricao().equals(e.getDescricao()) && d.getTipo().equals(e.getTipo()), "Despesa divergente");
                    conferir(d.getValorInformado().compareTo(e.getValorInformado()) == 0 && d.isEssencial() == e.isEssencial() && d.getPrioridade() == e.getPrioridade(), "Dados da despesa divergentes");
                    conferir(d.getParticipantes().equals(e.getParticipantes()), "Vinculos divergentes");
                    for (Participante pessoa : e.getParticipantes()) conferir(pessoa == r.buscarParticipante(pessoa.getCodigo()), "Referencia de participante nao preservada");
                }
                valor(r.calcularCustoTotal(), "210");
            } finally { Files.deleteIfExists(arq); }
        });
        testar("Arquivo corrompido preserva planejamento ativo", () -> {
            PlanejamentoController c = new PlanejamentoController();
            c.cadastrarParticipante(1, "Ana", reais("70"));
            Planejamento antes = c.getPlanejamento();
            Path arq = Files.createTempFile("cabenobolso-invalido-", ".dat");
            try {
                Files.writeString(arq, "conteudo invalido");
                esperarErro(PersistenciaException.class, () -> c.carregarPlanejamento(arq.toFile()));
                conferir(c.getPlanejamento() == antes && antes.getParticipantes().size() == 1, "Planejamento ativo alterado");
            } finally { Files.deleteIfExists(arq); }
        });
        testar("Arquivo ausente preserva planejamento ativo", () -> {
            PlanejamentoController c = new PlanejamentoController();
            c.cadastrarParticipante(1, "Ana", reais("70"));
            Planejamento antes = c.getPlanejamento();
            Path pasta = Files.createTempDirectory("cabenobolso-ausente-");
            try {
                esperarErro(PersistenciaException.class, () -> c.carregarPlanejamento(pasta.resolve("ausente.dat").toFile()));
                conferir(c.getPlanejamento() == antes && antes.getParticipantes().size() == 1, "Planejamento ativo alterado");
            } finally { Files.deleteIfExists(pasta); }
        });
        testar("Simulador preserva planejamento original", () -> {
            Planejamento p = referencia();
            List<Despesa> despesas = List.copyOf(p.getDespesas());
            SimuladorOtimizacao.ResultadoSimulacao r = SimuladorOtimizacao.simularMelhorCenario(p);
            conferir(r.isSolucaoEncontrada(), "Solucao esperada nao encontrada");
            conferir(p.getDespesas().equals(despesas), "Despesas originais alteradas");
            valor(p.calcularCustoTotal(), "210");
            valor(p.calcularGastoIndividual(p.buscarParticipante(1)), "75");
            valor(p.calcularGastoIndividual(p.buscarParticipante(2)), "60");
            valor(p.calcularGastoIndividual(p.buscarParticipante(3)), "75");
            valor(p.buscarParticipante(1).getOrcamento(), "70");
            valor(p.buscarParticipante(2).getOrcamento(), "60");
            valor(p.buscarParticipante(3).getOrcamento(), "70");
        });
        System.out.printf("%nRESULTADO COMPLEMENTAR: %d/%d%n", aprovados, total);
        if (aprovados != total) System.exit(1);
    }
}
