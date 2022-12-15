acc: 0
t: 1
stop: 2710
max: 2710
x: 0
y: 1
n: 3
start: NULL
loop1: LOOP $stop
JUMP $end1
LD $max
ST $stop
LD $acc
ADD $x
ST $acc
LD #3
ST $n
loop2: LOOP $n
JUMP $end2
LD $x
ST $t
LD $y
ST $x
LD $y
ADD $t
ST $y
LD $n
DEC
ST $n
JUMP $loop2
end2: NULL
LD $stop
SUB $x
ST $stop
JUMP $loop1
end1: NULL
LD $acc
OUT
HLT