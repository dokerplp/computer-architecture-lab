hwptr: hw
hw: 68
65
6C
6C
6F
20
77
6F
72
6C
64
0
start: NULL
loop1: LD (hwptr)
JZ $end1
OUT
LD $hwptr
INC
ST $hwptr
JUMP $loop1
end1: NULL
HLT
n: 0
hwptr: hw
hw: 68
65
6C
6C
6F
20
77
6F
72
6C
64
0
start: NULL
IN
ST $n
loop2: LOOP $n
JUMP $end2
LD $n
OUT
IN
ST $n
JUMP $loop2
end2: NULL
HLT
acc: 0
t: 1
stop: 3D0900
max: 3D0900
x: 0
y: 1
n: 3
hwptr: hw
hw: 68
65
6C
6C
6F
20
77
6F
72
6C
64
0
start: NULL
loop3: LOOP $stop
JUMP $end3
LD $max
ST $stop
LD $acc
ADD $x
ST $acc
LD #3
ST $n
loop4: LOOP $n
JUMP $end4
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
JUMP $loop4
end4: NULL
LD $stop
SUB $x
ST $stop
JUMP $loop3
end3: NULL
LD $acc
OUT
HLT