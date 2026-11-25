# Projeto Parcial da Disciplina de POO.

### Versão do JDK:
- 17

### Dependências:
- Nenhuma. Foi utilizado o próprio código nativo do Java com o swing

### Instalação:
- Clone o projeto:
``` bash
git clone https://github.com/DriAlexandria/Trabalho-POO-Parcial.git
```
- Entre na pasta do projeto:
``` bash
cd Trabalho-POO-Parcial
```
- Execução: executar o arquivo **MainFrame** no diretório **view**

### Estrutura do Projeto

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
└── .gitignore       # Arquivos ignorados pelo Git
```
### Compilação (versão por terminal):
``` 
mkdir out
dir /s /b controller\.java exception\.java model\.java persistence\.java view\*.java > fontes.txt
javac -d out @fontes.txt
```
### Execução:
```
java -cp out cabenobolso.view.MainFrame
```
### Testes:
- Como rodar os testes manualmente:
```
javac -cp out -d out test\TestePlanejamentoSuite.java
java -cp out cabenobolso.test.TestePlanejamentoSuite
```