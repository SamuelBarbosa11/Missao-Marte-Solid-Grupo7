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
- [ ] movimentação, embarque e conclusão da missão;
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
