# Missão Marte Unifor — Refatoração SOLID (Grupo 7)

Atividade prática de refatoração em Java. O jogo de console original foi
preservado em [`src/exercicio10`](src/exercicio10) e a versão refatorada,
organizada em camadas e guiada pelos princípios SOLID, está em
[`src/solidexercicio10`](src/solidexercicio10).

## Equipe

| Integrante            | Matrícula | Usuário Git     | Responsabilidade                                      |
| --------------------- | --------- | --------------- | ----------------------------------------------------- |
| Samuel Miguel Barbosa | 2517428   | SamuelBarbosa11 | Refatoração principal, `Main`, integração das camadas |
| João Gabriel Rinaldi  | 2510365   | JgRM0           | Entidades do domínio, OCP/LSP/ISP e diagramas UML     |
| Rafael Dantas         | 2517979   | r-dantas9       | Documentação, testes manuais e evidências             |

## Como compilar e executar

**Requisito:** JDK 14 ou superior — o código usa *switch expressions*
(`case FACIL -> 8`). Recomendado o JDK 17 LTS. Um JRE sozinho não compila.

Na raiz do projeto, no Windows:

```cmd
run start
```

O script cria a pasta `out`, configura UTF-8, compila os cinco pacotes de
`solidexercicio10` e inicia `solidexercicio10.Main`.

Manualmente, se preferir:

```cmd
javac -encoding UTF-8 -d out src\solidexercicio10\*.java src\solidexercicio10\model\*.java src\solidexercicio10\presentation\*.java src\solidexercicio10\repository\*.java src\solidexercicio10\service\*.java
java -Dfile.encoding=UTF-8 -cp out solidexercicio10.Main
```

Para comparar com a versão original:

```cmd
javac -encoding UTF-8 -d out-original src\exercicio10\*.java
java -Dfile.encoding=UTF-8 -cp out-original exercicio10.Main
```

## Diagramas UML

Os arquivos-fonte (`.puml`) e as imagens geradas (`.png`) estão em
[`docs/uml/`](docs/uml). Os diagramas descrevem a **estrutura real
implementada**, e não a estrutura sugerida pelo tutorial.

### 1. Diagrama de classes do domínio

Fonte: [`docs/uml/diagrama-classes-model.puml`](docs/uml/diagrama-classes-model.puml)

![Diagrama de classes do domínio](docs/uml/diagrama-classes-model.png)

Representa o pacote `solidexercicio10.model` e as decisões de modelagem que
sustentam os princípios de extensão e substituição:

- **`EntidadeMapa` como raiz abstrata.** Tudo que ocupa uma célula do mapa
  herda dela e é obrigado a responder `getSimbolo()`. É isso que permite ao
  `MapaRenderer` desenhar o mapa perguntando o símbolo a cada entidade, em vez
  de manter uma cadeia de `if/else` por tipo — **OCP**: um novo tipo de
  entidade não obriga a alterar a renderização.

- **`Passageiro` abstrata, com `getPontuacao()` sem implementação padrão.**
  No código original a classe base era concreta e devolvia 10 pontos, ou seja,
  a base representava um passageiro que não existe no jogo, e `Professor`
  apenas repetia o mesmo valor. Tornando-a abstrata, cada subclasse é obrigada
  a declarar sua pontuação (Professor 15, Engenheiro 20, Astronauta 10) e
  qualquer uma pode substituir a base sem surpresa para `Missao` e
  `JogoService` — **LSP**.

- **Dois contratos pequenos em vez de um grande.** `Posicionavel` (`getX`,
  `getY`) descreve o que ocupa uma posição; `Movel` (`mover`) descreve o que
  se desloca. Só `Nave` e `Inimigo` implementam `Movel`, então `Asteroide` e
  `Passageiro` não precisam carregar um `mover()` vazio — **ISP**.

- **Composição e agregação diferenciadas.** A `Missao` cria e é dona das
  listas de passageiros, asteroides e inimigos (composição): elas nascem e
  morrem com a missão. Já a `Nave` é construída pelo `JogoService` e injetada
  no construtor da `Missao` (agregação), que portanto não controla o ciclo de
  vida dela.

