acc: 0
t: 1
stop: 3D0900
max: 3D0900
x: 0
y: 1
n: 3
start: NULL
loop1: LOOP $stop
JUMP $end1
CLA
ADD $max
ST $stop
CLA
ADD $acc
ADD $x
ST $acc
CLA
ADD #3
ST $n
loop2: LOOP $n
JUMP $end2
CLA
ADD $x
ST $t
CLA
ADD $y
ST $x
CLA
ADD $y
ADD $t
ST $y
LD $n
DEC
ST $n
JUMP $loop2
end2: NULL
CLA
ADD $stop
SUB $x
ST $stop
JUMP $loop1
end1: NULL
LD $acc
OUT
HLT