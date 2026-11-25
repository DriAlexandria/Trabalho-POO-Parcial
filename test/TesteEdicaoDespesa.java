package cabenobolso.test;

import cabenobolso.controller.PlanejamentoController;
import cabenobolso.exception.ValidacaoException;
import cabenobolso.model.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TesteEdicaoDespesa {
    private static BigDecimal valor(String v) { return new BigDecimal(v); }
    private static void exigir(boolean ok, String msg) {
        if (!ok) throw new AssertionError(msg);
    }
    private static void total(Planejamento p, String esperado) {
        exigir(p.calcularCustoTotal().compareTo(valor(esperado)) == 0, "Total incorreto: " + p.calcularCustoTotal());
    }
    private static void individual(Planejamento p, Participante pessoa, String esperado) {
        exigir(p.calcularGastoIndividual(pessoa).compareTo(valor(esperado)) == 0, "Gasto incorreto: " + pessoa);
    }
    public static void main(String[] args) throws Exception {
        PlanejamentoController c = new PlanejamentoController();
        c.carregarCasoReferencia();
        Planejamento p = c.getPlanejamento();
        Participante a = p.buscarParticipante(1), b = p.buscarParticipante(2), carla = p.buscarParticipante(3);
        c.alterarDespesa("D1", "Transporte atualizado", valor("60"), "Fixa", List.of(a,b,carla), true, 0);
        total(p,"180"); individual(p,a,"65"); individual(p,b,"50"); individual(p,carla,"65");
        exigir(p.getDespesas().size() == 4 && p.getDespesas().get(0).getId().equals("D1") && p.getDespesas().get(0).getDescricao().equals("Transporte atualizado"), "Identidade ou descricao incorreta");
        System.out.println("[PASSOU] Edicao do valor e descricao; total 180, individuais 65/50/65");
        c.alterarDespesa("D1", "Ingresso", valor("12"), "Por pessoa", List.of(a,carla), false, 2);
        total(p,"144"); individual(p,a,"57"); individual(p,b,"30"); individual(p,carla,"57");
        Despesa editada = p.getDespesas().get(0);
        exigir(editada instanceof DespesaPorPessoa && !editada.isEssencial() && editada.getPrioridade()==2, "Tipo/classificacao incorreta");
        System.out.println("[PASSOU] Alteracao de tipo, participantes e prioridade; total 144");
        try {
            c.alterarDespesa("D1", "Invalida", valor("-1"), "Fixa", List.of(a), true, 0);
            throw new AssertionError("Valor invalido aceito");
        } catch (ValidacaoException esperado) { exigir(p.getDespesas().get(0)==editada,"Estado alterado"); }
        total(p,"144");
        System.out.println("[PASSOU] Edicao invalida preserva despesa e totais");
        try {
            c.alterarDespesa("D1", "Invalida", valor("10"), "Fixa", List.of(new Participante(9,"Fora",valor("50"))), true, 0);
            throw new AssertionError("Vinculo invalido aceito");
        } catch (ValidacaoException esperado) { exigir(p.getDespesas().get(0)==editada,"Estado alterado"); }
        System.out.println("[PASSOU] Edicao com participante externo recusada");
        Path arq = Files.createTempFile("edicao-despesa", ".dat");
        try {
            c.salvarPlanejamento(arq.toFile()); c.carregarPlanejamento(arq.toFile());
            Planejamento recuperado = c.getPlanejamento(); total(recuperado,"144");
            Despesa d = recuperado.getDespesas().get(0);
            exigir(d instanceof DespesaPorPessoa && d.getParticipantes().size()==2 && d.getParticipantes().get(0)==recuperado.buscarParticipante(1), "Vinculo ou tipo perdido");
        } finally { Files.deleteIfExists(arq); }
        System.out.println("[PASSOU] Persistencia da despesa editada");
        System.out.println("RESULTADO: 5/5");
    }
}
