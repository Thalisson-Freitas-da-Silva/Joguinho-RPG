# Fazenda do Recomeço

Um RPG de fazenda em pixel art para navegador. Um fazendeiro solitário chega a
uma propriedade abandonada e, com trabalho, transforma ruínas em casa, campos,
animais e um lago cheio de vida.

## Executar

O jogo usa somente Python 3 (biblioteca padrão), HTML, CSS e JavaScript.

```bash
python3 app.py
```

Depois, abra [http://localhost:8000](http://localhost:8000) no navegador.

## Controles

| Tecla | Ação |
| --- | --- |
| `WASD` ou setas | Caminhar pela fazenda |
| `E` ou espaço | Interagir com o local próximo |
| `N` | Começar o próximo dia |
| `R` | Recomeçar a jornada |

## Objetivo

1. Visite o **lago** para recolher madeira.
2. Use os **canteiros** para arar, plantar e colher. As plantas amadurecem a
   cada novo dia.
3. Na **oficina**, construa e melhore seu lar.
4. No **celeiro**, construa o galinheiro e depois o redil para acolher animais.
5. Converse com **Lia** e **Bento**, os vizinhos que voltarão a trazer vida à região.

O estado e as regras de progressão pertencem ao servidor Python; a interface no
navegador apenas desenha o mundo e envia as ações do jogador.
