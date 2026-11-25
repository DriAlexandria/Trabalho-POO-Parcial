package cabenobolso.controller;

import cabenobolso.exception.PersistenciaException;
import cabenobolso.exception.ValidacaoException;
import cabenobolso.model.*;
import cabenobolso.persistence.IPersistenciaPlanejamento;
import cabenobolso.persistence.SerializacaoPlanejamento;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PlanejamentoController {
    private Planejamento planejamento;
    private final IPersistenciaPlanejamento persistencia;

    public PlanejamentoController() {
        try {
            this.planejamento = new Planejamento("Passeio da Turma");
        } catch (ValidacaoException ignored) {}
        this.persistencia = new SerializacaoPlanejamento();
    }

    public Planejamento getPlanejamento() {
        return planejamento;
    }

    public void cadastrarParticipante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        Participante p = new Participante(codigo, nome, orcamento);
        planejamento.cadastrarParticipante(p);
    }

    public void alterarParticipante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        planejamento.alterarParticipante(codigo, nome, orcamento);
    }

    public void excluirParticipante(int codigo) throws ValidacaoException {
        planejamento.excluirParticipante(codigo);
    }

    public void adicionarDespesa(String id, String desc, BigDecimal valor, String tipo, 
                                 List<Participante> parts, boolean essencial, int prioridade) 
                                 throws ValidacaoException {
        planejamento.adicionarDespesa(criarDespesa(id, desc, valor, tipo, parts, essencial, prioridade));
    }

    public void alterarDespesa(String id, String desc, BigDecimal valor, String tipo,
                               List<Participante> parts, boolean essencial, int prioridade)
                               throws ValidacaoException {
        planejamento.alterarDespesa(criarDespesa(id, desc, valor, tipo, parts, essencial, prioridade));
    }

    private Despesa criarDespesa(String id, String desc, BigDecimal valor, String tipo,
                                List<Participante> parts, boolean essencial, int prioridade)
                                throws ValidacaoException {
        if ("Fixa".equalsIgnoreCase(tipo)) {
            return new DespesaFixa(id, desc, valor, parts, essencial, prioridade);
        } else if ("Por pessoa".equalsIgnoreCase(tipo)) {
            return new DespesaPorPessoa(id, desc, valor, parts, essencial, prioridade);
        }
        throw new ValidacaoException("Tipo de despesa inválido: " + tipo);
    }

    public void excluirDespesa(String id) throws ValidacaoException {
        planejamento.excluirDespesa(id);
    }

    public void salvarPlanejamento(File file) throws PersistenciaException {
        persistencia.salvar(this.planejamento, file);
    }

    public void carregarPlanejamento(File file) throws PersistenciaException {
        Planejamento carregado = persistencia.carregar(file);
        // Preserva o estado ativo caso a leitura seja corrompida
        this.planejamento = carregado;
    }

    public SimuladorOtimizacao.ResultadoSimulacao executarSimulador() {
        return SimuladorOtimizacao.simularMelhorCenario(this.planejamento);
    }

    public void novoPlanejamento() throws ValidacaoException {
        this.planejamento = new Planejamento("Novo Passeio");
    }

    public void carregarCasoReferencia() throws ValidacaoException {
        Planejamento exemplo = new Planejamento("Passeio da Turma");

        Participante ana = new Participante(
                1, "Ana", new BigDecimal("70.00"));
        Participante bruno = new Participante(
                2, "Bruno", new BigDecimal("60.00"));
        Participante carla = new Participante(
                3, "Carla", new BigDecimal("70.00"));

        exemplo.cadastrarParticipante(ana);
        exemplo.cadastrarParticipante(bruno);
        exemplo.cadastrarParticipante(carla);

        exemplo.adicionarDespesa(new DespesaFixa(
                "D1", "Transporte", new BigDecimal("90.00"),
                List.of(ana, bruno, carla), true, 0));

        exemplo.adicionarDespesa(new DespesaPorPessoa(
                "D2", "Almoço", new BigDecimal("20.00"),
                List.of(ana, bruno, carla), true, 0));

        exemplo.adicionarDespesa(new DespesaFixa(
                "D3", "Atividade recreativa", new BigDecimal("30.00"),
                List.of(ana, carla), false, 3));

        exemplo.adicionarDespesa(new DespesaPorPessoa(
                "D4", "Sobremesa", new BigDecimal("10.00"),
                List.of(ana, bruno, carla), false, 1));

        this.planejamento = exemplo;
    }
}