# Cabe no Bolso — Planejador de Despesas Compartilhadas

Aplicação em **Java + Swing** para planejar despesas compartilhadas em um passeio ou atividade em grupo: calcular quanto cada participante gastará e identificar se os valores **cabem no bolso** de cada um.

> **Disciplina:** Programação Orientada a Objetos — EAD Unifor
> **Entrega:** 27/11/2026, às 23h59
> **Repositório:** https://github.com/DriAlexandria/Trabalho-POO-Parcial

---

## 👥 Equipe

| Nome | Matrícula |
|---|---|
| Rebeca de Morais Lima | 2641684 |
| Eduardo Carvalho Pereyra | 2641689 |
| Drielly Alexandria Tavares | 2641604 |
| Pedro Henrique Marinho Eusébio | 2641610 |
| Eliabe Torres Bessa | 2640888 |

### Contribuições

| Integrante | Responsabilidade |
| --- | --- |
| Rebeca de Morais Lima - 2641684 | Model + diagrama UML + relatório em PDF (organização, revisão ABNT e conferência com a proposta) + GitHub (ajuda a subir o código, revisão do README, link acessível) + ajuda nos códigos da equipe (revisão e integração) |
| Eduardo Carvalho Pereyra - 2641689 | Controller (`PlanejamentoController` — validar entradas, orquestrar o Model e atualizar a View) + Test (`TestePlanejamentoSuite` — 8 testes) |
| Drielly Alexandria Tavares - 2641604 | View (`MainFrame`, abas, tabelas e diálogos em Swing) + README + GitHub |
| Pedro Henrique Marinho Eusébio - 2641610 | Persistence (`IPersistenciaPlanejamento` e `SerializacaoPlanejamento` — salvar/carregar, arquivo ausente/corrompido) + Simulador de otimização (essenciais, opcionais com prioridade, busca e sem solução) |
| Eliabe Torres Bessa - 2640888 | Exception (`ValidacaoException` e `PersistenciaException` — tratamento de erros, mensagens e preservação do estado) + apoio na validação |

---

## 🎯 Objetivo

Organizar um passeio em grupo e responder, de forma simples e transparente:

1. Quanto custa o passeio no total?
2. Quanto cada participante paga?
3. O valor cabe no bolso de cada participante?
4. Quem está acima ou abaixo do orçamento?

---

## ✨ Funcionalidades

- [x] Cadastro de **participantes** com código único, nome e orçamento individual
- [x] Cadastro de **despesas fixas** e **despesas por pessoa**
- [x] Cálculo do **total das despesas**
- [x] Rateio **exato em centavos**, sem perda por arredondamento
- [x] Verificação se o valor **cabe no bolso** de cada participante
- [x] Exibição de **saldo** (orçamento − valor a pagar)
- [x] Relatório final com total, divisão, saldos e alertas
- [x] **Simulador de otimização**: escolhe quais despesas opcionais manter para caber no orçamento
- [x] **Persistência**: salvar e carregar o planejamento
- [x] Botão **“Carregar caso de referência”**
- [x] Validações de entrada com exceções personalizadas
- [x] Interface gráfica em **Java Swing**
- [x] Testes manuais da aplicação

---

## 🧩 Modelagem — Programação Orientada a Objetos

### Arquitetura MVC

| Pacote | Papel |
|---|---|
| `cabenobolso.model` | `Participante`, `Despesa`, `DespesaFixa`, `DespesaPorPessoa`, `Planejamento`, `SimuladorOtimizacao` |
| `cabenobolso.view` | `MainFrame` com abas, tabelas, botões e diálogos |
| `cabenobolso.controller` | Valida entradas, orquestra o modelo e atualiza a view |
| `cabenobolso.exception` | `ValidacaoException`, `PersistenciaException` |
| `cabenobolso.persistence` | `IPersistenciaPlanejamento`, `SerializacaoPlanejamento` |
| `cabenobolso.test` | `TestePlanejamentoSuite` |

### Conceitos de POO aplicados

- **Encapsulamento:** atributos privados + getters/setters com validação
- **Herança:** `DespesaFixa` e `DespesaPorPessoa` herdam de `Despesa` (abstrata)
- **Polimorfismo:** cada tipo de despesa implementa sua própria regra de rateio com `valorPorParticipante(codigo)`
- **Abstração:** cada classe representa um conceito do mundo real
- **Composição / Agregação:** `Planejamento` agrega participantes e despesas
- **Interface:** persistência acessada por `IPersistenciaPlanejamento`
- **Separação de responsabilidades (MVC):** `model`, `view`, `controller`, `persistence` e `exception` separados

### Rateio em centavos

Todos os valores são convertidos para centavos na entrada e só voltam a ser exibidos como reais na view.

Na despesa fixa: `q = total ÷ n`, `r = total mod n`. Os primeiros `r` participantes, na ordem crescente de código, recebem `q + 1 centavo`; os demais recebem `q`.

Exemplo: R$ 100,00 entre 3 → R$ 33,34 / R$ 33,33 / R$ 33,33.

Na despesa por pessoa, cada participante selecionado paga o valor integral informado; quem não está selecionado paga zero.

---

## 📁 Estrutura do Projeto

```text
Trabalho-POO-Parcial/
│
├── controller/      # Controle das operações e regras de interação
│
├── exception/       # Exceções personalizadas
│
├── model/           # Classes que representam as entidades do sistema
│
├── persistence/     # Persistência e acesso aos dados
│
├── test/            # Testes da aplicação
│
├── view/            # Interfaces gráficas (Java Swing)
│
└── .gitignore

## Como executar
```bash
git clone https://github.com/DriAlexandria/Trabalho-POO-Parcial.git
cd Trabalho-POO-Parcial
javac -d out controller/*.java exception/*.java model/*.java persistence/*.java view/*.java
java -cp out cabenobolso.view.MainFrame
1 linha oculta 
## Como rodar os testes
```bash
javac -cp out -d out test/TestePlanejamentoSuite.java
java -cp out cabenobolso.test.TestePlanejamentoSuite
1 linha oculta