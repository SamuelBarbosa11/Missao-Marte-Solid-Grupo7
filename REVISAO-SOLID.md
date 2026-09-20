# Revisão final da atividade

## 1. Tabela de Contribuições

| Integrante            | Matrícula | Usuário Git     | Exercícios e funcionalidades                                            |
| --------------------- | --------- | --------------- | ----------------------------------------------------------------------- |
| Samuel Miguel Barbosa | 2517428   | SamuelBarbosa11 | Refatoração principal, `Main`, integração das camadas e validação geral |
| João Gabriel Rinaldi  | 2510365   | JgRM0           | Modelos, SOLID, UML e interfaces                                        |
| Rafael Dantas         | 2517979   | r-dantas9       | Documentação, testes manuais e evidências                               |

---

Data: 24.09.26

## Como validei a solução

Registre os comandos executados e os fluxos testados:

- [x] compilação do código inicial com `javac -encoding UTF-8 -d out-original`;
- [x] compilação da versão refatorada com `javac -encoding UTF-8 -d out`;
- [x] início de uma missão e renderização do mapa;
- [x] aborto voluntário da missão e retorno ao menu;
- [x] movimentação, embarque e conclusão da missão;
- [x] consulta e reset do ranking;
- [x] execução pelo `run.cmd start` e saída imediata;
- [x] teste de mapa insuficiente na dificuldade difícil, corrigido com tamanho mínimo automático.

## Achados da revisão

### Mapa pequeno e posições insuficientes

```text
Local: JogoService.lerTamanhoMapa e criação da missão
Princípio relacionado: SRP e robustez da regra de negócio
Observação: mapas pequenos não comportavam todos os passageiros e perigos e causavam IllegalStateException.
Impacto: a aplicação encerrava durante o início da missão, sem permitir ao usuário jogar.
Proposta: calcular o tamanho mínimo por dificuldade e ajustar entradas menores antes da geração aleatória.
Prioridade: alta
```

### Responsabilidade do serviço

```text
Local: JogoService
Princípio relacionado: SRP
Observação: o serviço coordena menu, entrada, criação da missão, pontuação e encerramento.
Impacto: a integração fica simples para o projeto atual, mas a classe tende a crescer com novas regras.
Proposta: manter como está neste escopo; extrair uma MissaoFactory e um serviço de pontuação se o jogo crescer.
Prioridade: baixa
```

### Criação de passageiros fixa no serviço

```text
Local: JogoService.posicionarPassageiros e model.Astronauta
Princípio relacionado: OCP
Observação: o embarque e a pontuação são polimórficos e aceitam qualquer subtipo de Passageiro, mas a criação continua fixa no serviço: posicionarPassageiros alterna Professor, Engenheiro e Professor por indice % 3. Astronauta existe no modelo, tem pontuação e símbolo próprios e aparece na legenda do mapa, mas nunca é instanciado.
Impacto: incluir um novo tipo de passageiro exige alterar o fluxo principal, e não apenas estender o domínio; o tipo não utilizado dá falsa impressão de extensibilidade e não é exercitado por nenhum teste.
Proposta: extrair a criação para uma fábrica (PassageiroFactory) ou receber os tipos disponíveis no construtor do serviço, e incluir Astronauta na distribuição.
Prioridade: média
```

### Tipo do passageiro guardado como texto

```text
Local: model.Passageiro e subclasses
Princípio relacionado: LSP
Observação: tornar Passageiro abstrata com getPontuacao() abstrato corrigiu o problema da versão original, em que a base era concreta e devolvia 10 pontos por padrão. Resta um ponto frágil: o campo tipo é uma String recebida pelo construtor e não tem vínculo com a classe real, então nada impede uma subclasse de se declarar com o tipo de outra.
Impacto: qualquer regra ou relatório que compare getTipo() pode divergir do tipo real do objeto, e a inconsistência só aparece em tempo de execução.
Proposta: derivar o rótulo da própria classe (getClass().getSimpleName()) ou transformá-lo em método abstrato ao lado de getPontuacao(), eliminando o estado redundante.
Prioridade: baixa
```

### Interface Movel sem cliente na Nave

```text
Local: model.Movel e model.Nave
Princípio relacionado: ISP
Observação: a separação entre Posicionavel e Movel é adequada e evita que Asteroide e Passageiro precisem de um mover() vazio. Porém Nave implementa Movel sem que nenhum cliente use esse método: JogoService movimenta a nave apenas por moverComLimites e Missao.moverInimigos chama mover somente nos inimigos. Nave.mover também ignora os limites do mapa.
Impacto: método público sem cliente amplia a superfície da classe, precisa ser mantido e pode ser chamado por engano, tirando a nave da área jogável.
Proposta: remover implements Movel de Nave, ou fazer moverComLimites validar os limites e delegar a mover, eliminando a duplicação do cálculo de posição.
Prioridade: média
```

## Decisões com as quais concordo

Manter `RankingRepository` como abstração foi uma boa decisão. `JogoService` não
precisa conhecer o arquivo nem a implementação concreta, o que facilita trocar
a persistência e criar testes com um repositório em memória.

## Decisões com as quais não concordo

A geração aleatória da missão ainda fica dentro de `JogoService`. Para o
projeto atual isso evita abstrações desnecessárias, mas eu a extrairia para uma
`MissaoFactory` caso surgissem mais mapas, cenários ou regras de distribuição.

## Melhoria implementada (opcional)

Foi implementada a validação do tamanho mínimo do mapa em
`JogoService.lerTamanhoMapa`. Antes, uma missão difícil em um mapa de tamanho
1 terminava com `IllegalStateException` por falta de posições livres. Depois,
o programa calcula o mínimo necessário por dificuldade, informa o ajuste e
continua a execução normalmente.
