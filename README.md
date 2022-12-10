# Архитектура Компьютера. Лабораторная работа №3.

## Вариант

`alg | acc | neum | hw | instr | struct | stream | port | prob2`

* `alg`: java-подобный язык.
* `acc`: все вычисления построены вокруг регистра **AC**, выполняющего роль аккумулятора.
* `neum`: команды и данные хранятся в общей памяти. 
* `hw`: Control Unit реализован как часть модели, микрокода нет.
* `instr` заменено на `tick`: каждая инструкция расписана по-тактово
* `struct` заменено на `binary`: в связи с тем, что данные и инструкции должны храниться в общей памяти, каждая инструкция представляет собой 16-и битное число
* `stream`: ввод-вывод реализован как поток данных
* `port`: port-mapped isa

## Язык

### Набор инструкций

| Syntax | Mnemonic | Кол-во тактов  | Циклы                                | Comment                                                     |
|:-------|:---------|:---------------|:-------------------------------------|:------------------------------------------------------------|
| `1xxx` | ADD M    | 1              | Command<br/> Operand <br/> Execution | AC + DR -> AC; set ZR                                       |
| `2xxx` | SUB M    | 1              | Command<br/> Operand <br/> Execution | AC + DR -> AC; set ZR                                       |
| `3xxx` | LOOP M   | 1              | Command<br/> Operand <br/> Execution | if (DR > 0) IP + 1 -> IP                                    |
| `4xxx` | LD M     | 1              | Command<br/> Operand <br/> Execution | DR -> CR                                                    |
| `5xxx` | ST M     | 3              | Command<br/> Execution               | DR(0-11) -> DR <br/> DR -> AR; AC -> DR <br/> DR -> MEM(AR) |
| `6xxx` | JUMP M   | 1              | Command<br/> Execution               | DR(0-11) -> DR; DR(0-11) -> IP                              |
| `7xxx` | JZ M     | 1              | Command<br/> Execution               | if (ZR == 1) DR(0-11) -> DR; DR(0-11) -> IP                 |
| `F100` | HLT      | 0              | Command<br/> Execution               | stop                                                        |
| `F200` | CLA      | 1              | Command<br/> Execution               | 0 -> AC; 1 -> ZR                                            |
| `F300` | INC      | 1              | Command<br/> Execution               | AC + 1 -> AC; set ZR                                        |
| `F400` | DEC      | 1              | Command<br/> Execution               | AC - 1 -> AC; set ZR                                        | 
| `F500` | OUT      | 1              | Command<br/> Execution               | write to buffer                                             |

### Циклы




## Модель процессора

![Processor](./img/proc.png)

* `AC`: аккумулятор (16 бит)
* `ZR`: регистр состояния (ZR = 1 <=> AC = 0) (16 бит)
* `DR`: регистр данных (соединен с памятью, MEM(AR) -> DR) (16 бит)
* `IP`: счетчик команд (11 бит)
* `CR`: регистр команд (исполняемая команда) (16 бит)
* `AR`: регистр адреса (соединен с памятью, MEM(AR) -> DR) (11 бит)

#### ВУ

* `input buffer`: буфер входных данных
* `output buffer`: буфер выходных данных
* `IO`: регистр данных ВУ (из него данные попадают из/в АЛУ) (16 бит)

#### MEM

* `слово`: 16 бит
* `обьем`: 2048 слов