- **Transferência de posse no embarque.** O `Passageiro` aparece ligado tanto
  à `Missao` (aguardando resgate) quanto à `Nave` (a bordo). As duas pontas
  existem, mas nunca contêm o mesmo objeto ao mesmo tempo: ao embarcar, ele
  sai da lista da missão e entra na da nave.

- **Multiplicidades por dificuldade.** 4 passageiros / 2 asteroides /
  2 inimigos no fácil, 5 / 2 / 2 no médio e 6 / 3 / 3 no difícil. A capacidade
  da `Nave` é definida como o total de passageiros da partida.

- **`Dificuldade` isolada.** O enum não se associa a nenhuma outra entidade do
  domínio: ele é consumido por `JogoService` (pontuação inicial e quantidade
  de entidades) e por `RankingEntry` (registro da partida). Permanece em
  `model` por ser vocabulário do domínio, não regra de fluxo.

### 2. Diagrama de pacotes

Fonte: [`docs/uml/diagrama-pacotes.puml`](docs/uml/diagrama-pacotes.puml)

![Diagrama de pacotes](docs/uml/diagrama-pacotes.png)

Mostra as cinco divisões do projeto e a direção das dependências, que apontam
sempre para dentro: `Main` → `service` → (`presentation`, `repository`) →
`model`.

- **`model` é a camada mais estável.** Nenhuma classe do domínio importa
  `service`, `presentation` ou `repository` — apenas `java.util`. Por isso as
  entidades podem ser testadas isoladamente, sem console e sem disco.

- **O serviço depende do contrato, não do detalhe — DIP.** `JogoService`
  importa `repository.RankingRepository`, mas **não** importa
  `RankingService`. O serviço nunca sabe que o ranking vive em um arquivo de
  texto. No diagrama, a interface aparece no topo do pacote `repository` e a
  implementação concreta logo abaixo dela, justamente para tornar essa direção
  visível.

- **`Main` é o *composition root*.** É o único ponto do programa que conhece a
  implementação concreta, e declara a variável pelo tipo do contrato:
  `RankingRepository repo = new RankingService(arquivo)`. Trocar a persistência
  por um banco de dados ou por uma versão em memória altera apenas essa linha.

- **`presentation` só lê o estado da missão.** O `MapaRenderer` recebe a
  `Missao` e os limites do mapa e desenha; ele não decide nada sobre a regra do
  jogo — **SRP**.

### Como regenerar as imagens

Os `.png` foram gerados com o PlantUML 1.2024.7, que já traz o Graphviz
embutido (não é preciso instalar o Graphviz separadamente):

```bash
java -jar plantuml.jar -charset UTF-8 -tpng docs/uml/diagrama-classes-model.puml docs/uml/diagrama-pacotes.puml
```

O `-charset UTF-8` é necessário porque os diagramas contêm acentuação; sem ele
o PlantUML assume a codificação padrão do sistema (Cp1252 no Windows) e os
rótulos saem corrompidos.

## Estrutura do repositório

```text
src/exercicio10/          versão original preservada, para comparação
src/solidexercicio10/     versão refatorada (Main, model, service, presentation, repository)
docs/uml/                 diagramas UML (.puml e .png)
apostilas-solid/          material de apoio sobre cada princípio
REVISAO-SOLID.md          revisão crítica da solução e testes realizados
Atividade.md              enunciado e roteiro da atividade
run.cmd                   script de compilação e execução no Windows
```

## Documentos relacionados

- [REVISAO-SOLID.md](REVISAO-SOLID.md) — revisão crítica, achados e testes.
- [src/solidexercicio10/README.md](src/solidexercicio10/README.md) — detalhes da
  versão refatorada, regras do jogo e testes manuais recomendados.
- [src/README.md](src/README.md) — tutorial passo a passo da refatoração.
- [Divisao-Tarefas-Integrantes.md](Divisao-Tarefas-Integrantes.md) — divisão de
  responsabilidades da equipe.

## Pendências desta entrega

Seções exigidas pelo enunciado que ainda precisam ser preenchidas pelos
responsáveis:

- **Alterações realizadas e decisões de projeto** — visão geral da refatoração,
  além das decisões de modelagem já descritas acima (Samuel).
- **Limitações que permanecem** e consolidação das evidências de teste, a
  partir do que já está em [REVISAO-SOLID.md](REVISAO-SOLID.md) (Rafael).
