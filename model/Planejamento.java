package cabenobolso.model;

import cabenobolso.exception.ValidacaoException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titulo;
    private final List<Participante> participantes;
    private final List<Despesa> despesas;

    public Planejamento(String titulo) throws ValidacaoException {
        setTitulo(titulo);
        this.participantes = new ArrayList<>();
        this.despesas = new ArrayList<>();
    }

    public String getTitulo() { return titulo; }

    public void setTitulo(String titulo) throws ValidacaoException {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ValidacaoException("O título do planejamento é obrigatório.");
        }
        this.titulo = titulo.trim();
    }

    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    public void cadastrarParticipante(Participante p) throws ValidacaoException {
        if (p == null) throw new ValidacaoException("Participante não pode ser nulo.");
        for (Participante existente : participantes) {
            if (existente.getCodigo() == p.getCodigo()) {
                throw new ValidacaoException("Já existe participante com o código: " + p.getCodigo());
            }
        }
        participantes.add(p);
        Collections.sort(participantes);
    }

    public void alterarParticipante(int codigo, String novoNome, BigDecimal novoOrcamento) throws ValidacaoException {
        Participante p = buscarParticipante(codigo);
        // Valida os dois campos antes de alterar o participante existente.
        Participante validado = new Participante(codigo, novoNome, novoOrcamento);
        p.setNome(validado.getNome());
        p.setOrcamento(validado.getOrcamento());
    }

    public void excluirParticipante(int codigo) throws ValidacaoException {
        Participante p = buscarParticipante(codigo);
        for (Despesa d : despesas) {
            if (d.getParticipantes().contains(p)) {
                throw new ValidacaoException("Impossível excluir participante: vinculado à despesa '" + d.getDescricao() + "'.");
            }
        }
        participantes.remove(p);
    }

    public Participante buscarParticipante(int codigo) throws ValidacaoException {
        for (Participante p : participantes) {
            if (p.getCodigo() == codigo) return p;
        }
        throw new ValidacaoException("Participante com código " + codigo + " não encontrado.");
    }

    public void adicionarDespesa(Despesa d) throws ValidacaoException {
        validarVinculos(d);
        for (Despesa existente : despesas) {
            if (existente.getId().equalsIgnoreCase(d.getId())) {
                throw new ValidacaoException("Já existe despesa com o identificador: " + d.getId());
            }
        }
        despesas.add(d);
    }

    private void validarVinculos(Despesa d) throws ValidacaoException {
        if (d == null) throw new ValidacaoException("Despesa não pode ser nula.");
        for (Participante p : d.getParticipantes()) {
            if (p == null || buscarParticipante(p.getCodigo()) != p) {
                throw new ValidacaoException("Use apenas participantes cadastrados neste planejamento.");
            }
        }
    }

    // Substitui a despesa somente depois de validar todos os novos dados.
    // O identificador original é preservado, inclusive ao trocar o tipo.
    public void alterarDespesa(Despesa nova) throws ValidacaoException {
        validarVinculos(nova);
        for (int i = 0; i < despesas.size(); i++) {
            if (despesas.get(i).getId().equals(nova.getId())) {
                despesas.set(i, nova);
                return;
            }
        }
        throw new ValidacaoException("Despesa não encontrada para edição: " + nova.getId());
    }

    public void excluirDespesa(String id) throws ValidacaoException {
        Despesa remover = null;
        for (Despesa d : despesas) {
            if (d.getId().equalsIgnoreCase(id)) {
                remover = d;
                break;
            }
        }
        if (remover == null) {
            throw new ValidacaoException("Despesa " + id + " não encontrada para exclusão.");
        }
        despesas.remove(remover);
    }

    public BigDecimal calcularCustoTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Despesa d : despesas) {
            total = total.add(d.getValorTotalCalculado());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularGastoIndividual(Participante p) {
        BigDecimal gasto = BigDecimal.ZERO;
        for (Despesa d : despesas) {
            gasto = gasto.add(d.getValorIndividual(p));
        }
        return gasto.setScale(2, RoundingMode.HALF_UP);
    }

    public Map<Participante, BigDecimal> obterTotaisIndividuais() {
        Map<Participante, BigDecimal> mapa = new LinkedHashMap<>();
        for (Participante p : participantes) {
            mapa.put(p, calcularGastoIndividual(p));
        }
        return mapa;
    }
}