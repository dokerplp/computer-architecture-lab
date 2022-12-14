t: 1
x: 1
y: 1
n: 5
start: NULL
loop1: LOOP $n
JUMP $end1
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
JUMP $loop1
end1: NULL
HLT